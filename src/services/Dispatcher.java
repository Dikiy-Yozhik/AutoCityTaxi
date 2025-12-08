package services;

import models.RideRequest;
import models.TaxiStatus;
import stats.StatisticsCollector;
import util.FareCalculator;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class Dispatcher implements Runnable, DispatcherCallback {
    
    private final BlockingQueue<RideRequest> requestQueue;
    private final List<TaxiWorker> taxis;
    private final DispatchStrategy strategy;
    private final ReentrantLock selectionLock;
    private final StatisticsCollector statisticsCollector;
    private volatile boolean running = true;
    
    private int totalAssignedRides = 0;
    private int failedAssignments = 0;
 
    public Dispatcher(BlockingQueue<RideRequest> requestQueue, 
                     List<TaxiWorker> taxis, 
                     DispatchStrategy strategy,
                     StatisticsCollector statisticsCollector) {
        this.requestQueue = requestQueue;
        this.taxis = taxis;
        this.strategy = strategy;
        this.selectionLock = new ReentrantLock();
        this.statisticsCollector = statisticsCollector;

        // регестрируем такси в сборщике статистики
        for (TaxiWorker taxi : taxis) {
            statisticsCollector.registerTaxi(taxi.getId(), taxi.getType());
        }
    }

    @Override
    public void run() {
        System.out.println("Диспетчер запущен. Стратегия: " + strategy.getName());
        System.out.println("Доступно такси: " + taxis.size());
        
        while (running) {
            try {
                // Забираем заказ из общей очереди
                RideRequest request = requestQueue.take();
                
                // Проверяем, не poison pill ли это
                if (isPoisonPill(request)) {
                    System.out.println("Диспетчер получил poison pill. Завершаю работу...");
                    break;
                }
                
                System.out.println("Диспетчер обрабатывает заказ #" + request.getId() + 
                                " (тип: " + request.getRequestedType() + ")");
                
                // Выбираем такси через стратегию
                TaxiWorker selectedTaxi = selectTaxiForRequest(request);
                
                if (selectedTaxi != null) {
                    // Проверяем running перед назначением
                    if (!running) {
                        System.out.println("Диспетчер: получена команда остановки, отменяю назначение заказа #" + request.getId());
                        break;
                    }
                    
                    // Помечаем такси занятым
                    selectedTaxi.setStatus(TaxiStatus.TO_PICKUP);
                    
                    // Кладем заказ в личную очередь такси
                    selectedTaxi.assignRequest(request);
                    
                    totalAssignedRides++;
                    System.out.println("Заказ #" + request.getId() + 
                                    " назначен такси " + selectedTaxi.getId() + 
                                    " (тип: " + selectedTaxi.getType() + ")");
                } else {
                    failedAssignments++;
                    System.out.println("Нет подходящего такси для заказа #" + request.getId() + 
                                    " (тип: " + request.getRequestedType() + ")");
                }
                
            } catch (InterruptedException e) {
                if (!running) {
                    System.out.println("Диспетчер прерван по команде остановки");
                    break;
                } else {
                    System.err.println("Диспетчер был неожиданно прерван");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        finishDispatcherWork();
    }


    private void finishDispatcherWork() {
        System.out.println("Диспетчер завершает работу...");
        
        // 1. Обрабатываем оставшиеся заказы в очереди (если есть)
        int remainingRequests = requestQueue.size();
        if (remainingRequests > 0) {
            System.out.println("В очереди осталось " + remainingRequests + " заказов. Обрабатываю...");
            
            // Обрабатываем заказы пока очередь не опустеет или не получим poison pill
            while (!requestQueue.isEmpty()) {
                try {
                    RideRequest request = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (request == null) break;
                    
                    if (isPoisonPill(request)) {
                        System.out.println("Диспетчер: получен дополнительный poison pill");
                        break;
                    }
                    
                    // Пытаемся назначить оставшиеся заказы
                    TaxiWorker selectedTaxi = selectTaxiForRequest(request);
                    if (selectedTaxi != null) {
                        selectedTaxi.assignRequest(request);
                        totalAssignedRides++;
                        System.out.println("Диспетчер: назначил оставшийся заказ #" + request.getId());
                    } else {
                        failedAssignments++;
                        System.out.println("Диспетчер: не удалось назначить оставшийся заказ #" + request.getId());
                    }
                    
                } catch (InterruptedException e) {
                    System.out.println("Диспетчер прерван при обработке оставшихся заказов");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        // 2. Останавливаем такси
        stopAllTaxis();
        
        // 3. Выводим итоговую статистику
        System.out.println("Диспетчер остановлен. Назначено поездок: " + 
                        totalAssignedRides + ", не удалось назначить: " + failedAssignments);
        
        // 4. Сообщаем о качестве завершения
        if (requestQueue.isEmpty()) {
            System.out.println("Диспетчер: все заказы обработаны, очередь пуста.");
        } else {
            System.out.println("Диспетчер: в очереди остались необработанные заказы: " + requestQueue.size());
        }
    }

    
    private TaxiWorker selectTaxiForRequest(RideRequest request) {
        selectionLock.lock();
        try {
            return strategy.selectTaxi(taxis, request);
        } finally {
            selectionLock.unlock();
        }
    }
    
    private boolean isPoisonPill(RideRequest request) {
        return request.getId() == -1; 
    }
    
    private void stopAllTaxis() {
        System.out.println("Диспетчер останавливает все такси...");
        int stoppedCount = 0;
        
        for (TaxiWorker taxi : taxis) {
            try {
                taxi.stop();
                stoppedCount++;
                System.out.println("Диспетчер: отправил команду остановки такси " + taxi.getId());
            } catch (Exception e) {
                System.err.println("Диспетчер: ошибка при остановке такси " + taxi.getId() + ": " + e.getMessage());
            }
        }
        
        System.out.println("Диспетчер: команды остановки отправлены " + stoppedCount + " такси из " + taxis.size());
    }
    
    public void stop() {
        this.running = false;
        
        // Отправляем poison pill вместо interrupt
        try {
            RideRequest dispatcherPoisonPill = RideRequest.createPoisonPill();
            
            // Пытаемся положить poison pill с таймаутом (100 мс)
            boolean success = requestQueue.offer(dispatcherPoisonPill, 100, TimeUnit.MILLISECONDS);
            
            if (success) {
                System.out.println("Диспетчер получил команду остановки (poison pill отправлен в очередь)");
            } else {
                System.err.println("Диспетчер: не удалось отправить poison pill (очередь переполнена или заблокирована)");
                // Если не удалось отправить poison pill, прерываем поток
                Thread.currentThread().interrupt();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Диспетчер: прервано при отправке poison pill");
        }
    }

    @Override
    public void onRideCompleted(TaxiWorker taxi, RideRequest ride, 
                               double distance, double fare, long waitTimeMillis) {
        // Рассчитываем время поездки
        long rideTimeMillis = System.currentTimeMillis() - 
                            (ride.getCreatedAtMillis() + waitTimeMillis);
        
        // Рассчитываем стоимость поездки, если не передана
        if (fare <= 0 && taxi.getType() != null) {
            fare = FareCalculator.calculateFare(taxi.getType(), distance);
        }
        
        if (statisticsCollector != null) {
            statisticsCollector.recordCompletedRide(
                taxi.getId(),
                taxi.getType(),
                distance,
                fare,
                rideTimeMillis,
                waitTimeMillis
            );
        }
        
        System.out.printf("Диспетчер: такси %d завершило поездку #%d, " +
                         "расстояние: %.2f, стоимость: %.2f, " +
                         "ожидание: %d мс, время поездки: %d мс%n",
                         taxi.getId(), ride.getId(), distance, fare,
                         waitTimeMillis, rideTimeMillis);
    }
    
    // =============== Геттеры ===================
    
    public List<TaxiWorker> getTaxis() {
        return taxis;
    }
    
    public DispatchStrategy getStrategy() {
        return strategy;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public int getTotalAssignedRides() {
        return totalAssignedRides;
    }
    
    public int getFailedAssignments() {
        return failedAssignments;
    }
}
