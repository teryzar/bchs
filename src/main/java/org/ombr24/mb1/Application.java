package org.ombr24.mb1;

import org.ombr24.mb1.config.*;
import org.ombr24.mb1.exception.InvalidInputException;
import org.ombr24.mb1.utils.XlsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Головний клас додатку для обробки звітів бойових підрозділів
 */
public class Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final int MIN_DAY = 1;
    private static final int MAX_DAY = 31;

    public static void main(String[] args) {
        // Встановлюємо UTF-8 для консольного виводу
        // Не закриваємо ці потоки, оскільки вони системні
        PrintStream out = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        System.setOut(out);
        
        PrintStream err = new PrintStream(System.err, true, StandardCharsets.UTF_8);
        System.setErr(err);
        
        // Використовуємо try-with-resources для автоматичного закриття reader
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            
            logger.info("=== Запуск додатку обробки звітів ===");
            logger.info("Робоча директорія: {}", System.getProperty("user.dir"));

            AppConfig config = new AppConfigLoader().load();
            logger.info("Конфігурацію завантажено успішно");

            ReferenceData refData = new ReferenceDataLoader().load(config);
            logger.info("Довідкові дані завантажено успішно");

            int startDate = readDate("Введіть початкову дату (день місяця)", reader);
            int endDate = readDate("Введіть кінцеву дату (день місяця)", reader);

            validateDateRange(startDate, endDate);

            logger.info("Обробка звітів з {} по {} число", startDate, endDate);

            for (int dayOfMonth = startDate; dayOfMonth <= endDate; dayOfMonth++) {
                logger.info("Обробка даних за {} число...", dayOfMonth);
                new XlsProcessor(config, refData).process(dayOfMonth);
                logger.info("Обробку за {} число завершено успішно", dayOfMonth);
            }

            logger.info("=== Обробка всіх звітів завершена успішно ===");

        } catch (InvalidInputException e) {
            logger.error("Помилка валідації вводу: {}", e.getMessage());
            System.err.println("Помилка: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            logger.error("Помилка вводу/виводу", e);
            System.err.println("Помилка вводу/виводу: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Критична помилка виконання програми", e);
            System.err.println("Критична помилка: " + e.getMessage());
            System.exit(1);
        }
        // Примітка: System.out і System.err не закриваємо, оскільки вони системні потоки
    }

    /**
     * Читає та валідує день місяця від користувача
     */
    private static int readDate(String prompt, BufferedReader reader) {
        System.out.println(prompt + " (" + MIN_DAY + "-" + MAX_DAY + "):");

        try {
            String input = reader.readLine();
            
            if (input == null || input.trim().isEmpty()) {
                throw new InvalidInputException("Введення не може бути порожнім");
            }

            try {
                int day = Integer.parseInt(input.trim());
                validateDay(day);
                return day;
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Невірний формат числа: " + input);
            }

        } catch (IOException e) {
            throw new InvalidInputException("Помилка читання вводу", e);
        }
    }

    /**
     * Валідує день місяця
     */
    private static void validateDay(int day) {
        if (day < MIN_DAY || day > MAX_DAY) {
            throw new InvalidInputException(
                    String.format("День має бути в діапазоні %d-%d. Введено: %d", 
                            MIN_DAY, MAX_DAY, day));
        }
    }

    /**
     * Валідує діапазон дат
     */
    private static void validateDateRange(int startDate, int endDate) {
        if (startDate > endDate) {
            throw new InvalidInputException(
                    String.format("Початкова дата (%d) не може бути більшою за кінцеву (%d)", 
                            startDate, endDate));
        }
    }
}
