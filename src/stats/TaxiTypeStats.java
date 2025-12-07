package stats;

/**
 * Статистика по типу такси
 */
public class TaxiTypeStats {
    
    private int completedRides;
    private double totalDistance;
    private double totalRevenue;
    private long totalRideTimeMillis;
    private long totalWaitTimeMillis;
    
    public TaxiTypeStats() {
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
    
    /**
     * Получает среднюю стоимость поездки
     */
    public synchronized double getAverageFare() {
        return completedRides > 0 ? totalRevenue / completedRides : 0.0;
    }
    
    /**
     * Получает среднее расстояние
     */
    public synchronized double getAverageDistance() {
        return completedRides > 0 ? totalDistance / completedRides : 0.0;
    }
    
    @Override
    public synchronized String toString() {
        return String.format(
            "Поездок: %d, Выручка: %.2f, Ср. чек: %.2f, Ср. расстояние: %.2f",
            completedRides, totalRevenue, getAverageFare(), getAverageDistance()
        );
    }
}