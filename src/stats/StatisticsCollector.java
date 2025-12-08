package stats;

import models.TaxiType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


public class StatisticsCollector {
    
    // Агрегированная статистика
    private final AtomicInteger totalCompletedRides = new AtomicInteger(0);
    private final AtomicLong totalWaitTimeMillis = new AtomicLong(0L);
    private final AtomicLong totalRideTimeMillis = new AtomicLong(0L);
    private final AtomicReference<Double> totalDistance = new AtomicReference<>(0.0);
    private final AtomicReference<Double> totalRevenue = new AtomicReference<>(0.0);
    private final Map<Long, TaxiType> taxiIdToTypeMap = new ConcurrentHashMap<>();
    
    // Статистика по типам такси
    private final Map<TaxiType, TaxiTypeStats> statsByTaxiType = new ConcurrentHashMap<>();
    
    // Статистика по каждому такси (ключ - ID такси)
    private final Map<Long, TaxiStats> taxiStatistics = new ConcurrentHashMap<>();
    

    public void recordCompletedRide(long taxiId, TaxiType taxiType,
                                   double distance, double revenue,
                                   long rideTimeMillis, long waitTimeMillis) {                           
        // Обновляем общую статистику
        totalCompletedRides.incrementAndGet();
        totalDistance.updateAndGet(current -> current + distance);
        totalRevenue.updateAndGet(current -> current + revenue);
        totalRideTimeMillis.addAndGet(rideTimeMillis);
        totalWaitTimeMillis.addAndGet(waitTimeMillis);

        // Обновляем статистику по типу такси
        statsByTaxiType.computeIfAbsent(taxiType, k -> new TaxiTypeStats())
                      .addRide(distance, revenue, rideTimeMillis, waitTimeMillis);
        
        // Обновляем статистику по конкретному такси
        taxiStatistics.computeIfAbsent(taxiId, k -> new TaxiStats())
                     .addRide(distance, revenue, rideTimeMillis, waitTimeMillis);
        
        // Регистрируем тип такси (на случай, если registerTaxi не был вызван)
        taxiIdToTypeMap.putIfAbsent(taxiId, taxiType);
    }

    public void registerTaxi(long taxiId, TaxiType taxiType) {
        taxiIdToTypeMap.put(taxiId, taxiType);
    }
    
    // ================ Геттеры =============
    
    public int getTotalCompletedRides() {
        return totalCompletedRides.get();
    }
    
    public double getTotalDistance() {
        return totalDistance.get();
    }
    
    public double getTotalRevenue() {
        return totalRevenue.get();
    }
    
    public long getTotalRideTimeMillis() {
        return totalRideTimeMillis.get();
    }
    
    public long getTotalWaitTimeMillis() {
        return totalWaitTimeMillis.get();
    }
    
    public double getAverageWaitTimeSeconds() {
        long completed = totalCompletedRides.get();
        long totalWait = totalWaitTimeMillis.get();
        
        return completed > 0 ? (double) totalWait / completed / 1000.0 : 0.0;
    }
    
    public double getAverageRideTimeSeconds() {
        int rides = totalCompletedRides.get();
        return rides > 0 ? totalRideTimeMillis.get() / (rides * 1000.0) : 0.0;
    }

    public double getAverageDistance() {
        int rides = totalCompletedRides.get();
        return rides > 0 ? totalDistance.get() / rides : 0.0;
    }
    
    public double getAverageFare() {
        int rides = totalCompletedRides.get();
        return rides > 0 ? totalRevenue.get() / rides : 0.0;
    }
    
    public TaxiStats getTaxiStats(long taxiId) {
        return taxiStatistics.get(taxiId);
    }
    
    public TaxiTypeStats getStatsByTaxiType(TaxiType type) {
        return statsByTaxiType.get(type);
    }
    
    public Map<Long, TaxiStats> getAllTaxiStats() {
        return taxiStatistics;
    }
    
    public Map<TaxiType, TaxiTypeStats> getAllTaxiTypeStats() {
        return statsByTaxiType;
    }

    private String getTaxiTypeName(long taxiId) {
        TaxiType type = taxiIdToTypeMap.get(taxiId);
        if (type == null) {
            return "UNKNOWN";
        }
        return type.name(); 
    }


    public void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ОТЧЕТ ПО СТАТИСТИКЕ СИМУЛЯЦИИ");
        System.out.println("=".repeat(60));
        
        // Общая статистика
        System.out.println("\nОБЩАЯ СТАТИСТИКА:");
        System.out.printf("Всего выполнено поездок: %d%n", getTotalCompletedRides());
        System.out.printf("Общий пробег: %.2f%n", getTotalDistance());
        System.out.printf("Общая выручка: %.2f%n", getTotalRevenue());
        System.out.printf("Среднее время ожидания: %.3f сек%n", getAverageWaitTimeSeconds());
        System.out.printf("Среднее время поездки: %.1f сек%n", getAverageRideTimeSeconds());
        System.out.printf("Среднее расстояние: %.2f%n", getAverageDistance());
        System.out.printf("Средняя стоимость поездки: %.2f%n", getAverageFare());
        
        // Статистика по типам такси
        System.out.println("\nСТАТИСТИКА ПО ТИПАМ ТАКСИ:");
        for (Map.Entry<TaxiType, TaxiTypeStats> entry : statsByTaxiType.entrySet()) {
            System.out.printf("%-10s: %s%n", entry.getKey(), entry.getValue());
        }
        
        // Статистика по каждому такси
        System.out.println("\nСТАТИСТИКА ПО КАЖДОМУ ТАКСИ:");
        System.out.println("ID  | Тип       | Поездки | Пробег   | Выручка  | Ср. ожидание | Ср. поездка");
        System.out.println("----+-----------+---------+----------+----------+--------------+------------");
        
        taxiStatistics.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                long taxiId = entry.getKey();
                TaxiStats stats = entry.getValue();
                String typeName = getTaxiTypeName(taxiId);
                
                System.out.printf("%-3d | %-9s | %-7d | %-8.2f | %-8.2f | %-11.3fс | %-10.1fс%n",
                    taxiId,
                    typeName,
                    stats.getCompletedRides(),
                    stats.getTotalDistance(),
                    stats.getTotalRevenue(),
                    stats.getAverageWaitTimeSeconds(),
                    stats.getAverageRideTimeSeconds());
            });
        
        System.out.println("=".repeat(60));
    }
}
