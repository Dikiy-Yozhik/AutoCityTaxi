package infra;

import models.*;

import java.util.concurrent.BlockingQueue;
import java.util.Random;


public class RequestGenerator implements Runnable {
    private final BlockingQueue<RideRequest> requestQueue;
    private final SimulationConfig config;
    private final Random random;
    private volatile boolean running = true;
    
    public static final RideRequest DISPATCHER_POISON_PILL = RideRequest.createPoisonPill();
    
    public RequestGenerator(BlockingQueue<RideRequest> requestQueue, SimulationConfig config) {
        this.requestQueue = requestQueue;
        this.config = config;
        this.random = new Random();
    }
    
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
            sendPoisonPill();
            System.out.println("Генератор запросов остановлен.");
        }
    }
    
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
        
        // Создаем заказ 
        return new RideRequest(pickup, dropoff, requestedType);
    }
    
    private Point generateRandomPoint() {
        double x = config.getCityMinX() + 
                  random.nextDouble() * (config.getCityMaxX() - config.getCityMinX());
        double y = config.getCityMinY() + 
                  random.nextDouble() * (config.getCityMaxY() - config.getCityMinY());
        return new Point(x, y);
    }
    
    private long getNextInterval() {
        return (long)(config.getMeanRequestIntervalMillis() * 
                    (0.5 + random.nextDouble()));
    }
    
    private void sendPoisonPill() {
        try {
            requestQueue.put(DISPATCHER_POISON_PILL);
            System.out.println("Генератор отправил poison pill диспетчеру");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    public void stop() {
        this.running = false;
        Thread.currentThread().interrupt();
    }

    public boolean isRunning() {
        return running;
    }
}
