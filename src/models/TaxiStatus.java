package models;

/**
 * Статусы такси.
 */
public enum TaxiStatus {
    IDLE,            // Свободно, ожидает заказ
    TO_PICKUP,       // Едет к месту посадки клиента
    WITH_PASSENGER,  // Весет клиента к месту назначения
    MAINTENANCE,     // На техобслуживании (опционально)
    OFFLINE;         // Не работает (опционально)

    @Override
    public String toString() {
        switch (this) {
            case IDLE: return "Свободен";
            case TO_PICKUP: return "Едет к клиенту";
            case WITH_PASSENGER: return "Вестет клиента";
            case MAINTENANCE: return "На обслуживании";
            case OFFLINE: return "Неактивен";
            default: return name();
        }
    }
}
