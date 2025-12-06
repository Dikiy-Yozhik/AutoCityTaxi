package ru.mystudent.taxi.infra;

import ru.mystudent.taxi.service.*;
import ru.mystudent.taxi.model.*;
import ru.mystudent.taxi.stats.*;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    public void runSimulation() {
        System.out.println("Запуск симуляции с конфигурацией: " + config);
        
        // TODO: Реализовать полную логику симуляции (будет в шаге 9)
        // Пока просто создаем основные компоненты
        
        // 1. Создаем очередь заявок
        BlockingQueue<RideRequest> requestQueue = new LinkedBlockingQueue<>();
        
        // 2. Создаем такси
        List<TaxiWorker> taxis = createTaxis();
        
        // 3. Создаем стратегию
        DispatchStrategy strategy = createStrategy(config.getStrategyName());
        
        // 4. Создаем диспетчера
        Dispatcher dispatcher = new Dispatcher(requestQueue, taxis, strategy);
        
        // 5. Создаем генератор запросов
        RequestGenerator generator = new RequestGenerator(requestQueue, config);
        
        // 6. Устанавливаем обратные вызовы для такси
        for (TaxiWorker taxi : taxis) {
            taxi.setDispatcherCallback(dispatcher);
        }
        
        System.out.println("Симуляция инициализирована. " + 
                          taxis.size() + " такси, стратегия: " + strategy.getName());
        
        // TODO: Запуск потоков, ожидание, остановка (будет в шаге 9)
        
        System.out.println("Симуляция завершена.");
    }
    
    /**
     * Создает список такси
     */
    private List<TaxiWorker> createTaxis() {
        List<TaxiWorker> taxis = new ArrayList<>();
        
        // Для простоты создаем такси разных типов
        TaxiType[] types = TaxiType.values();
        
        for (int i = 0; i < config.getNumberOfTaxis(); i++) {
            long taxiId = i + 1;
            TaxiType type = types[i % types.length];
            
            // Начальная позиция такси - случайная точка в городе
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
     * Создает стратегию по названию
     */
    private DispatchStrategy createStrategy(String strategyName) {
        // TODO: Реализовать создание конкретных стратегий (будет в шаге 7)
        // Пока возвращаем заглушку
        return new DispatchStrategy() {
            @Override
            public TaxiWorker selectTaxi(List<TaxiWorker> taxis, RideRequest request) {
                return null;
            }
            
            @Override
            public String getName() {
                return "Заглушка (не реализовано)";
            }
        };
    }
    
    /**
     * Останавливает все компоненты симуляции
     */
    private void stopSimulation(RequestGenerator generator, 
                               Dispatcher dispatcher, 
                               List<TaxiWorker> taxis) {
        // TODO: Реализовать корректную остановку (будет в шаге 9)
        System.out.println("Остановка симуляции...");
        
        generator.stop();
        dispatcher.stop();
        
        // TODO: Остановить такси через poison pill
    }
}