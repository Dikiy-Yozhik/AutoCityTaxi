package ru.mystudent.taxi.stats;

import ru.mystudent.taxi.model.TaxiType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicDouble;

/**
 * Сборщик статистики по всей симуляции
 */
public class StatisticsCollector {
    
    // Агрегированная статистика (используем атомарные типы для потокобезопасности)
    private final AtomicInteger totalCompletedRides = new AtomicInteger(0);
    private final AtomicLong totalWaitTimeMillis = new AtomicLong(0L);
    private final AtomicLong totalRideTimeMillis = new AtomicLong(0L);
    private final AtomicDouble totalDistance = new AtomicDouble(0.0);
    private final AtomicDouble totalRevenue = new AtomicDouble(0.0);
    
    // Статистика по типам такси
    private final Map<TaxiType, TaxiTypeStats> statsByTaxiType = new ConcurrentHashMap<>();
    
    // Статистика по каждому такси (ключ - ID такси)
    private final Map<Long, TaxiStats> taxiStatistics = new ConcurrentHashMap<>();
    
    /**
     * Записывает данные о завершенной поездке
     */
    public void recordCompletedRide(long taxiId, TaxiType taxiType,
                                   double distance, double revenue,
                                   long rideTimeMillis, long waitTimeMillis) {
        // Обновляем общую статистику
        totalCompletedRides.incrementAndGet();
        totalDistance.addAndGet(distance);
        totalRevenue.addAndGet(revenue);
        totalRideTimeMillis.addAndGet(rideTimeMillis);
        totalWaitTimeMillis.addAndGet(waitTimeMillis);
        
        // Обновляем статистику по типу такси
        statsByTaxiType.computeIfAbsent(taxiType, k -> new TaxiTypeStats())
                      .addRide(distance, revenue, rideTimeMillis, waitTimeMillis);
        
        // Обновляем статистику по конкретному такси
        taxiStatistics.computeIfAbsent(taxiId, k -> new TaxiStats())
                     .addRide(distance, revenue, rideTimeMillis, waitTimeMillis);
    }
    
    // Методы для получения статистики
    
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
    
    /**
     * Получает среднее время ожидания в секундах
     */
    public double getAverageWaitTimeSeconds() {
        int rides = totalCompletedRides.get();
        return rides > 0 ? totalWaitTimeMillis.get() / (rides * 1000.0) : 0.0;
    }
    
    /**
     * Получает среднее время поездки в секундах
     */
    public double getAverageRideTimeSeconds() {
        int rides = totalCompletedRides.get();
        return rides > 0 ? totalRideTimeMillis.get() / (rides * 1000.0) : 0.0;
    }
    
    /**
     * Получает среднее расстояние поездки
     */
    public double getAverageDistance() {
        int rides = totalCompletedRides.get();
        return rides > 0 ? totalDistance.get() / rides : 0.0;
    }
    
    /**
     * Получает среднюю стоимость поездки
     */
    public double getAverageFare() {
        int rides = totalCompletedRides.get();
        return rides > 0 ? totalRevenue.get() / rides : 0.0;
    }
    
    /**
     * Получает статистику по конкретному такси
     */
    public TaxiStats getTaxiStats(long taxiId) {
        return taxiStatistics.get(taxiId);
    }
    
    /**
     * Получает статистику по типу такси
     */
    public TaxiTypeStats getStatsByTaxiType(TaxiType type) {
        return statsByTaxiType.get(type);
    }
    
    /**
     * Получает все статистики по такси
     */
    public Map<Long, TaxiStats> getAllTaxiStats() {
        return taxiStatistics;
    }
    
    /**
     * Получает все статистики по типам такси
     */
    public Map<TaxiType, TaxiTypeStats> getAllTaxiTypeStats() {
        return statsByTaxiType;
    }
    
    /**
     * Выводит полный отчет по статистике
     */
    public void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("ОТЧЕТ ПО СТАТИСТИКЕ СИМУЛЯЦИИ");
        System.out.println("=".repeat(60));
        
        // Общая статистика
        System.out.println("\nОБЩАЯ СТАТИСТИКА:");
        System.out.printf("Всего выполнено поездок: %d%n", getTotalCompletedRides());
        System.out.printf("Общий пробег: %.2f%n", getTotalDistance());
        System.out.printf("Общая выручка: %.2f%n", getTotalRevenue());
        System.out.printf("Среднее время ожидания: %.1f сек%n", getAverageWaitTimeSeconds());
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
                TaxiStats stats = entry.getValue();
                System.out.printf("%-3d | %-9s | %-7d | %-8.2f | %-8.2f | %-11.1fс | %-10.1fс%n",
                    entry.getKey(),
                    getTaxiTypeForId(entry.getKey()),
                    stats.getCompletedRides(),
                    stats.getTotalDistance(),
                    stats.getTotalRevenue(),
                    stats.getAverageWaitTimeSeconds(),
                    stats.getAverageRideTimeSeconds());
            });
        
        System.out.println("=".repeat(60));
    }
    
    /**
     * Вспомогательный метод для получения типа такси по ID
     * (В реальной реализации нужно передавать информацию о типе такси)
     */
    private String getTaxiTypeForId(long taxiId) {
        // Временная заглушка - в реальном коде нужно хранить информацию о типах такси
        return "UNKNOWN";
    }
}