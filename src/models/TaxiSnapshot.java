package models;


public class TaxiSnapshot {
    private final long taxiId;
    private final Point currentLocation;
    private final TaxiStatus status;
    private final TaxiType type;
    private final int completedRides;
    private final double totalDistance;
    private final double totalRevenue;

    public TaxiSnapshot(long taxiId, Point currentLocation, TaxiStatus status, 
                       TaxiType type, int completedRides, double totalDistance, 
                       double totalRevenue) {
        this.taxiId = taxiId;
        this.currentLocation = currentLocation;
        this.status = status;
        this.type = type;
        this.completedRides = completedRides;
        this.totalDistance = totalDistance;
        this.totalRevenue = totalRevenue;
    }

    // =============== Геттеры ================
    public long getTaxiId() {
        return taxiId;
    }

    public Point getCurrentLocation() {
        return currentLocation;
    }

    public TaxiStatus getStatus() {
        return status;
    }

    public TaxiType getType() {
        return type;
    }

    public int getCompletedRides() {
        return completedRides;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    @Override
    public String toString() {
        return String.format("Такси #%d [%s, %s, позиция: %s, поездок: %d, пробег: %.2f км, выручка: %.2f руб]", 
            taxiId, type, status, currentLocation, completedRides, totalDistance, totalRevenue);
    }
}
