package infra;

import services.*;
import models.*;
import stats.*;

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
    private final StatisticsCollector statisticsCollector;
    
    /**
     * Конструктор
     */
    public SimulationRunner(SimulationConfig config) {
        this.config = config;
        this.statisticsCollector = new StatisticsCollector();
    }
    
    /**
     * Запускает симуляцию
     */
    public void runSimulation() {
        System.out.println("=== ЗАПУСК СИМУЛЯЦИИ ===");
        System.out.println("Конфигурация: " + config);
        
        ExecutorService executor = null;
        
        try {
            // 1. Создаем все компоненты
            BlockingQueue<RideRequest> requestQueue = new LinkedBlockingQueue<>();
            List<TaxiWorker> taxis = createTaxis();
            DispatchStrategy strategy = createStrategy(config.getStrategyType());
            Dispatcher dispatcher = new Dispatcher(requestQueue, taxis, strategy, statisticsCollector);
            RequestGenerator generator = new RequestGenerator(requestQueue, config);
            
            // Устанавливаем обратные вызовы для такси
            for (TaxiWorker taxi : taxis) {
                taxi.setDispatcherCallback(dispatcher);
            }
            
            System.out.println("\nИнициализация завершена:");
            System.out.println("- Такси: " + taxis.size() + " единиц");
            System.out.println("- Стратегия: " + strategy.getName());
            System.out.println("- Длительность: " + config.getSimulationDurationSeconds() + " сек");
            System.out.println("- Интервал запросов: " + config.getMeanRequestIntervalMillis() + " мс");
            System.out.println();
            
            // 2. Запускаем потоки
            executor = Executors.newFixedThreadPool(taxis.size() + 2);
            
            // Запускаем такси
            for (TaxiWorker taxi : taxis) {
                executor.submit(taxi);
            }
            
            // Запускаем диспетчера и генератор
            executor.submit(dispatcher);
            executor.submit(generator);
            
            System.out.println("Все потоки запущены. Симуляция работает " + 
                             config.getSimulationDurationSeconds() + " секунд...\n");
            
            // 3. Ждем указанное время
            TimeUnit.SECONDS.sleep(config.getSimulationDurationSeconds());
            
            System.out.println("\nВремя симуляции истекло. Начинаем остановку...");
            
        } catch (InterruptedException e) {
            System.err.println("Симуляция была прервана");
            Thread.currentThread().interrupt();
        } finally {
            // 4. Останавливаем все потоки
            if (executor != null) {
                shutdownExecutor(executor);
            }
            
            // 5. Выводим статистику
            System.out.println("\n=== СИМУЛЯЦИЯ ЗАВЕРШЕНА ===");
            statisticsCollector.printSummary();
        }
    }
    
    /**
     * Корректно завершает работу ExecutorService
     */
    private void shutdownExecutor(ExecutorService executor) {
        try {
            System.out.println("Запрашиваем завершение потоков...");
            
            // Запрещаем новые задачи
            executor.shutdown();
            
            // Ждем завершения существующих задач
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                System.err.println("Некоторые потоки не завершились вовремя. Принудительная остановка...");
                executor.shutdownNow();
                
                // Ждем еще немного
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Потоки не реагируют на принудительную остановку");
                }
            }
            
            System.out.println("Все потоки завершены корректно");
            
        } catch (InterruptedException e) {
            System.err.println("Остановка была прервана");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
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
     * Создает стратегию по типу
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
}
