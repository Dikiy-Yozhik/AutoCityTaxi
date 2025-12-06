package ru.mystudent.taxi.infra;

/**
 * Конфигурация симуляции
 */
public class SimulationConfig {
    
    // Параметры симуляции
    private final int numberOfTaxis;
    private final int simulationDurationSeconds;
    private final long meanRequestIntervalMillis;
    private final String strategyName;
    
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
                          String strategyName,
                          double cityMinX, 
                          double cityMaxX, 
                          double cityMinY, 
                          double cityMaxY,
                          double taxiSpeed) {
        this.numberOfTaxis = numberOfTaxis;
        this.simulationDurationSeconds = simulationDurationSeconds;
        this.meanRequestIntervalMillis = meanRequestIntervalMillis;
        this.strategyName = strategyName;
        this.cityMinX = cityMinX;
        this.cityMaxX = cityMaxX;
        this.cityMinY = cityMinY;
        this.cityMaxY = cityMaxY;
        this.taxiSpeed = taxiSpeed;
    }
    
    /**
     * Упрощенный конструктор с параметрами по умолчанию для города
     */
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          String strategyName) {
        this(numberOfTaxis, 
             simulationDurationSeconds, 
             meanRequestIntervalMillis, 
             strategyName,
             0.0,  // cityMinX
             100.0, // cityMaxX
             0.0,  // cityMinY
             100.0, // cityMaxY
             10.0  // taxiSpeed (10 единиц расстояния в секунду)
        );
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
    
    public String getStrategyName() {
        return strategyName;
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
               ", strategyName='" + strategyName + '\'' +
               ", cityMinX=" + cityMinX +
               ", cityMaxX=" + cityMaxX +
               ", cityMinY=" + cityMinY +
               ", cityMaxY=" + cityMaxY +
               ", taxiSpeed=" + taxiSpeed +
               '}';
    }
}