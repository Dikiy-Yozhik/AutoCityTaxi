package ru.mystudent.taxi.service;

import ru.mystudent.taxi.model.RideRequest;

/**
 * Интерфейс обратного вызова для уведомлений от такси
 */
public interface DispatcherCallback {
    
    /**
     * Вызывается при завершении поездки такси
     * 
     * @param taxi такси, завершившее поездку
     * @param ride выполненный заказ
     * @param distance пройденное расстояние
     * @param fare стоимость поездки
     * @param waitTimeMillis время ожидания клиента
     */
    void onRideCompleted(TaxiWorker taxi, RideRequest ride, 
                         double distance, double fare, long waitTimeMillis);
}