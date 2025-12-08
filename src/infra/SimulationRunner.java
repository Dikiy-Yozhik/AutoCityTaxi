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

public class SimulationRunner {
    
    private final SimulationConfig config;
    private final StatisticsCollector statisticsCollector;

    public SimulationRunner(SimulationConfig config) {
        this.config = config;
        this.statisticsCollector = new StatisticsCollector();
    }
    
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
            
            // 2. СОЗДАЕМ БАРЬЕР ДЛЯ СТАРТА
            java.util.concurrent.CountDownLatch startSignal = new java.util.concurrent.CountDownLatch(1);
            
            // 3. Запускаем потоки
            executor = Executors.newFixedThreadPool(taxis.size() + 2);
            
            // Запускаем такси
            for (TaxiWorker taxi : taxis) {
                executor.submit(() -> {
                    try {
                        // Ждем команды старта
                        startSignal.await();
                        taxi.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            
            // Запускаем диспетчера
            executor.submit(() -> {
                try {
                    startSignal.await();
                    dispatcher.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // Запускаем генератор
            executor.submit(() -> {
                try {
                    startSignal.await();
                    generator.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            // 4. Даем команду "СТАРТ!" - ВСЕ потоки запускаются ОДНОВРЕМЕННО
            System.out.println("Все потоки запущены. Симуляция работает " + 
                            config.getSimulationDurationSeconds() + " секунд...\n");
            startSignal.countDown();
            
            // 5. Ждем указанное время с проверкой прерывания
            long startTime = System.currentTimeMillis();
            long remainingTime = config.getSimulationDurationSeconds() * 1000L;
            
            while (remainingTime > 0 && !Thread.currentThread().isInterrupted()) {
                long sleepTime = Math.min(remainingTime, 1000L); 
                Thread.sleep(sleepTime);
                remainingTime = config.getSimulationDurationSeconds() * 1000L - 
                            (System.currentTimeMillis() - startTime);
            }
            
            if (Thread.currentThread().isInterrupted()) {
                System.out.println("\nСимуляция прервана пользователем.");
                throw new InterruptedException("Прервано пользователем");
            }
            
            System.out.println("\nВремя симуляции истекло. Начинаем остановку...");
            
            // 6. ПРАВИЛЬНАЯ ПОСЛЕДОВАТЕЛЬНОСТЬ ОСТАНОВКИ
            System.out.println("1. Останавливаем генератор запросов...");
            generator.stop();
            
            // Даем время генератору отправить poison pill
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            System.out.println("2. Даем диспетчеру время обработать оставшиеся заказы...");
            // Ждем пока очередь диспетчера опустеет 
            int maxWaitCycles = 50;
            for (int i = 0; i < maxWaitCycles && !requestQueue.isEmpty(); i++) {
                try {
                    Thread.sleep(100);
                    if (Thread.currentThread().isInterrupted()) break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            System.out.println("3. Останавливаем диспетчер...");
            dispatcher.stop();
            
            System.out.println("4. Останавливаем такси...");
            for (TaxiWorker taxi : taxis) {
                taxi.stop();
            }
            
            System.out.println("5. Ждем завершения текущих поездок (2 секунды)...");
            // Даем такси время завершить текущие поездки
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
        } catch (InterruptedException e) {
            if (!"Прервано пользователем".equals(e.getMessage())) {
                System.err.println("Симуляция была прервана");
            }
            Thread.currentThread().interrupt();
        } finally {
            // 7. Останавливаем executor
            if (executor != null) {
                shutdownExecutor(executor);
            }
            
            // 8. Выводим статистику
            System.out.println("\n=== СИМУЛЯЦИЯ ЗАВЕРШЕНА ===");
            statisticsCollector.printSummary();
        }
    }
    
    private void shutdownExecutor(ExecutorService executor) {
        System.out.println("Завершение работы всех потоков...");
        
        try {
            executor.shutdown();
            
            if (!executor.awaitTermination(25, TimeUnit.SECONDS)) {
                System.out.println("Некоторые потоки не завершились, применяем принудительную остановку...");
                executor.shutdownNow();
                
                executor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            System.out.println("Процесс остановки был ускорен...");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Все потоки остановлены.");
    }

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
            TaxiWorker taxi = new TaxiWorker(taxiId, type, startLocation, config.getTaxiSpeed());
            taxis.add(taxi);
        }
        
        return taxis;
    }
    
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
