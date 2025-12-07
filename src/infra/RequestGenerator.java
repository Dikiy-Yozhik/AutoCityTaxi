package infra;

import models.*;

import java.util.concurrent.BlockingQueue;
import java.util.Random;

/**
 * Генератор заявок на поездки
 */
public class RequestGenerator implements Runnable {
    
    // Поля класса
    private final BlockingQueue<RideRequest> requestQueue;
    private final SimulationConfig config;
    private final Random random;
    private volatile boolean running = true;
    
    // Poison pill для остановки диспетчера
    public static final RideRequest DISPATCHER_POISON_PILL = RideRequest.createPoisonPill();
    
    /**
     * Конструктор генератора
     */
    public RequestGenerator(BlockingQueue<RideRequest> requestQueue, SimulationConfig config) {
        this.requestQueue = requestQueue;
        this.config = config;
        this.random = new Random();
    }
    
    /**
     * Основной метод потока генератора
     */
    @Override
    public void run() {
        System.out.println("Генератор запросов запущен. Интервал: " + 
                          config.getMeanRequestIntervalMillis() + " мс");
        
        try {
            while (running) {
                // Генерируем новый заказ
                RideRequest request = generateRequest();
                
                // Помещаем в очередь
                requestQueue.put(request);
                System.out.println("Сгенерирован заказ #" + request.getId() + 
                                 " от " + request.getPickupLocation() + 
                                 " до " + request.getDropoffLocation() +
                                 " (тип: " + request.getRequestedType() + ")");
                
                // Ждем перед генерацией следующего заказа
                long interval = getNextInterval();
                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            System.err.println("Генератор запросов был прерван");
            Thread.currentThread().interrupt();
        } finally {
            // При завершении отправляем poison pill диспетчеру
            sendPoisonPill();
            System.out.println("Генератор запросов остановлен.");
        }
    }
    
    /**
     * Генерирует случайный заказ на поездку
     */
    private RideRequest generateRequest() {
        // Генерируем случайные точки в пределах города
        Point pickup = generateRandomPoint();
        Point dropoff = generateRandomPoint();
        
        // Убедимся, что точки не совпадают
        while (pickup.distanceTo(dropoff) < 1.0) {
            dropoff = generateRandomPoint();
        }
        
        // Выбираем случайный тип такси
        TaxiType[] allTypes = TaxiType.values();
        TaxiType requestedType = allTypes[random.nextInt(allTypes.length)];
        
        // Создаем заказ (ID генерируется автоматически в конструкторе)
        return new RideRequest(pickup, dropoff, requestedType);
    }
    
    /**
     * Генерирует случайную точку в пределах города
     */
    private Point generateRandomPoint() {
        double x = config.getCityMinX() + 
                  random.nextDouble() * (config.getCityMaxX() - config.getCityMinX());
        double y = config.getCityMinY() + 
                  random.nextDouble() * (config.getCityMaxY() - config.getCityMinY());
        return new Point(x, y);
    }
    
    /**
     * Получает следующий интервал с некоторой случайностью
     */
    private long getNextInterval() {
        // Добавляем некоторую случайность к интервалу (±50%)
        return (long)(config.getMeanRequestIntervalMillis() * 
                    (0.5 + random.nextDouble()));
    }
    
    /**
     * Отправляет poison pill в очередь для остановки диспетчера
     */
    private void sendPoisonPill() {
        try {
            requestQueue.put(DISPATCHER_POISON_PILL);
            System.out.println("Генератор отправил poison pill диспетчеру");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
