package services;

import models.RideRequest;
import models.TaxiStatus;

import java.util.List;

/**
 * Стратегия выбора наименее загруженного такси
 */
public class LeastLoadedTaxiStrategy implements DispatchStrategy {
    
    @Override
    public TaxiWorker selectTaxi(List<TaxiWorker> taxis, RideRequest request) {
        if (taxis == null || taxis.isEmpty()) {
            return null;
        }
        
        TaxiWorker leastLoadedTaxi = null;
        int minCompletedRides = Integer.MAX_VALUE;
        
        // Ищем наименее загруженное свободное такси подходящего типа
        for (TaxiWorker taxi : taxis) {
            // Проверяем, что такси свободно
            if (taxi.getStatus() != TaxiStatus.IDLE) {
                continue;
            }
            
            // Проверяем тип такси
            if (!isTaxiSuitableForRequest(taxi, request)) {
                continue;
            }
            
            // Получаем количество выполненных поездок
            int completedRides = taxi.getCompletedRides();
            
            // Если это первое подходящее такси или оно менее загружено
            if (leastLoadedTaxi == null || completedRides < minCompletedRides) {
                minCompletedRides = completedRides;
                leastLoadedTaxi = taxi;
            }
            // Если загрузка одинаковая, выбираем ближайшее (опциональное улучшение)
            else if (completedRides == minCompletedRides) {
                double currentDistance = leastLoadedTaxi.getCurrentLocation()
                    .distanceTo(request.getPickupLocation());
                double newDistance = taxi.getCurrentLocation()
                    .distanceTo(request.getPickupLocation());
                
                if (newDistance < currentDistance) {
                    leastLoadedTaxi = taxi;
                }
            }
        }
        
        return leastLoadedTaxi;
    }
    
    /**
     * Проверяет, подходит ли такси для выполнения заказа
     */
    private boolean isTaxiSuitableForRequest(TaxiWorker taxi, RideRequest request) {
        if (request.getRequestedType() == null) {
            return true; // Если тип не указан, подходит любое такси
        }
        
        // Такси должно быть того же или выше класса
        return taxi.getType().ordinal() >= request.getRequestedType().ordinal();
    }
    
    @Override
    public String getName() {
        return "Наименее загруженное такси";
    }
}