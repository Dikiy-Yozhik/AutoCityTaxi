package infra;

/**
 * Конфигурация симуляции
 */
public class SimulationConfig {
    
    // Параметры симуляции
    private final int numberOfTaxis;
    private final int simulationDurationSeconds;
    private final long meanRequestIntervalMillis;
    private final StrategyType strategyType; // Изменено с String на StrategyType
    
    // Границы города для генерации координат
    private final double cityMinX;
    private final double cityMaxX;
    private final double cityMinY;
    private final double cityMaxY;
    
    // Скорость движения (единиц расстояния в секунду)
    private final double taxiSpeed;
    
    /**
     * Конструктор со всеми параметрами
     */
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          StrategyType strategyType, // Изменено
                          double cityMinX, 
                          double cityMaxX, 
                          double cityMinY, 
                          double cityMaxY,
                          double taxiSpeed) {
        this.numberOfTaxis = numberOfTaxis;
        this.simulationDurationSeconds = simulationDurationSeconds;
        this.meanRequestIntervalMillis = meanRequestIntervalMillis;
        this.strategyType = strategyType;
        this.cityMinX = cityMinX;
        this.cityMaxX = cityMaxX;
        this.cityMinY = cityMinY;
        this.cityMaxY = cityMaxY;
        this.taxiSpeed = taxiSpeed;
    }
    
    /**
     * Конструктор со строкой для стратегии (для обратной совместимости)
     */
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          String strategyName, // Оставляем для обратной совместимости
                          double cityMinX, 
                          double cityMaxX, 
                          double cityMinY, 
                          double cityMaxY,
                          double taxiSpeed) {
        this(numberOfTaxis, 
             simulationDurationSeconds, 
             meanRequestIntervalMillis, 
             StrategyType.fromCode(strategyName), // Конвертируем строку в enum
             cityMinX, cityMaxX, cityMinY, cityMaxY, 
             taxiSpeed);
    }
    
    /**
     * Упрощенный конструктор с параметрами по умолчанию для города
     */
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          StrategyType strategyType) {
        this(numberOfTaxis, 
             simulationDurationSeconds, 
             meanRequestIntervalMillis, 
             strategyType,
             0.0,  // cityMinX
             100.0, // cityMaxX
             0.0,  // cityMinY
             100.0, // cityMaxY
             10.0  // taxiSpeed (10 единиц расстояния в секунду)
        );
    }
    
    /**
     * Упрощенный конструктор со строкой (для обратной совместимости)
     */
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          String strategyName) {
        this(numberOfTaxis, 
             simulationDurationSeconds, 
             meanRequestIntervalMillis, 
             StrategyType.fromCode(strategyName),
             0.0, 100.0, 0.0, 100.0, 10.0);
    }
    
    // Геттеры для всех полей
    
    public int getNumberOfTaxis() {
        return numberOfTaxis;
    }
    
    public int getSimulationDurationSeconds() {
        return simulationDurationSeconds;
    }
    
    public long getMeanRequestIntervalMillis() {
        return meanRequestIntervalMillis;
    }
    
    // Новый геттер для StrategyType
    public StrategyType getStrategyType() {
        return strategyType;
    }
    
    // Геттер для обратной совместимости (возвращает код стратегии)
    public String getStrategyName() {
        return strategyType.getCode();
    }
    
    public double getCityMinX() {
        return cityMinX;
    }
    
    public double getCityMaxX() {
        return cityMaxX;
    }
    
    public double getCityMinY() {
        return cityMinY;
    }
    
    public double getCityMaxY() {
        return cityMaxY;
    }
    
    public double getTaxiSpeed() {
        return taxiSpeed;
    }
    
    @Override
    public String toString() {
        return "SimulationConfig{" +
               "numberOfTaxis=" + numberOfTaxis +
               ", simulationDurationSeconds=" + simulationDurationSeconds +
               ", meanRequestIntervalMillis=" + meanRequestIntervalMillis +
               ", strategyType=" + strategyType +
               ", cityMinX=" + cityMinX +
               ", cityMaxX=" + cityMaxX +
               ", cityMinY=" + cityMinY +
               ", cityMaxY=" + cityMaxY +
               ", taxiSpeed=" + taxiSpeed +
               '}';
    }
}