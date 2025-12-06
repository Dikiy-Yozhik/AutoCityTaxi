package ru.mystudent.taxi.service;

import ru.mystudent.taxi.model.RideRequest;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Диспетчер, распределяющий заказы между такси
 */
public class Dispatcher implements Runnable, DispatcherCallback {
    
    // Поля класса
    private final BlockingQueue<RideRequest> requestQueue;
    private final List<TaxiWorker> taxis;
    private final DispatchStrategy strategy;
    private volatile boolean running = true;
    
    /**
     * Конструктор диспетчера
     */
    public Dispatcher(BlockingQueue<RideRequest> requestQueue, 
                     List<TaxiWorker> taxis, 
                     DispatchStrategy strategy) {
        this.requestQueue = requestQueue;
        this.taxis = taxis;
        this.strategy = strategy;
    }
    
    /**
     * Основной метод потока диспетчера
     */
    @Override
    public void run() {
        System.out.println("Диспетчер запущен. Стратегия: " + strategy.getName());
        
        // TODO: Реализовать логику распределения заказов (будет в шаге 6)
        while (running) {
            try {
                // Временная заглушка
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("Диспетчер остановлен.");
    }
    
    /**
     * Остановка работы диспетчера
     */
    public void stop() {
        this.running = false;
    }
    
    /**
     * Обратный вызов при завершении поездки
     */
    @Override
    public void onRideCompleted(TaxiWorker taxi, RideRequest ride, 
                               double distance, double fare, long waitTimeMillis) {
        // TODO: Реализовать обработку завершения поездки (будет в шаге 8)
        System.out.println("Такси " + taxi.getId() + " завершило поездку #" + ride.getId());
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
}