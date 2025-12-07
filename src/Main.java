import infra.SimulationConfig;
import infra.SimulationRunner;
import infra.StrategyType;

import java.util.Scanner;

/**
 * Основной класс для запуска симуляции
 */
public class Main {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("    СИСТЕМА УПРАВЛЕНИЯ БЕСПИЛОТНЫМИ ТАКСИ");
        System.out.println("=".repeat(60));
        
        // Создаем единый Scanner для всего ввода
        Scanner scanner = new Scanner(System.in);
        
        try {
            // Чтение конфигурации
            SimulationConfig config = readConfiguration(args, scanner);
            
            // Вывод информации о конфигурации
            printConfigurationInfo(config);
            
            // Подтверждение запуска
            if (!confirmStart(scanner)) {
                System.out.println("Симуляция отменена пользователем.");
                return;
            }
            
            // Создание и запуск симуляции
            SimulationRunner runner = new SimulationRunner(config);
            runner.runSimulation();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("    СИМУЛЯЦИЯ УСПЕШНО ЗАВЕРШЕНА");
            System.out.println("=".repeat(60));
            
        } catch (Exception e) { // Изменено: ловим Exception вместо InterruptedException
            System.err.println("\nОшибка при выполнении симуляции: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            // Закрываем Scanner только здесь
            scanner.close();
        }
    }
    
    /**
     * Читает конфигурацию из аргументов или запрашивает у пользователя
     */
    private static SimulationConfig readConfiguration(String[] args, Scanner scanner) {
        // Если есть аргументы командной строки, используем их
        if (args.length >= 4) {
            try {
                int numberOfTaxis = Integer.parseInt(args[0]);
                int durationSeconds = Integer.parseInt(args[1]);
                long requestIntervalMillis = Long.parseLong(args[2]);
                String strategyCode = args[3];
                
                System.out.println("\nИспользуются параметры командной строки:");
                System.out.printf("- Количество такси: %d%n", numberOfTaxis);
                System.out.printf("- Длительность: %d секунд%n", durationSeconds);
                System.out.printf("- Интервал запросов: %d мс%n", requestIntervalMillis);
                System.out.printf("- Стратегия: %s%n", strategyCode);
                
                return new SimulationConfig(
                    numberOfTaxis,
                    durationSeconds,
                    requestIntervalMillis,
                    strategyCode
                );
                
            } catch (NumberFormatException e) {
                System.err.println("\nОшибка в формате аргументов. Переход к интерактивному вводу.");
                System.err.println("Формат: <кол-во такси> <длительность(с)> <интервал(мс)> <стратегия>");
                System.err.println("Пример: 5 30 2000 nearest");
            }
        }
        
        // Интерактивный ввод
        return readInteractiveConfiguration(scanner);
    }
    
    /**
     * Читает конфигурацию в интерактивном режиме
     */
    private static SimulationConfig readInteractiveConfiguration(Scanner scanner) {
        System.out.println("\nНАСТРОЙКА СИМУЛЯЦИИ");
        System.out.println("Введите параметры (или нажмите Enter для значений по умолчанию):");
        
        // Количество такси
        int numberOfTaxis = readIntInput(scanner, 
            "Количество такси [5]: ", 5, 1, 50);
        
        // Длительность симуляции
        int durationSeconds = readIntInput(scanner,
            "Длительность симуляции (секунд) [30]: ", 30, 5, 300);
        
        // Интервал между запросами
        long requestIntervalMillis = readLongInput(scanner,
            "Средний интервал между заказами (миллисекунд) [2000]: ", 
            2000L, 500L, 10000L);
        
        // Стратегия
        StrategyType strategyType = readStrategyInput(scanner,
            "Стратегия распределения (nearest/leastloaded) [nearest]: ");
        
        return new SimulationConfig(
            numberOfTaxis,
            durationSeconds,
            requestIntervalMillis,
            strategyType
        );
    }
    
    /**
     * Читает целочисленный ввод с валидацией
     */
    private static int readIntInput(Scanner scanner, String prompt, 
                                   int defaultValue, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                return defaultValue;
            }
            
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.err.printf("Значение должно быть от %d до %d. Попробуйте снова.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат числа. Попробуйте снова.");
            }
        }
    }
    
    /**
     * Читает long ввод с валидацией
     */
    private static long readLongInput(Scanner scanner, String prompt,
                                     long defaultValue, long min, long max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                return defaultValue;
            }
            
            try {
                long value = Long.parseLong(input);
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.err.printf("Значение должно быть от %d до %d. Попробуйте снова.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.err.println("Неверный формат числа. Попробуйте снова.");
            }
        }
    }
    
    /**
     * Читает ввод стратегии
     */
    private static StrategyType readStrategyInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.isEmpty()) {
                return StrategyType.NEAREST;
            }
            
            try {
                return StrategyType.fromCode(input);
            } catch (IllegalArgumentException e) {
                System.err.println("Неизвестная стратегия. Доступные варианты: nearest, leastloaded");
            }
        }
    }
    
    /**
     * Выводит информацию о конфигурации
     */
    private static void printConfigurationInfo(SimulationConfig config) {
        System.out.println("\n" + "-".repeat(60));
        System.out.println("НАСТРОЙКИ СИМУЛЯЦИИ:");
        System.out.println("-".repeat(60));
        System.out.printf("Количество такси: %d%n", config.getNumberOfTaxis());
        System.out.printf("Длительность: %d секунд%n", config.getSimulationDurationSeconds());
        System.out.printf("Интервал запросов: %d мс%n", config.getMeanRequestIntervalMillis());
        System.out.printf("Стратегия: %s%n", config.getStrategyType());
        System.out.printf("Город: (%.1f, %.1f) - (%.1f, %.1f)%n",
            config.getCityMinX(), config.getCityMinY(),
            config.getCityMaxX(), config.getCityMaxY());
        System.out.println("-".repeat(60));
    }
    
    /**
     * Запрашивает подтверждение запуска
     */
    private static boolean confirmStart(Scanner scanner) {
        System.out.print("\nЗапустить симуляцию? (y/n) [y]: ");
        String input = scanner.nextLine().trim().toLowerCase();
        
        return input.isEmpty() || input.equals("y") || input.equals("да");
    }
}
