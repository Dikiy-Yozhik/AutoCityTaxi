package models;

/**
 * Типы такси с соответствующими тарифами.
 */
public enum TaxiType {
    ECONOMY(50.0, 10.0),     // базовая стоимость 50 руб, 10 руб/км
    COMFORT(100.0, 15.0),    // базовая стоимость 100 руб, 15 руб/км
    BUSINESS(200.0, 25.0);   // базовая стоимость 200 руб, 25 руб/км

    private final double baseFare;    // Базовая стоимость посадки
    private final double perKmRate;   // Стоимость за километр

    TaxiType(double baseFare, double perKmRate) {
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getPerKmRate() {
        return perKmRate;
    }

    /**
     * Рассчитывает стоимость поездки для данного типа такси.
     * 
     * @param distance расстояние в километрах
     * @return общая стоимость поездки
     */
    public double calculateFare(double distance) {
        return baseFare + (perKmRate * distance);
    }

    @Override
    public String toString() {
        return name() + " (база: " + baseFare + " руб, за км: " + perKmRate + " руб)";
    }
}