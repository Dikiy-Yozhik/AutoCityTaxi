package models;


public enum RideStatus {
    NEW,           // Новый заказ, ожидает назначения такси
    ASSIGNED,      // Заказ назначен такси, но поездка еще не началась
    IN_PROGRESS,   // Такси везет клиента
    COMPLETED,     // Поездка завершена
    CANCELLED;     // Поездка отменена (опционально, можно добавить)

    @Override
    public String toString() {
        switch (this) {
            case NEW: return "Новый";
            case ASSIGNED: return "Назначен";
            case IN_PROGRESS: return "В процессе";
            case COMPLETED: return "Завершен";
            case CANCELLED: return "Отменен";
            default: return name();
        }
    }
}
