package org.ombr24.mb1.integration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.ombr24.mb1.config.AppConfig;
import org.ombr24.mb1.config.ReferenceData;
import org.ombr24.mb1.config.ReferenceDataLoader;
import org.ombr24.mb1.report.DivisionReportWriter;
import org.ombr24.mb1.report.JsonReportWriter;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.utils.EngagementContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Інтеграційні тести для ReportWriter компонентів
 */
class ReportWriterIntegrationTest {

    @TempDir
    Path tempDir;

    private AppConfig config;
    private ReferenceData referenceData;
    private EngagementContext context;
    private Path outputDir;
    private Path configDir;

    @BeforeEach
    void setUp() throws IOException {
        outputDir = tempDir.resolve("output");
        configDir = tempDir.resolve("config");

        Files.createDirectories(outputDir);
        Files.createDirectories(outputDir.resolve("json"));
        Files.createDirectories(configDir);

        config = new AppConfig(
            tempDir.resolve("data").toString(),
            outputDir.toString(),
            configDir.toString()
        );

        createReferenceFiles();
        referenceData = new ReferenceDataLoader().load(config);
        context = createTestContext();
    }

    @Test
    @DisplayName("DivisionReportWriter: генерація повного звіту")
    void shouldGenerateCompleteDivisionReport() throws IOException {
        // Given
        String reportFile = outputDir.resolve("test-report.txt").toString();
        DivisionReportWriter writer = new DivisionReportWriter(referenceData);

        // When
        writer.write(reportFile, context);

        // Then
        Path report = Path.of(reportFile);
        assertThat(report).exists();
        
        String content = Files.readString(report);
        assertThat(content)
            .contains("1 мр")
            .contains("2 мр")
            .contains("БП «70» (приведені):")
            .contains("100% (облікові):")
            .contains("Особовий склад підрозділу:")
            .contains("_____________________________________________________");
    }

    @Test
    @DisplayName("JsonReportWriter: генерація JSON звітів для всіх підрозділів")
    void shouldGenerateJsonReportsForAllDivisions() throws IOException {
        // Given
        String baseReportFile = outputDir.resolve("json/test").toString();
        JsonReportWriter writer = new JsonReportWriter(referenceData);

        // When
        writer.write(baseReportFile, context);

        // Then
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());

        // Має бути 2 JSON файли (для 1 мр та 2 мр)
        assertThat(jsonFiles).hasSizeGreaterThanOrEqualTo(2);
        
        // Перевіряємо що файли не порожні
        for (Path jsonFile : jsonFiles) {
            assertThat(Files.size(jsonFile)).isGreaterThan(0);
            
            String content = Files.readString(jsonFile);
            assertThat(content)
                .contains("\"division\"")
                .contains("\"reportDate\"")
                .contains("\"list1\"");
        }
    }

    @Test
    @DisplayName("Звіти містять інформацію про командирів")
    void shouldIncludeCommanderInformation() throws IOException {
        // Given
        String reportFile = outputDir.resolve("commander-test.txt").toString();
        DivisionReportWriter writer = new DivisionReportWriter(referenceData);

        // When
        writer.write(reportFile, context);

        // Then
        String content = Files.readString(Path.of(reportFile));
        
        // Перевіряємо що є інформація про командирів
        assertThat(content).contains("1 мр");
        assertThat(content).contains("2 мр");
    }

    @Test
    @DisplayName("Звіти коректно обробляють порожні списки підрозділів")
    void shouldHandleEmptyUnitLists() throws IOException {
        // Given
        EngagementContext emptyContext = new EngagementContext();
        emptyContext.setReportDate("15.05.2024");
        emptyContext.addUnitToDivision("Порожній підрозділ", 
            new BattleUnit("майор", "Тестовий", "100", false, null, null));
        
        String reportFile = outputDir.resolve("empty-test.txt").toString();
        DivisionReportWriter writer = new DivisionReportWriter(referenceData);

        // When
        writer.write(reportFile, emptyContext);

        // Then
        assertThat(Path.of(reportFile)).exists();
        String content = Files.readString(Path.of(reportFile));
        assertThat(content).contains("Порожній підрозділ");
    }

    @Test
    @DisplayName("Перевірка форматування приданих підрозділів")
    void shouldFormatAddedUnitsCorrectly() throws IOException {
        // Given
        String reportFile = outputDir.resolve("added-units-test.txt").toString();
        DivisionReportWriter writer = new DivisionReportWriter(referenceData);

        // When
        writer.write(reportFile, context);

        // Then
        String content = Files.readString(Path.of(reportFile));
        
        // Приданий підрозділ має містити відомості про джерело
        assertThat(content).contains("Приданий особовий склад");
    }

    @Test
    @DisplayName("JSON звіти містять всі обов'язкові поля")
    void shouldIncludeAllRequiredFieldsInJson() throws IOException {
        // Given
        String baseReportFile = outputDir.resolve("json/complete").toString();
        JsonReportWriter writer = new JsonReportWriter(referenceData);

        // When
        writer.write(baseReportFile, context);

        // Then
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());

        assertThat(jsonFiles).isNotEmpty();
        
        for (Path jsonFile : jsonFiles) {
            String content = Files.readString(jsonFile);
            
            // Перевіряємо всі обов'язкові поля
            assertThat(content)
                .contains("\"division\"")
                .contains("\"reportDate\"")
                .contains("\"list1\"")
                .contains("\"list2\"")
                .contains("\"list3\"")
                .contains("\"list4\"")
                .contains("\"list5\"")
                .contains("\"list6\"")
                .contains("\"position\"")
                .contains("\"rank\"")
                .contains("\"name\"")
                .contains("\"number\"");
        }
    }

    @Test
    @DisplayName("Паралельна генерація текстового та JSON звітів")
    void shouldGenerateBothReportTypesConsistently() throws IOException {
        // Given
        String textReportFile = outputDir.resolve("parallel-test.txt").toString();
        String jsonReportFile = outputDir.resolve("json/parallel").toString();
        
        DivisionReportWriter textWriter = new DivisionReportWriter(referenceData);
        JsonReportWriter jsonWriter = new JsonReportWriter(referenceData);

        // When
        textWriter.write(textReportFile, context);
        jsonWriter.write(jsonReportFile, context);

        // Then
        assertThat(Path.of(textReportFile)).exists();
        
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());
        assertThat(jsonFiles).isNotEmpty();

        // Обидва типи звітів мають містити ті самі підрозділи
        String textContent = Files.readString(Path.of(textReportFile));
        assertThat(textContent).contains("1 мр");
        assertThat(textContent).contains("2 мр");
    }

    /**
     * Створює тестовий контекст з даними
     */
    private EngagementContext createTestContext() throws IOException {
        EngagementContext ctx = new EngagementContext();
        ctx.setReportDate("15.05.2024");

        // Додаємо підрозділи для 1 мр
        BattleUnit unit1 = new BattleUnit("майор", "Іванов Іван", "70", false, null, null);
        BattleUnit unit2 = new BattleUnit("капітан", "Петров Петро", "100", false, null, null);
        BattleUnit unit3 = new BattleUnit("лейтенант", "Сидоренко Сергій", "30", false, null, null);
        
        ctx.addUnitToDivision("1 мр", unit1);
        ctx.addUnitToDivision("1 мр", unit2);
        ctx.addUnitToDivision("1 мр", unit3);

        // Додаємо підрозділи для 2 мр
        BattleUnit unit4 = new BattleUnit("підполковник", "Коваленко Костянтин", "70", false, null, null);
        BattleUnit unit5 = new BattleUnit("майор", "Мельник Михайло", "100", true, "1 мр", "2 мр");
        
        ctx.addUnitToDivision("2 мр", unit4);
        ctx.addUnitToDivision("2 мр", unit5);

        // Інформація про командирів
        ctx.getCommanderRank().put("1 мр", "підполковник");
        ctx.getCommanderName().put("1 мр", "Іванов І.І.");
        ctx.getCommanderRank().put("2 мр", "полковник");
        ctx.getCommanderName().put("2 мр", "Петров П.П.");

        // Створюємо тестові дані для номерів звітів
        createTestNumbersData(ctx);

        return ctx;
    }

    private void createTestNumbersData(EngagementContext ctx) {
        var numbersRow1 = new org.ombr24.mb1.domain.ParsedNumbersRow();
        numbersRow1.setReportNr("001");
        numbersRow1.setReportDate("15.05.2024");
        numbersRow1.setDivision("1 мр");

        var numbersRow2 = new org.ombr24.mb1.domain.ParsedNumbersRow();
        numbersRow2.setReportNr("002");
        numbersRow2.setReportDate("15.05.2024");
        numbersRow2.setDivision("2 мр");

        ctx.setParsedNumberList(List.of(numbersRow1, numbersRow2));
    }

    private void createReferenceFiles() throws IOException {
        Files.write(configDir.resolve("positions70.txt"), 
            List.of("70", "71", "72"));

        Files.write(configDir.resolve("positions100.txt"), 
            List.of("100", "101", "102"));

        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир 1 мр",
            "2 мр:Командир 2 мр"
        ));
    }
}
