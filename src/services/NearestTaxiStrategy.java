package services;

import models.RideRequest;
import models.TaxiStatus;

import java.util.List;


public class NearestTaxiStrategy implements DispatchStrategy {
    
    @Override
    public TaxiWorker selectTaxi(List<TaxiWorker> taxis, RideRequest request) {
        if (taxis == null || taxis.isEmpty()) {
            return null;
        }
        
        TaxiWorker nearestTaxi = null;
        double minDistance = Double.MAX_VALUE;
        
        // Ищем ближайшее свободное такси подходящего типа
        for (TaxiWorker taxi : taxis) {
            // Проверяем, что такси свободно
            if (taxi.getStatus() != TaxiStatus.IDLE) {
                continue;
            }
            
            // Проверяем тип такси
            if (!isTaxiSuitableForRequest(taxi, request)) {
                continue;
            }
            
            // Вычисляем расстояние от такси до точки посадки
            double distance = taxi.getCurrentLocation().distanceTo(request.getPickupLocation());
            
            if (distance < minDistance) {
                minDistance = distance;
                nearestTaxi = taxi;
            }
        }
        
        return nearestTaxi;
    }
    

    private boolean isTaxiSuitableForRequest(TaxiWorker taxi, RideRequest request) {
        if (request.getRequestedType() == null) {
            return true; // Если тип не указан, подходит любое такси
        }
        
        return taxi.getType().ordinal() == request.getRequestedType().ordinal();
    }
    
    @Override
    public String getName() {
        return "Ближайшее такси";
    }
}
