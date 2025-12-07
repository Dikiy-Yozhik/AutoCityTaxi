package util;

import models.TaxiType;


public class FareCalculator {
    
    private FareCalculator() {}
    
    public static double calculateFare(TaxiType taxiType, double distance) {
        if (taxiType == null) {
            throw new IllegalArgumentException("TaxiType не может быть null");
        }
        // Стоимость = базовая стоимость + (расстояние * стоимость за км)
        return taxiType.getBaseFare() + (distance * taxiType.getPerKmRate());
    }
    

    public static double calculateFareWithMinimum(TaxiType taxiType, double distance, double minimumFare) {
        double fare = calculateFare(taxiType, distance);
        return Math.max(fare, minimumFare);
    }
}