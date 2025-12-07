package infra;

public enum StrategyType {
    NEAREST("nearest", "Ближайшее такси"),
    LEAST_LOADED("leastloaded", "Наименее загруженное такси");
    
    private final String code;
    private final String description;
    
    StrategyType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static StrategyType fromCode(String code) {
        if (code == null) {
            return NEAREST;
        }
        
        for (StrategyType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        
        // Если код не распознан, возвращаем значение по умолчанию
        return NEAREST;
    }
    
    @Override
    public String toString() {
        return description + " (" + code + ")";
    }
}
