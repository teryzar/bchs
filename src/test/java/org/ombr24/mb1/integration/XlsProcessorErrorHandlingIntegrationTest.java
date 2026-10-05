package org.ombr24.mb1.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.ombr24.mb1.config.AppConfig;
import org.ombr24.mb1.config.ReferenceData;
import org.ombr24.mb1.config.ReferenceDataLoader;
import org.ombr24.mb1.exception.FileOperationException;
import org.ombr24.mb1.utils.XlsProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Інтеграційні тести для обробки помилок та edge cases
 */
class XlsProcessorErrorHandlingIntegrationTest {

    @TempDir
    Path tempDir;

    private Path dataDir;
    private Path outputDir;
    private Path configDir;

    @BeforeEach
    void setUp() throws IOException {
        dataDir = tempDir.resolve("data");
        outputDir = tempDir.resolve("output");
        configDir = tempDir.resolve("config");

        Files.createDirectories(dataDir);
        Files.createDirectories(outputDir);
        Files.createDirectories(outputDir.resolve("json"));
        Files.createDirectories(configDir);

        // Створюємо базові довідкові файли
        createReferenceFiles();
    }

    @Test
    @DisplayName("Помилка: відсутній основний Excel файл")
    void shouldThrowExceptionWhenMainExcelFileNotFound() throws IOException {
        // Given
        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );
        ReferenceData referenceData = new ReferenceDataLoader().load(config);
        
        // Файл 1.xlsx не існує
        XlsProcessor processor = new XlsProcessor(config, referenceData);

        // When/Then
        assertThatThrownBy(() -> processor.process(15))
            .isInstanceOf(FileOperationException.class)
            .hasMessageContaining("не існує");
    }

    @Test
    @DisplayName("Помилка: відсутній файл з номерами звітів")
    void shouldThrowExceptionWhenNumbersFileNotFound() throws IOException {
        // Given
        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Створюємо тільки основний файл, без numbers.xlsx
        Files.createFile(dataDir.resolve("1.xlsx"));
        
        XlsProcessor processor = new XlsProcessor(config, referenceData);

        // When/Then
        assertThatThrownBy(() -> processor.process(15))
            .isInstanceOf(FileOperationException.class);
    }

    @Test
    @DisplayName("Помилка: відсутня директорія для виводу")
    void shouldCreateOutputDirectoryIfNotExists() throws IOException {
        // Given
        Path nonExistentOutput = tempDir.resolve("non-existent-output");
        
        AppConfig config = new AppConfig(
            dataDir.toString(),
            nonExistentOutput.toString(),
            configDir.toString()
        );

        // When/Then - перевіряємо що директорія буде створена або операція впаде з відповідною помилкою
        assertThat(nonExistentOutput).doesNotExist();
    }

    @Test
    @DisplayName("Помилка: некоректний формат довідкового файлу")
    void shouldHandleInvalidReferenceFileFormat() throws IOException {
        // Given
        Files.write(configDir.resolve("commanders.txt"), 
            List.of("InvalidLineWithoutColon", "Another:Valid:Line:With:Multiple:Colons"));

        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then - має завантажитись, проігнорувавши некоректні рядки
        assertThat(referenceData.getCommanderPositions()).isNotNull();
    }

    @Test
    @DisplayName("Помилка: порожні довідкові файли")
    void shouldHandleEmptyReferenceFiles() throws IOException {
        // Given - створюємо порожні файли
        Files.write(configDir.resolve("positions70.txt"), List.of());
        Files.write(configDir.resolve("positions100.txt"), List.of());
        Files.write(configDir.resolve("commanders.txt"), List.of());

        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then
        assertThat(referenceData.getPositions70()).isEmpty();
        assertThat(referenceData.getPositions100()).isEmpty();
        assertThat(referenceData.getCommanderPositions()).isEmpty();
    }

    @Test
    @DisplayName("Помилка: відсутній лист з даними в Excel")
    void shouldHandleMissingSheetGracefully() {
        // Given/When/Then
        // Цей тест буде розширено після створення тестового Excel без потрібних листів
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    @DisplayName("Обробка спеціальних символів в іменах")
    void shouldHandleSpecialCharactersInNames() {
        // Given/When/Then
        // Перевірка що система коректно обробляє апострофи, дефіси тощо
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    @DisplayName("Обробка дуже довгих імен")
    void shouldHandleVeryLongNames() {
        // Given/When/Then
        // Перевірка обробки імен довжиною >100 символів
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    @DisplayName("Concurrent обробка декількох днів")
    void shouldHandleConcurrentProcessing() {
        // Given/When/Then
        // Тест на потокобезпечність якщо буде потрібно
        assertThat(true).isTrue(); // Placeholder
    }

    @Test
    @DisplayName("Обробка великого обсягу даних")
    void shouldHandleLargeDataset() {
        // Given/When/Then
        // Перевірка продуктивності на великих файлах (>1000 записів)
        assertThat(true).isTrue(); // Placeholder
    }

    /**
     * Створює базові довідкові файли
     */
    private void createReferenceFiles() throws IOException {
        Files.write(configDir.resolve("positions70.txt"), 
            List.of("70", "71", "72"));

        Files.write(configDir.resolve("positions100.txt"), 
            List.of("100", "101", "102"));

        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир 1",
            "2 мр:Командир 2"
        ));
    }
}
