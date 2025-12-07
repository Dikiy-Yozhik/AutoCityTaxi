package models;


public enum TaxiType {
    ECONOMY(50.0, 10.0),     // базовая стоимость 50 руб, 10 руб/км
    COMFORT(100.0, 15.0),    // базовая стоимость 100 руб, 15 руб/км
    BUSINESS(200.0, 25.0);   // базовая стоимость 200 руб, 25 руб/км

    private final double baseFare;    
    private final double perKmRate; 

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

    public double calculateFare(double distance) {
        return baseFare + (perKmRate * distance);
    }

    @Override
    public String toString() {
        return name() + " (база: " + baseFare + " руб, за км: " + perKmRate + " руб)";
    }
}
