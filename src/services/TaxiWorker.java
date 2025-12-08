package services;

import models.*;
import util.FareCalculator;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class TaxiWorker implements Runnable {
    
    private final long id;
    private final TaxiType type;
    private final double taxiSpeed;
    private Point currentLocation;
    private TaxiStatus status;
    private final BlockingQueue<RideRequest> personalQueue;
    private DispatcherCallback dispatcherCallback;
    
    private int completedRides = 0;
    private double totalDistance = 0.0;
    private double totalRevenue = 0.0;
    
    private static final RideRequest POISON_PILL = createPoisonPill();
    private volatile boolean running = true;
    

    public TaxiWorker(long id, TaxiType type, Point initialLocation, double taxiSpeed) {
        this.id = id;
        this.type = type;
        this.taxiSpeed = taxiSpeed;
        this.currentLocation = initialLocation;
        this.status = TaxiStatus.IDLE;
        this.personalQueue = new LinkedBlockingQueue<>();
    }
    
    public void setDispatcherCallback(DispatcherCallback callback) {
        this.dispatcherCallback = callback;
    }
    
    public void assignRequest(RideRequest request) {
        try {
            personalQueue.put(request);
            System.out.println("Такси " + id + " получило заказ #" + request.getId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Такси " + id + " было прервано при получении заказа");
        }
    }
    

    @Override
    public void run() {
        System.out.println("Такси " + id + " (" + type + ") запущено. Текущая позиция: " + currentLocation);
        
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                // Ждем заказ с таймаутом для частой проверки прерывания
                RideRequest request = personalQueue.poll(200, TimeUnit.MILLISECONDS);
                
                if (request == null) {
                    // Таймаут - проверяем условия и продолжаем
                    continue;
                }
                
                // Проверяем, не poison pill ли это
                if (request == POISON_PILL) {
                    System.out.println("Такси " + id + " получило poison pill. Завершение работы...");
                    break;
                }
                
                // Обрабатываем заказ
                processRide(request);
            }
        } catch (InterruptedException e) {
            // Проверяем почему прервано
            if (!running) {
                System.out.println("Такси " + id + " корректно прервано по команде остановки");
            } else {
                System.err.println("Такси " + id + " было неожиданно прервано");
            }
            Thread.currentThread().interrupt();
        } finally {
            System.out.println("Такси " + id + " остановлено. Выполнено поездок: " + completedRides);
        }
    }
    

    private void processRide(RideRequest request) {
        try {
            // Рассчитываем время ожидания (от создания заказа до начала поездки)
            long waitTimeMillis = System.currentTimeMillis() - request.getCreatedAtMillis();
            
            // 1. Едем к клиенту
            System.out.println("Такси " + id + " едет к клиенту #" + request.getId() + 
                            " из " + currentLocation + " в " + request.getPickupLocation() +
                            " (ожидание: " + waitTimeMillis + " мс)");
            
            double distanceToPickup = currentLocation.distanceTo(request.getPickupLocation());
            setStatus(TaxiStatus.TO_PICKUP);
            
            // Имитируем поездку к клиенту с проверкой running
            long travelTimeToPickup = calculateTravelTime(distanceToPickup);
            if (!sleepWithInterruptCheck(travelTimeToPickup)) {
                System.out.println("Такси " + id + " прервано по пути к клиенту #" + request.getId());
                return;
            }
            
            // Проверяем running после сна
            if (!running) {
                System.out.println("Такси " + id + " получило команду остановки на пути к клиенту #" + request.getId());
                return;
            }
            
            // 2. Клиент сел в такси
            System.out.println("Такси " + id + " забрало клиента #" + request.getId());
            currentLocation = request.getPickupLocation();
            setStatus(TaxiStatus.WITH_PASSENGER);
            
            // 3. Едем к точке назначения с проверкой running
            System.out.println("Такси " + id + " везет клиента #" + request.getId() + 
                            " из " + currentLocation + " в " + request.getDropoffLocation());
            
            double rideDistance = request.getPickupLocation().distanceTo(request.getDropoffLocation());
            long rideTime = calculateTravelTime(rideDistance);
            if (!sleepWithInterruptCheck(rideTime)) {
                System.out.println("Такси " + id + " прервано во время поездки с клиентом #" + request.getId());
                return;
            }
            
            // Проверяем running после сна
            if (!running) {
                System.out.println("Такси " + id + " получило команду остановки во время поездки с клиентом #" + request.getId());
                return;
            }
            
            // 4. Завершаем поездку 
            System.out.println("Такси " + id + " доставило клиента #" + request.getId());
            currentLocation = request.getDropoffLocation();
            setStatus(TaxiStatus.IDLE);
            
            // Обновляем статистику
            completedRides++;
            totalDistance += distanceToPickup + rideDistance;
            
            // Рассчитываем стоимость поездки
            double totalDistance = distanceToPickup + rideDistance;
            double fare = FareCalculator.calculateFare(type, totalDistance);
            totalRevenue += fare;
            
            // Уведомляем диспетчер
            if (dispatcherCallback != null) {
                dispatcherCallback.onRideCompleted(this, request, 
                    totalDistance, fare, waitTimeMillis);
            }
            
        } catch (InterruptedException e) {
            // Восстанавливаем статус прерывания
            Thread.currentThread().interrupt();
            
            if (!running) {
                System.out.println("Такси " + id + " корректно прервано во время поездки #" + request.getId());
            } else {
                System.err.println("Такси " + id + " было неожиданно прервано во время поездки #" + request.getId());
            }
        }
    }


    private boolean sleepWithInterruptCheck(long millis) throws InterruptedException {
        long endTime = System.currentTimeMillis() + millis;
        long remaining = millis;
        
        while (remaining > 0 && running && !Thread.currentThread().isInterrupted()) {
            // Спим маленькими порциями для частой проверки
            long sleepTime = Math.min(remaining, 50L); 
            Thread.sleep(sleepTime);
            
            // Обновляем оставшееся время
            remaining = endTime - System.currentTimeMillis();
        }
        
        // Если sleep прерван из-за остановки, выбрасываем исключение
        if (!running || Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        
        return remaining <= 0;
    }
    

    private long calculateTravelTime(double distance) {
        double timeSeconds = distance / taxiSpeed;
        return (long)(timeSeconds * 1000); 
    }
    
    public void stop() {
        // Устанавливаем флаг остановки
        this.running = false;
        
        Thread.currentThread().interrupt();
        
        // Пытаемся отправить poison pill с таймаутом
        try {
            boolean success = personalQueue.offer(POISON_PILL, 50, TimeUnit.MILLISECONDS);
            
            if (success) {
                System.out.println("Такси " + id + " получило команду остановки (poison pill отправлен)");
            } else {
                System.out.println("Такси " + id + " получило команду остановки (очередь переполнена, использован interrupt)");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Такси " + id + ": прервано при отправке poison pill (используем interrupt)");
        }
    }
        
    private static RideRequest createPoisonPill() {
        return RideRequest.createPoisonPill();
    }
    
    // ================= Геттеры =================
    
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

    // ============== Сеттеры ===============
    
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
