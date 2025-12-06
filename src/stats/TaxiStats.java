package ru.mystudent.taxi.stats;

/**
 * Статистика по одному такси
 */
public class TaxiStats {
    
    private int completedRides;
    private double totalDistance;
    private double totalRevenue;
    private long totalRideTimeMillis;
    private long totalWaitTimeMillis;
    
    public TaxiStats() {
        this.completedRides = 0;
        this.totalDistance = 0.0;
        this.totalRevenue = 0.0;
        this.totalRideTimeMillis = 0L;
        this.totalWaitTimeMillis = 0L;
    }
    
    /**
     * Добавляет данные о завершенной поездке
     */
    public synchronized void addRide(double distance, double revenue, 
                                   long rideTimeMillis, long waitTimeMillis) {
        this.completedRides++;
        this.totalDistance += distance;
        this.totalRevenue += revenue;
        this.totalRideTimeMillis += rideTimeMillis;
        this.totalWaitTimeMillis += waitTimeMillis;
    }
    
    // Геттеры
    
    public synchronized int getCompletedRides() {
        return completedRides;
    }
    
    public synchronized double getTotalDistance() {
        return totalDistance;
    }
    
    public synchronized double getTotalRevenue() {
        return totalRevenue;
    }
    
    public synchronized long getTotalRideTimeMillis() {
        return totalRideTimeMillis;
    }
    
    public synchronized long getTotalWaitTimeMillis() {
        return totalWaitTimeMillis;
    }
    
    /**
     * Получает среднее время поездки
     */
    public synchronized double getAverageRideTimeSeconds() {
        return completedRides > 0 ? totalRideTimeMillis / (completedRides * 1000.0) : 0.0;
    }
    
    /**
     * Получает среднее время ожидания
     */
    public synchronized double getAverageWaitTimeSeconds() {
        return completedRides > 0 ? totalWaitTimeMillis / (completedRides * 1000.0) : 0.0;
    }
    
    @Override
    public synchronized String toString() {
        return String.format(
            "Поездок: %d, Пробег: %.2f, Выручка: %.2f, Ср. время поездки: %.1fс, Ср. ожидание: %.1fс",
            completedRides, totalDistance, totalRevenue,
            getAverageRideTimeSeconds(), getAverageWaitTimeSeconds()
        );
    }
}