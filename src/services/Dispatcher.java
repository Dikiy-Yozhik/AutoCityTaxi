package ru.mystudent.taxi.service;

import ru.mystudent.taxi.model.RideRequest;
import ru.mystudent.taxi.model.TaxiStatus;
import ru.mystudent.taxi.stats.StatisticsCollector;
import ru.mystudent.taxi.util.FareCalculator;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Диспетчер, распределяющий заказы между такси
 */
public class Dispatcher implements Runnable, DispatcherCallback {
    
    // Поля класса
    private final BlockingQueue<RideRequest> requestQueue;
    private final List<TaxiWorker> taxis;
    private final DispatchStrategy strategy;
    private final ReentrantLock selectionLock;
    private final StatisticsCollector statisticsCollector; // Добавлено
    private volatile boolean running = true;
    
    private int totalAssignedRides = 0;
    private int failedAssignments = 0;
    
    /**
     * Конструктор диспетчера
     */
    public Dispatcher(BlockingQueue<RideRequest> requestQueue, 
                     List<TaxiWorker> taxis, 
                     DispatchStrategy strategy,
                     StatisticsCollector statisticsCollector) { // Добавлен параметр
        this.requestQueue = requestQueue;
        this.taxis = taxis;
        this.strategy = strategy;
        this.selectionLock = new ReentrantLock();
        this.statisticsCollector = statisticsCollector;
    }
    
    /**
     * Основной метод потока диспетчера
     */
    @Override
    public void run() {
        System.out.println("Диспетчер запущен. Стратегия: " + strategy.getName());
        System.out.println("Доступно такси: " + taxis.size());
        
        while (running) {
            try {
                // 1. Забираем заказ из общей очереди (блокирующая операция)
                RideRequest request = requestQueue.take();
                
                // Проверяем, не poison pill ли это (если используем для остановки)
                if (isPoisonPill(request)) {
                    System.out.println("Диспетчер получил команду на остановку");
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
        
        System.out.println("Диспетчер остановлен. Назначено поездок: " + 
                         totalAssignedRides + ", не удалось назначить: " + failedAssignments);
    }
    
    /**
     * Выбирает такси для заказа с использованием стратегии
     */
    private TaxiWorker selectTaxiForRequest(RideRequest request) {
        selectionLock.lock();
        try {
            return strategy.selectTaxi(taxis, request);
        } finally {
            selectionLock.unlock();
        }
    }
    
    /**
     * Проверяет, является ли запрос poison pill'ом
     */
    private boolean isPoisonPill(RideRequest request) {
        return request.getId() == -1; // ID poison pill из TaxiWorker
    }
    
    /**
     * Остановка работы диспетчера
     */
    public void stop() {
        this.running = false;
        // Отправляем poison pill в очередь для выхода из take()
        try {
            RideRequest poisonPill = new RideRequest(-1, null, null, 0, null);
            requestQueue.put(poisonPill);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Обратный вызов при завершении поездки
     */
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
    
    // Геттеры
    
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