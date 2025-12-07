package infra;

public class SimulationConfig {
    private final int numberOfTaxis;
    private final int simulationDurationSeconds;
    private final long meanRequestIntervalMillis;
    private final StrategyType strategyType; 
    
    private final double cityMinX;
    private final double cityMaxX;
    private final double cityMinY;
    private final double cityMaxY;
    
    private final double taxiSpeed;
    
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          StrategyType strategyType, 
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
    
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          String strategyName, 
                          double cityMinX, 
                          double cityMaxX, 
                          double cityMinY, 
                          double cityMaxY,
                          double taxiSpeed) {
        this(numberOfTaxis, 
             simulationDurationSeconds, 
             meanRequestIntervalMillis, 
             StrategyType.fromCode(strategyName), 
             cityMinX, cityMaxX, cityMinY, cityMaxY, 
             taxiSpeed);
    }
    
    public SimulationConfig(int numberOfTaxis, 
                          int simulationDurationSeconds, 
                          long meanRequestIntervalMillis, 
                          StrategyType strategyType) {
        this(numberOfTaxis, 
             simulationDurationSeconds, 
             meanRequestIntervalMillis, 
             strategyType,
             0.0,  
             100.0, 
             0.0,  
             100.0, 
             10.0  
        );
    }
    
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
    
    // =========== Геттеры для всех полей ================
    
    public int getNumberOfTaxis() {
        return numberOfTaxis;
    }
    
    public int getSimulationDurationSeconds() {
        return simulationDurationSeconds;
    }
    
    public long getMeanRequestIntervalMillis() {
        return meanRequestIntervalMillis;
    }
    
    public StrategyType getStrategyType() {
        return strategyType;
    }
    
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