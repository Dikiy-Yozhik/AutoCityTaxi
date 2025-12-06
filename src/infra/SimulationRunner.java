package ru.mystudent.taxi.infra;

import ru.mystudent.taxi.service.*;
import ru.mystudent.taxi.model.*;
import ru.mystudent.taxi.stats.*;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Запускает и управляет симуляцией
 */
public class SimulationRunner {
    
    private final SimulationConfig config;
    
    /**
     * Конструктор
     */
    public SimulationRunner(SimulationConfig config) {
        this.config = config;
    }
    
    /**
     * Запускает симуляцию
     */
    public void runSimulation() throws InterruptedException {
        System.out.println("=== ЗАПУСК СИМУЛЯЦИИ ===");
        System.out.println("Конфигурация: " + config);
        
        // 1. Создаем очередь заявок
        BlockingQueue<RideRequest> requestQueue = new LinkedBlockingQueue<>();
        
        // 2. Создаем такси
        List<TaxiWorker> taxis = createTaxis();
        
        // 3. Создаем стратегию
        DispatchStrategy strategy = createStrategy(config.getStrategyType());
        
        // 4. Создаем диспетчера
        Dispatcher dispatcher = new Dispatcher(requestQueue, taxis, strategy);
        
        // 5. Создаем генератор запросов
        RequestGenerator generator = new RequestGenerator(requestQueue, config);
        
        // 6. Устанавливаем обратные вызовы для такси
        for (TaxiWorker taxi : taxis) {
            taxi.setDispatcherCallback(dispatcher);
        }
        
        System.out.println("\nИнициализация завершена:");
        System.out.println("- Такси: " + taxis.size() + " единиц");
        System.out.println("- Стратегия: " + strategy.getName());
        System.out.println("- Длительность: " + config.getSimulationDurationSeconds() + " сек");
        System.out.println("- Интервал запросов: " + config.getMeanRequestIntervalMillis() + " мс");
        System.out.println();
        
        // 7. Запускаем потоки
        ExecutorService executor = Executors.newFixedThreadPool(taxis.size() + 2);
        
        // Запускаем такси
        for (TaxiWorker taxi : taxis) {
            executor.submit(taxi);
        }
        
        // Запускаем диспетчера и генератор
        executor.submit(dispatcher);
        executor.submit(generator);
        
        System.out.println("Все потоки запущены. Симуляция работает...\n");
        
        // 8. Ждем указанное время
        Thread.sleep(config.getSimulationDurationSeconds() * 1000L);
        
        // 9. Останавливаем симуляцию
        System.out.println("\nВремя симуляции истекло. Останавливаем...");
        stopSimulation(generator, dispatcher, taxis, executor);
        
        System.out.println("\n=== СИМУЛЯЦИЯ ЗАВЕРШЕНА ===");
        printStatistics(dispatcher, taxis);
    }
    
    /**
     * Создает список такси
     */
    private List<TaxiWorker> createTaxis() {
        List<TaxiWorker> taxis = new ArrayList<>();
        
        TaxiType[] types = TaxiType.values();
        
        for (int i = 0; i < config.getNumberOfTaxis(); i++) {
            long taxiId = i + 1;
            TaxiType type = types[i % types.length];
            
            double x = config.getCityMinX() + 
                      Math.random() * (config.getCityMaxX() - config.getCityMinX());
            double y = config.getCityMinY() + 
                      Math.random() * (config.getCityMaxY() - config.getCityMinY());
            
            Point startLocation = new Point(x, y);
            TaxiWorker taxi = new TaxiWorker(taxiId, type, startLocation);
            taxis.add(taxi);
        }
        
        return taxis;
    }
    
    /**
     * Создает стратегию по типу (новая версия с enum)
     */
    private DispatchStrategy createStrategy(StrategyType strategyType) {
        switch (strategyType) {
            case NEAREST:
                return new NearestTaxiStrategy();
            case LEAST_LOADED:
                return new LeastLoadedTaxiStrategy();
            default:
                System.out.println("Неизвестный тип стратегии '" + strategyType + 
                                 "'. Используется NearestTaxiStrategy по умолчанию.");
                return new NearestTaxiStrategy();
        }
    }
    
    /**
     * Создает стратегию по названию (старая версия для обратной совместимости)
     */
    private DispatchStrategy createStrategy(String strategyName) {
        StrategyType type = StrategyType.fromCode(strategyName);
        return createStrategy(type);
    }
    
    /**
     * Останавливает все компоненты симуляции
     */
    private void stopSimulation(RequestGenerator generator, 
                               Dispatcher dispatcher, 
                               List<TaxiWorker> taxis,
                               ExecutorService executor) throws InterruptedException {
        // Останавливаем генератор
        generator.stop();
        
        // Останавливаем диспетчер
        dispatcher.stop();
        
        // Останавливаем такси
        for (TaxiWorker taxi : taxis) {
            taxi.stop();
        }
        
        // Ждем завершения всех потоков
        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.err.println("Не все потоки завершились корректно");
            executor.shutdownNow();
        }
    }
    
    /**
     * Выводит статистику по завершению симуляции
     */
    private void printStatistics(Dispatcher dispatcher, List<TaxiWorker> taxis) {
        System.out.println("Диспетчер назначил поездок: " + dispatcher.getTotalAssignedRides());
        System.out.println("Не удалось назначить: " + dispatcher.getFailedAssignments());
        
        // Подсчитываем общую статистику
        int totalCompletedRides = 0;
        double totalDistance = 0.0;
        
        System.out.println("\nСтатистика по такси:");
        System.out.println("ID  | Тип       | Поездки | Пробег   | Статус");
        System.out.println("----+-----------+---------+----------+-----------");
        
        for (TaxiWorker taxi : taxis) {
            totalCompletedRides += taxi.getCompletedRides();
            totalDistance += taxi.getTotalDistance();
            
            System.out.printf("%-3d | %-9s | %-7d | %-8.2f | %s%n",
                taxi.getId(),
                taxi.getType(),
                taxi.getCompletedRides(),
                taxi.getTotalDistance(),
                taxi.getStatus());
        }
        
        System.out.println("\nОбщая статистика:");
        System.out.println("Всего выполнено поездок: " + totalCompletedRides);
        System.out.println("Общий пробег: " + String.format("%.2f", totalDistance));
        
        // Средняя загрузка такси
        if (taxis.size() > 0) {
            double avgRidesPerTaxi = (double) totalCompletedRides / taxis.size();
            System.out.println("Средняя загрузка такси: " + 
                             String.format("%.2f", avgRidesPerTaxi) + " поездок");
        }
    }
}