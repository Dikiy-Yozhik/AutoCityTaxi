package models;

import java.util.concurrent.atomic.AtomicLong;


public class RideRequest {
    private static final AtomicLong idGenerator = new AtomicLong(1);
    
    private final long id;
    private final Point pickupLocation;
    private final Point dropoffLocation;
    private final long createdAtMillis;
    private final TaxiType requestedType;
    private volatile RideStatus status;
    private Long assignedTaxiId; 
    
    public RideRequest(Point pickupLocation, Point dropoffLocation, TaxiType requestedType) {
        this.id = idGenerator.getAndIncrement();
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.createdAtMillis = System.currentTimeMillis();
        this.requestedType = requestedType;
        this.status = RideStatus.NEW;
        this.assignedTaxiId = null;
    }
    
    public RideRequest(Point pickupLocation, Point dropoffLocation) {
        this(pickupLocation, dropoffLocation, TaxiType.ECONOMY);
    }

    public static RideRequest createPoisonPill() {
        try {
            RideRequest pill = new RideRequest(new Point(0, 0), new Point(0, 0), null);
            java.lang.reflect.Field idField = RideRequest.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pill, -1L);
            return pill;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось создать poison pill", e);
        }
    }

    // =========== Геттеры ============
    public long getId() {
        return id;
    }

    public Point getPickupLocation() {
        return pickupLocation;
    }

    public Point getDropoffLocation() {
        return dropoffLocation;
    }

    public long getCreatedAtMillis() {
        return createdAtMillis;
    }

    public TaxiType getRequestedType() {
        return requestedType;
    }

    public RideStatus getStatus() {
        return status;
    }

    public Long getAssignedTaxiId() {
        return assignedTaxiId;
    }

    // ========= Сеттеры ===========
    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public void setAssignedTaxiId(Long assignedTaxiId) {
        this.assignedTaxiId = assignedTaxiId;
    }


    public double calculateDistance() {
        return pickupLocation.distanceTo(dropoffLocation);
    }

    public double calculateEstimatedFare() {
        double distance = calculateDistance();
        return requestedType.calculateFare(distance);
    }

    public long calculateWaitTime(long assignmentTimeMillis) {
        return assignmentTimeMillis - createdAtMillis;
    }

    @Override
    public String toString() {
        return String.format("Заказ #%d: %s -> %s [%s, статус: %s, такси: %s]", 
            id, pickupLocation, dropoffLocation, requestedType, status,
            assignedTaxiId != null ? "#" + assignedTaxiId : "не назначено");
    }
}
