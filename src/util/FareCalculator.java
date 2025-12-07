package util;

import models.TaxiType;

/**
 * Калькулятор стоимости поездки
 */
public class FareCalculator {
    
    private FareCalculator() {
        // Утилитарный класс, не должен быть инстанциирован
    }
    
    /**
     * Рассчитывает стоимость поездки на основе типа такси и расстояния
     */
    public static double calculateFare(TaxiType taxiType, double distance) {
        if (taxiType == null) {
            throw new IllegalArgumentException("TaxiType не может быть null");
        }
        
        // Стоимость = базовая стоимость + (расстояние * стоимость за км)
        return taxiType.getBaseFare() + (distance * taxiType.getPerKmRate());
    }
    
    /**
     * Рассчитывает стоимость поездки с учетом минимальной стоимости
     */
    public static double calculateFareWithMinimum(TaxiType taxiType, double distance, double minimumFare) {
        double fare = calculateFare(taxiType, distance);
        return Math.max(fare, minimumFare);
    }
}