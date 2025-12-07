package services;

import models.RideRequest;
import models.TaxiStatus;
import stats.StatisticsCollector;
import util.FareCalculator;

import java.util.List;
import java.util.concurrent.BlockingQueue;
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
    }

    @Override
    public void run() {
        System.out.println("Диспетчер запущен. Стратегия: " + strategy.getName());
        System.out.println("Доступно такси: " + taxis.size());
        
        while (running) {
            try {
                // 1. Забираем заказ из общей очереди (блокирующая операция)
                RideRequest request = requestQueue.take();
                
                // Проверяем, не poison pill ли это
                if (isPoisonPill(request)) {
                    System.out.println("Диспетчер получил poison pill. Завершение работы...");
                    break;
                }
                
                System.out.println("Диспетчер обрабатывает заказ #" + request.getId() + 
                                 " (тип: " + request.getRequestedType() + ")");
                
                // 2. Выбираем такси через стратегию
                TaxiWorker selectedTaxi = selectTaxiForRequest(request);
                
                if (selectedTaxi != null) {
                    // 3. Помечаем такси занятым
                    selectedTaxi.setStatus(TaxiStatus.TO_PICKUP);
                    
                    // 4. Кладем заказ в личную очередь такси
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
                System.err.println("Диспетчер был прерван");
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        // Останавливаем такси перед завершением
        stopAllTaxis();
        
        System.out.println("Диспетчер остановлен. Назначено поездок: " + 
                         totalAssignedRides + ", не удалось назначить: " + failedAssignments);
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
        return request.getId() == -1; // ID poison pill
    }
    
    private void stopAllTaxis() {
        System.out.println("Диспетчер останавливает все такси...");
        for (TaxiWorker taxi : taxis) {
            taxi.stop();
        }
    }
    
    public void stop() {
        this.running = false;
        Thread.currentThread().interrupt();
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
        
        // Записываем статистику
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
