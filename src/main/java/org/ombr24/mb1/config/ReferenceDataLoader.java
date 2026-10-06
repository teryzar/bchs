package org.ombr24.mb1.config;

import org.ombr24.mb1.exception.FileOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Завантажувач довідкових даних з файлів конфігурації
 */
public class ReferenceDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(ReferenceDataLoader.class);
    private static final String MAP_DELIMITER = ":";

    public ReferenceData load(AppConfig appConfig) {
        logger.debug("Завантаження довідкових даних...");

        List<String> positions70 = readFileToList(appConfig.positions70(), "позиції 70");
        List<String> positions100 = readFileToList(appConfig.positions100(), "позиції 100");
        Map<String, String> commanderPositions = readFileToMap(appConfig.commanderPositions(), "позиції командирів");

        logger.info("Довідкові дані завантажено: позицій 70={}, позицій 100={}, командирів={}", 
                positions70.size(), positions100.size(), commanderPositions.size());

        return new ReferenceData(positions70, positions100, commanderPositions);
    }

    /**
     * Читає файл у вигляді Map (формат: ключ:значення)
     */
    private Map<String, String> readFileToMap(String filePath, String description) {
        Path path = resolvePath(filePath);
        logger.debug("Читання {} з файлу: {}", description, path);

        validateFileExists(path, description);

        try {
            Map<String, String> result = Files.lines(path)
                    .filter(line -> !line.trim().isEmpty())
                    .filter(line -> line.contains(MAP_DELIMITER))
                    .collect(Collectors.toMap(
                            line -> line.split(MAP_DELIMITER)[0].trim(),
                            line -> line.split(MAP_DELIMITER)[1].trim(),
                            (v1, v2) -> {
                                logger.warn("Дублікат ключа в {}: використовується перше значення", description);
                                return v1;
                            }
                    ));

            logger.debug("Успішно прочитано {} записів з {}", result.size(), description);
            return result;

        } catch (IOException e) {
            throw new FileOperationException("Не вдалося прочитати файл " + description + ": " + path, e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new FileOperationException(
                    "Некоректний формат даних в " + description + ". Очікується формат 'ключ:значення'", e);
        }
    }

    /**
     * Читає файл у вигляді списку рядків
     */
    private List<String> readFileToList(String filePath, String description) {
        Path path = resolvePath(filePath);
        logger.debug("Читання {} з файлу: {}", description, path);

        validateFileExists(path, description);

        try {
            List<String> result = Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());

            logger.debug("Успішно прочитано {} рядків з {}", result.size(), description);
            return result;

        } catch (IOException e) {
            throw new FileOperationException("Не вдалося прочитати файл " + description + ": " + path, e);
        }
    }

    /**
     * Перетворює шлях на Path, обробляючи як абсолютні, так і відносні шляхи
     */
    private Path resolvePath(String filePath) {
        Path path = Paths.get(filePath);
        // Якщо шлях вже абсолютний (наприклад, з тестів), використовуємо його як є
        if (path.isAbsolute()) {
            return path;
        }
        // Інакше резолвимо відносно робочої директорії
        return Paths.get(System.getProperty("user.dir"), filePath);
    }

    /**
     * Перевіряє, чи існує файл
     */
    private void validateFileExists(Path path, String description) {
        if (!Files.exists(path)) {
            throw new FileOperationException(
                    String.format("Файл %s не знайдено: %s", description, path));
        }
        if (!Files.isReadable(path)) {
            throw new FileOperationException(
                    String.format("Файл %s не доступний для читання: %s", description, path));
        }
    }
}
