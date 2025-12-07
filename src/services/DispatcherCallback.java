package services;

import models.RideRequest;

public interface DispatcherCallback {
    
    void onRideCompleted(TaxiWorker taxi, RideRequest ride, 
                         double distance, double fare, long waitTimeMillis);
}
