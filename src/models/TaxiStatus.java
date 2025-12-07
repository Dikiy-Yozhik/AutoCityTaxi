package models;


public enum TaxiStatus {
    IDLE,            // Свободно, ожидает заказ
    TO_PICKUP,       // Едет к месту посадки клиента
    WITH_PASSENGER,  // Везет клиента к месту назначения
    MAINTENANCE,     // На техобслуживании (на будущее?)
    OFFLINE;         // Не работает (на будущее?)

    @Override
    public String toString() {
        switch (this) {
            case IDLE: return "Свободен";
            case TO_PICKUP: return "Едет к клиенту";
            case WITH_PASSENGER: return "Везет клиента";
            case MAINTENANCE: return "На обслуживании";
            case OFFLINE: return "Неактивен";
            default: return name();
        }
    }
}
