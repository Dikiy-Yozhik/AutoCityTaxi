package services;

import models.RideRequest;

import java.util.List;

/**
 * Интерфейс стратегии распределения заказов между такси
 */
public interface DispatchStrategy {
    
    /**
     * Выбирает подходящее такси для выполнения заказа
     * 
     * @param taxis список доступных такси
     * @param request заявка на поездку
     * @return выбранное такси или null, если подходящего такси нет
     */
    TaxiWorker selectTaxi(List<TaxiWorker> taxis, RideRequest request);
    
    /**
     * Возвращает название стратегии
     */
    String getName();
}