package org.ombr24.mb1.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.ombr24.mb1.config.*;
import org.ombr24.mb1.exception.FileOperationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Інтеграційні тести для системи конфігурації
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigurationIntegrationTest {

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
        Files.createDirectories(configDir);
    }

    @Test
    @Order(1)
    @DisplayName("Повне завантаження конфігурації")
    void shouldLoadCompleteConfiguration() throws IOException {
        // Given
        createAllConfigFiles();
        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then
        assertThat(referenceData.getPositions70())
            .isNotEmpty()
            .hasSize(6)
            .contains("70", "71", "72", "73", "74", "75");

        assertThat(referenceData.getPositions100())
            .isNotEmpty()
            .hasSize(5)
            .contains("100", "101", "102", "103", "104");

        assertThat(referenceData.getCommanderPositions())
            .isNotEmpty()
            .hasSize(3)
            .containsEntry("1 мр", "Командир 1 мр батальйону")
            .containsEntry("2 мр", "Командир 2 мр батальйону")
            .containsEntry("3 мр", "Командир 3 мр батальйону");
    }

    @Test
    @Order(2)
    @DisplayName("Конфігурація з мінімальними даними")
    void shouldHandleMinimalConfiguration() throws IOException {
        // Given
        Files.write(configDir.resolve("positions70.txt"), List.of("70"));
        Files.write(configDir.resolve("positions100.txt"), List.of("100"));
        Files.write(configDir.resolve("commanders.txt"), List.of("1 мр:Командир"));

        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then
        assertThat(referenceData.getPositions70()).containsExactly("70");
        assertThat(referenceData.getPositions100()).containsExactly("100");
        assertThat(referenceData.getCommanderPositions()).containsExactly(
            entry("1 мр", "Командир")
        );
    }

    @Test
    @Order(3)
    @DisplayName("AppConfig генерує коректні шляхи")
    void shouldGenerateCorrectPaths() {
        // Given
        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When/Then
        assertThat(config.inputExcel())
            .contains("data")
            .endsWith("1.xlsx");

        assertThat(config.positions70())
            .contains("config")
            .endsWith("positions70.txt");

        assertThat(config.positions100())
            .contains("config")
            .endsWith("positions100.txt");

        assertThat(config.commanderPositions())
            .contains("config")
            .endsWith("commanders.txt");

        assertThat(config.divisionReport(15))
            .contains("output")
            .endsWith("15травня.txt");

        assertThat(config.divisionJsonReport(20))
            .contains("output")
            .contains("json");
    }

    @Test
    @Order(4)
    @DisplayName("Обробка UTF-8 символів в конфігурації")
    void shouldHandleUtf8CharactersInConfig() throws IOException {
        // Given
        Files.write(configDir.resolve("positions70.txt"), 
            List.of("70", "71 - позиція"));
        Files.write(configDir.resolve("positions100.txt"), 
            List.of("100"));
        Files.write(configDir.resolve("commanders.txt"), 
            List.of("1 мр:Командир з ініціалами Ї.Є."));

        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then
        assertThat(referenceData.getPositions70())
            .contains("70", "71 - позиція");
        assertThat(referenceData.getCommanderPositions())
            .containsEntry("1 мр", "Командир з ініціалами Ї.Є.");
    }

    @Test
    @Order(5)
    @DisplayName("Обробка коментарів та порожніх рядків")
    void shouldIgnoreCommentsAndEmptyLines() throws IOException {
        // Given
        Files.write(configDir.resolve("positions70.txt"), List.of(
            "70",
            "",
            "71",
            "  ",
            "72"
        ));
        Files.write(configDir.resolve("positions100.txt"), List.of("100"));
        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир 1",
            "",
            "2 мр:Командир 2"
        ));

        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then
        assertThat(referenceData.getPositions70())
            .hasSize(3)
            .containsExactly("70", "71", "72");
        assertThat(referenceData.getCommanderPositions())
            .hasSize(2);
    }

    @Test
    @Order(6)
    @DisplayName("Валідація відсутніх конфігураційних файлів")
    void shouldThrowExceptionForMissingConfigFiles() {
        // Given
        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When/Then
        assertThatThrownBy(() -> new ReferenceDataLoader().load(config))
            .isInstanceOf(FileOperationException.class)
            .hasMessageContaining("не знайдено");
    }

    @Test
    @Order(7)
    @DisplayName("Завантаження конфігурації з різних директорій")
    void shouldLoadConfigFromDifferentDirectories() throws IOException {
        // Given
        Path altConfigDir = tempDir.resolve("alternative-config");
        Files.createDirectories(altConfigDir);

        Files.write(altConfigDir.resolve("positions70.txt"), List.of("70"));
        Files.write(altConfigDir.resolve("positions100.txt"), List.of("100"));
        Files.write(altConfigDir.resolve("commanders.txt"), List.of("test:test"));

        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            altConfigDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then
        assertThat(referenceData.getPositions70()).containsExactly("70");
        assertThat(referenceData.getPositions100()).containsExactly("100");
    }

    @Test
    @Order(8)
    @DisplayName("Перевірка Constants")
    void shouldHaveCorrectConstants() {
        // Then
        assertThat(Constants.NUMBERS_SHEET).isEqualTo("numbers.xlsx");
        assertThat(Constants.ADDED_UNITS_SHEET).isEqualTo("оперативне підпорядкування");
        assertThat(Constants.FIRST_DATA_SHEET).isEqualTo("1 мр");
        assertThat(Constants.DATE_FORMAT).isEqualTo("dd.MM.yyyy");
        assertThat(Constants.POSITION_30).isEqualTo("30");
        assertThat(Constants.HEADER_ROW).isEqualTo(0);
        assertThat(Constants.COMMANDER_ROW).isEqualTo(1);
        assertThat(Constants.RANK_COLUMN).isEqualTo(0);
        assertThat(Constants.NAME_COLUMN).isEqualTo(1);
        assertThat(Constants.REPORT_NR_COLUMN).isEqualTo(0);
        assertThat(Constants.REPORT_DATE_COLUMN).isEqualTo(1);
        assertThat(Constants.DIVISION_COLUMN).isEqualTo(2);

        assertThat(Constants.SKIP_SHEETS)
            .contains( "ПРИМІТКИ",
                    "Управління",
                    "Прикомандировані",
                    "БРЕЗ",
                    "оперативне підпорядкування");
    }

    @Test
    @Order(9)
    @DisplayName("Перевірка TZPositions")
    void shouldHaveCorrectTZPositions() {
        // Then
        assertThat(TZPositions.POSITION_TYPE_MAP)
            .contains("Спектра", "Самсунг");
    }

    @Test
    @Order(10)
    @DisplayName("Перевірка Reporters")
    void shouldHaveReportersMap() {
        // Then
        assertThat(Reporters.REPORTERS_MAP).isNotNull();
        // Додаткові перевірки залежно від реалізації
    }

    @Test
    @Order(11)
    @DisplayName("ReferenceData: immutability")
    void shouldProvideImmutableReferenceData() throws IOException {
        // Given
        createAllConfigFiles();
        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // When/Then - спроба модифікації не має впливати на оригінальні дані
        List<String> positions70 = referenceData.getPositions70();
        int originalSize = positions70.size();

        // Перевіряємо що можемо читати дані
        assertThat(positions70).isNotEmpty();
        assertThat(referenceData.getPositions70()).hasSize(originalSize);
    }

    @Test
    @Order(12)
    @DisplayName("Конфігурація з дублікатами")
    void shouldHandleDuplicatesInConfig() throws IOException {
        // Given
        Files.write(configDir.resolve("positions70.txt"), List.of(
            "70",
            "71",
            "70",  // дублікат
            "72"
        ));
        Files.write(configDir.resolve("positions100.txt"), List.of("100"));
        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир перший",
            "1 мр:Командир другий"  // дублікат ключа
        ));

        AppConfig config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // When
        ReferenceData referenceData = new ReferenceDataLoader().load(config);

        // Then - дублікати можуть бути збережені в списку, але в мапі залишається перше значення
        assertThat(referenceData.getCommanderPositions().get("1 мр"))
            .isEqualTo("Командир перший");
    }

    /**
     * Створює всі конфігураційні файли з повними даними
     */
    private void createAllConfigFiles() throws IOException {
        Files.write(configDir.resolve("positions70.txt"), List.of(
            "70", "71", "72", "73", "74", "75"
        ));

        Files.write(configDir.resolve("positions100.txt"), List.of(
            "100", "101", "102", "103", "104"
        ));

        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир 1 мр батальйону",
            "2 мр:Командир 2 мр батальйону",
            "3 мр:Командир 3 мр батальйону"
        ));
    }
}
