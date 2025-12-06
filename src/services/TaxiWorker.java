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
    
    // Статистические поля (пока оставим простыми)
    private int completedRides = 0;
    private double totalDistance = 0.0;
    private double totalRevenue = 0.0;
    
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
        // TODO: Добавить синхронизацию при необходимости
        try {
            personalQueue.put(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Основной метод потока такси
     */
    @Override
    public void run() {
        // TODO: Реализовать логику работы такси (будет в шаге 5)
        System.out.println("Такси " + id + " (" + type + ") запущено. Текущая позиция: " + currentLocation);
    }
    
    // Геттеры для доступа к состоянию такси
    
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
    
    // Сеттеры для изменения состояния (пока без синхронизации)
    
    public void setCurrentLocation(Point location) {
        this.currentLocation = location;
    }
    
    public void setStatus(TaxiStatus status) {
        this.status = status;
    }
}