package services;

import models.RideRequest;

import java.util.List;

public interface DispatchStrategy {
    
    TaxiWorker selectTaxi(List<TaxiWorker> taxis, RideRequest request);
    
    String getName();
}