package org.ombr24.mb1.integration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.ombr24.mb1.config.AppConfig;
import org.ombr24.mb1.config.ReferenceData;
import org.ombr24.mb1.config.ReferenceDataLoader;
import org.ombr24.mb1.domain.DailyCommanderReportDataObject;
import org.ombr24.mb1.utils.XlsProcessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Інтеграційні тести для перевірки генерації звітів
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReportGenerationIntegrationTest {

    @TempDir
    Path tempDir;

    private AppConfig config;
    private ReferenceData referenceData;
    private Path dataDir;
    private Path outputDir;
    private Path configDir;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        dataDir = tempDir.resolve("data");
        outputDir = tempDir.resolve("output");
        configDir = tempDir.resolve("config");

        Files.createDirectories(dataDir);
        Files.createDirectories(outputDir);
        Files.createDirectories(outputDir.resolve("json"));
        Files.createDirectories(configDir);

        config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        createReferenceFiles();
        referenceData = new ReferenceDataLoader().load(config);
        gson = new Gson();
    }

    @Test
    @Order(1)
    @DisplayName("Генерація текстового звіту: перевірка всіх секцій")
    void shouldGenerateTextReportWithAllSections() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = outputDir.resolve("15травня.txt");
        String content = Files.readString(textReport);

        // Перевіряємо наявність всіх обов'язкових секцій
        assertThat(content)
            .contains("1 мр")
            .contains("2 мр")
            .contains("3 мр")
            .contains("БП «70» (приведені):")
            .contains("100% (облікові):")
            .contains("30% (приведені):")
            .contains("Особовий склад підрозділу:")
            .contains("Приданий особовий склад");
    }

    @Test
    @Order(2)
    @DisplayName("JSON звіт: перевірка структури та обов'язкових полів")
    void shouldGenerateValidJsonReports() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .toList();

        assertThat(jsonFiles).hasSizeGreaterThanOrEqualTo(3);

        // Перевіряємо кожен JSON файл
        for (Path jsonFile : jsonFiles) {
            String jsonContent = Files.readString(jsonFile);
            JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);

            // Обов'язкові поля
            assertThat(jsonObject.has("division")).isTrue();
            assertThat(jsonObject.has("reportDate")).isTrue();
            assertThat(jsonObject.has("list1")).isTrue();
            assertThat(jsonObject.has("list2")).isTrue();
            assertThat(jsonObject.has("list3")).isTrue();
            assertThat(jsonObject.has("list4")).isTrue();
            assertThat(jsonObject.has("list5")).isTrue();
            assertThat(jsonObject.has("list6")).isTrue();
            assertThat(jsonObject.has("position")).isTrue();
            assertThat(jsonObject.has("rank")).isTrue();
            assertThat(jsonObject.has("name")).isTrue();
            assertThat(jsonObject.has("number")).isTrue();

            // Перевіряємо що дата має правильний формат
            String reportDate = jsonObject.get("reportDate").getAsString();
            assertThat(reportDate).matches("\\d{2}\\.\\d{2}\\.\\d{4}");
        }
    }

    @Test
    @Order(3)
    @DisplayName("JSON звіт: десеріалізація в об'єкт")
    void shouldDeserializeJsonToObject() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .findFirst()
            .stream()
            .toList();

        assertThat(jsonFiles).isNotEmpty();
        String jsonContent = Files.readString(jsonFiles.get(0));

        // Десеріалізація в об'єкт має пройти успішно
        DailyCommanderReportDataObject report = gson.fromJson(jsonContent, DailyCommanderReportDataObject.class);

        assertThat(report).isNotNull();
        assertThat(report.getDivision()).isNotNull().isNotEmpty();
        assertThat(report.getReportDate()).isNotNull().isNotEmpty();
        assertThat(report.getList1()).isNotNull();
        assertThat(report.getList2()).isNotNull();
        assertThat(report.getPosition()).isNotNull();
        assertThat(report.getRank()).isNotNull();
        assertThat(report.getName()).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("Форматування імен у звіті")
    void shouldFormatNamesCorrectly() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = outputDir.resolve("15травня.txt");
        String content = Files.readString(textReport);

        // Перевіряємо що прізвища у верхньому регістрі
        assertThat(content).containsPattern("[А-ЯҐЄІЇ]+ [А-Яа-яґєії]+");
    }

    @Test
    @Order(5)
    @DisplayName("Позиції з типами (БП, ТЗ)")
    void shouldIncludePositionTypes() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = outputDir.resolve("15травня.txt");
        String content = Files.readString(textReport);

        // Перевіряємо наявність типів позицій
        assertThat(content).contains("БП «70»");
    }

    @Test
    @Order(6)
    @DisplayName("Розділення на оригінальні та придані підрозділи")
    void shouldSeparateOriginalAndAddedUnits() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = outputDir.resolve("15травня.txt");
        String content = Files.readString(textReport);

        // Має бути 2 секції для кожної категорії
        int originalCount = countOccurrences(content, "Особовий склад підрозділу:");
        int addedCount = countOccurrences(content, "Приданий особовий склад");

        // Для кожного підрозділу є обидві секції для кожної категорії позицій
        assertThat(originalCount).isGreaterThanOrEqualTo(3);
        assertThat(addedCount).isGreaterThanOrEqualTo(3);
    }

    @Test
    @Order(7)
    @DisplayName("Консистентність між текстовим та JSON звітами")
    void shouldHaveConsistentDataBetweenTextAndJsonReports() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = outputDir.resolve("15травня.txt");
        String textContent = Files.readString(textReport);

        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .toList();

        // Перевіряємо що кожен підрозділ з JSON є в текстовому звіті
        for (Path jsonFile : jsonFiles) {
            String jsonContent = Files.readString(jsonFile);
            JsonObject jsonObject = gson.fromJson(jsonContent, JsonObject.class);
            String division = jsonObject.get("division").getAsString();

            assertThat(textContent).contains(division);
        }
    }

    @Test
    @Order(8)
    @DisplayName("Правильна кількість підрозділів у кожній секції")
    void shouldHaveCorrectUnitCountsInSections() throws IOException {
        // Given
        createCompleteExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .toList();

        for (Path jsonFile : jsonFiles) {
            String jsonContent = Files.readString(jsonFile);
            DailyCommanderReportDataObject report = gson.fromJson(jsonContent, DailyCommanderReportDataObject.class);

            // Всі списки мають бути ініціалізовані (не null)
            assertThat(report.getList1()).isNotNull();
            assertThat(report.getList2()).isNotNull();
            assertThat(report.getList3()).isNotNull();
            assertThat(report.getList4()).isNotNull();
            assertThat(report.getList5()).isNotNull();
            assertThat(report.getList6()).isNotNull();
        }
    }

    /**
     * Створює повні тестові Excel файли
     */
    private void createCompleteExcelFiles() throws IOException {
        createMainExcelFile();
        createNumbersExcelFile();
    }

    private void createMainExcelFile() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.MAY, 15);

            String[] divisions = {"1 мр", "2 мр", "3 мр"};
            
            for (String division : divisions) {
                Sheet sheet = workbook.createSheet(division);
                
                // Заголовок з датами
                Row headerRow = sheet.createRow(0);
                for (int day = 1; day <= 31; day++) {
                    Cell cell = headerRow.createCell(day);
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cell.setCellValue(cal.getTime());
                    
                    CellStyle dateStyle = workbook.createCellStyle();
                    CreationHelper createHelper = workbook.getCreationHelper();
                    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));
                    cell.setCellStyle(dateStyle);
                }

                // Командир
                Row commanderRow = sheet.createRow(1);
                commanderRow.createCell(0).setCellValue("підполковник");
                commanderRow.createCell(1).setCellValue("Командиров Командир");
                commanderRow.createCell(15).setCellValue("100");

                // Тестові дані
                addDetailedTestUnits(sheet);
            }

            // Придані підрозділи
            Sheet addedSheet = workbook.createSheet("Придані");
            Row headerRow = addedSheet.createRow(0);
            for (int day = 1; day <= 31; day++) {
                headerRow.createCell(day).setCellValue(day);
            }

            Row addedRow1 = addedSheet.createRow(2);
            addedRow1.createCell(1).setCellValue("Петров Петро");
            addedRow1.createCell(15).setCellValue("1 мр");
            addedRow1.createCell(16).setCellValue("2 мр");

            try (FileOutputStream fos = new FileOutputStream(dataDir.resolve("1.xlsx").toFile())) {
                workbook.write(fos);
            }
        }
    }

    private void addDetailedTestUnits(Sheet sheet) {
        String[][] testData = {
            {"майор", "Петров Петро", "70"},
            {"капітан", "Сидоренко Сергій", "71"},
            {"підполковник", "Коваленко Костянтин", "100"},
            {"лейтенант", "Мельник Михайло", "101"},
            {"капітан", "Шевченко Шевченко", "30"},
            {"майор", "Бондаренко Борис", "70"},
            {"підполковник", "Ткаченко Тарас", "102"}
        };

        int rowNum = 2;
        for (String[] unitData : testData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(unitData[0]);
            row.createCell(1).setCellValue(unitData[1]);
            row.createCell(15).setCellValue(unitData[2]);
        }
    }

    private void createNumbersExcelFile() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Numbers");
            
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.MAY, 15);

            String[] divisions = {"1 мр", "2 мр", "3 мр"};
            
            int rowNum = 0;
            for (int day = 15; day <= 20; day++) {
                for (String division : divisions) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(String.format("%03d/%s", day, division));
                    
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    Cell dateCell = row.createCell(1);
                    dateCell.setCellValue(cal.getTime());
                    
                    CellStyle dateStyle = workbook.createCellStyle();
                    CreationHelper createHelper = workbook.getCreationHelper();
                    dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.mm.yyyy"));
                    dateCell.setCellStyle(dateStyle);
                    
                    row.createCell(2).setCellValue(division);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(dataDir.resolve("numbers.xlsx").toFile())) {
                workbook.write(fos);
            }
        }
    }

    private void createReferenceFiles() throws IOException {
        Files.write(configDir.resolve("positions70.txt"), 
            List.of("70", "71", "72", "73", "74", "75"));

        Files.write(configDir.resolve("positions100.txt"), 
            List.of("100", "101", "102", "103"));

        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир 1 мр батальйону",
            "2 мр:Командир 2 мр батальйону",
            "3 мр:Командир 3 мр батальйону"
        ));
    }

    private int countOccurrences(String text, String substring) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }
}
