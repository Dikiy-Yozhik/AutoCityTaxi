package ru.mystudent.taxi.infra;

import ru.mystudent.taxi.model.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Генератор заявок на поездки
 */
public class RequestGenerator implements Runnable {
    
    // Поля класса
    private final BlockingQueue<RideRequest> requestQueue;
    private final SimulationConfig config;
    private final AtomicLong requestIdCounter;
    private volatile boolean running = true;
    
    /**
     * Конструктор генератора
     */
    public RequestGenerator(BlockingQueue<RideRequest> requestQueue, SimulationConfig config) {
        this.requestQueue = requestQueue;
        this.config = config;
        this.requestIdCounter = new AtomicLong(1);
    }
    
    /**
     * Основной метод потока генератора
     */
    @Override
    public void run() {
        System.out.println("Генератор запросов запущен. Интервал: " + 
                          config.getMeanRequestIntervalMillis() + " мс");
        
        // TODO: Реализовать генерацию запросов (будет в шаге 5)
        while (running) {
            try {
                // Временная заглушка
                Thread.sleep(config.getMeanRequestIntervalMillis());
                System.out.println("Генератор: сгенерирован запрос #" + requestIdCounter.getAndIncrement());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        System.out.println("Генератор запросов остановлен.");
    }
    
    /**
     * Остановка генератора
     */
    public void stop() {
        this.running = false;
    }
    
    /**
     * Проверка, работает ли генератор
     */
    public boolean isRunning() {
        return running;
    }
}