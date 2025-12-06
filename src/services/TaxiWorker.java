package ru.mystudent.taxi.service;

import ru.mystudent.taxi.model.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Класс, представляющий такси как отдельный поток выполнения
 */
public class TaxiWorker implements Runnable {
    
    // Поля класса
    private final long id;
    private final TaxiType type;
    private Point currentLocation;
    private TaxiStatus status;
    private final BlockingQueue<RideRequest> personalQueue;
    private DispatcherCallback dispatcherCallback;
    
    // Статистические поля
    private int completedRides = 0;
    private double totalDistance = 0.0;
    private double totalRevenue = 0.0;
    
    // Для graceful shutdown
    private static final RideRequest POISON_PILL = createPoisonPill();
    private volatile boolean running = true;
    
    /**
     * Конструктор такси
     */
    public TaxiWorker(long id, TaxiType type, Point initialLocation) {
        this.id = id;
        this.type = type;
        this.currentLocation = initialLocation;
        this.status = TaxiStatus.IDLE;
        this.personalQueue = new LinkedBlockingQueue<>();
    }
    
    /**
     * Устанавливает обратный вызов к диспетчеру
     */
    public void setDispatcherCallback(DispatcherCallback callback) {
        this.dispatcherCallback = callback;
    }
    
    /**
     * Назначает заказ этому такси
     */
    public void assignRequest(RideRequest request) {
        try {
            personalQueue.put(request);
            System.out.println("Такси " + id + " получило заказ #" + request.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Такси " + id + " было прервано при получении заказа");
        }
    }
    
    /**
     * Основной метод потока такси
     */
    @Override
    public void run() {
        System.out.println("Такси " + id + " (" + type + ") запущено. Текущая позиция: " + currentLocation);
        
        try {
            while (running) {
                // Ждем новый заказ из очереди
                RideRequest request = personalQueue.take();
                
                // Проверяем, не poison pill ли это
                if (request == POISON_PILL) {
                    System.out.println("Такси " + id + " получило команду на остановку");
                    break;
                }
                
                // Обрабатываем заказ
                processRide(request);
            }
        } catch (InterruptedException e) {
            System.err.println("Такси " + id + " было прервано");
            Thread.currentThread().interrupt();
        }
        
        System.out.println("Такси " + id + " остановлено");
    }
    
    /**
     * Обрабатывает одну поездку
     */
    private void processRide(RideRequest request) {
        try {
            // 1. Едем к клиенту
            System.out.println("Такси " + id + " едет к клиенту #" + request.getId() + 
                             " из " + currentLocation + " в " + request.getPickup());
            
            double distanceToPickup = currentLocation.distanceTo(request.getPickup());
            setStatus(TaxiStatus.TO_PICKUP);
            
            // Имитируем поездку к клиенту (упрощенно - просто sleep)
            long travelTimeToPickup = calculateTravelTime(distanceToPickup);
            Thread.sleep(travelTimeToPickup);
            
            // 2. Клиент сел в такси
            System.out.println("Такси " + id + " забрало клиента #" + request.getId());
            currentLocation = request.getPickup();
            setStatus(TaxiStatus.WITH_PASSENGER);
            
            // 3. Едем к точке назначения
            System.out.println("Такси " + id + " везет клиента #" + request.getId() + 
                             " из " + currentLocation + " в " + request.getDropoff());
            
            double rideDistance = request.getPickup().distanceTo(request.getDropoff());
            long rideTime = calculateTravelTime(rideDistance);
            Thread.sleep(rideTime);
            
            // 4. Завершаем поездку
            System.out.println("Такси " + id + " доставило клиента #" + request.getId());
            currentLocation = request.getDropoff();
            setStatus(TaxiStatus.IDLE);
            
            // Обновляем статистику
            completedRides++;
            totalDistance += distanceToPickup + rideDistance;
            
            // Уведомляем диспетчер (если callback установлен)
            if (dispatcherCallback != null) {
                // Пока используем заглушки для параметров (будет реализовано в шаге 8)
                dispatcherCallback.onRideCompleted(this, request, 
                    distanceToPickup + rideDistance, 0.0, 0);
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Такси " + id + " было прервано во время поездки #" + request.getId());
        }
    }
    
    /**
     * Рассчитывает время поездки на основе расстояния
     * (упрощенно - 1 секунда на 10 единиц расстояния)
     */
    private long calculateTravelTime(double distance) {
        // В реальной реализации используем config.taxiSpeed
        return (long)(distance * 100); // 100 мс на единицу расстояния для быстрой симуляции
    }
    
    /**
     * Останавливает работу такси
     */
    public void stop() {
        this.running = false;
        // Отправляем poison pill для выхода из BlockingQueue.take()
        try {
            personalQueue.put(POISON_PILL);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Создает poison pill для graceful shutdown
     */
    private static RideRequest createPoisonPill() {
        return new RideRequest(-1, 
            new Point(0, 0), 
            new Point(0, 0), 
            System.currentTimeMillis(), 
            null); // null для TaxiType
    }
    
    // Геттеры и сеттеры (остаются без изменений)
    
    public long getId() {
        return id;
    }
    
    public TaxiType getType() {
        return type;
    }
    
    public Point getCurrentLocation() {
        return currentLocation;
    }
    
    public TaxiStatus getStatus() {
        return status;
    }
    
    public int getCompletedRides() {
        return completedRides;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public void setCurrentLocation(Point location) {
        this.currentLocation = location;
    }
    
    public void setStatus(TaxiStatus status) {
        this.status = status;
        System.out.println("Такси " + id + " сменило статус на: " + status);
    }
    
    public boolean isRunning() {
        return running;
    }
}