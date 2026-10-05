package org.ombr24.mb1.integration;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.ombr24.mb1.config.*;
import org.ombr24.mb1.utils.XlsProcessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * Інтеграційні тести для повного циклу обробки Excel файлів
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class XlsProcessorIntegrationTest {

    @TempDir
    Path tempDir;

    private AppConfig config;
    private ReferenceData referenceData;
    private Path dataDir;
    private Path outputDir;
    private Path configDir;

    @BeforeEach
    void setUp() throws IOException {
        // Створюємо структуру директорій
        dataDir = tempDir.resolve("data");
        outputDir = tempDir.resolve("output");
        configDir = tempDir.resolve("config");

        Files.createDirectories(dataDir);
        Files.createDirectories(outputDir);
        Files.createDirectories(outputDir.resolve("json"));
        Files.createDirectories(configDir);

        // Ініціалізація конфігурації
        config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // Створюємо довідкові файли
        createReferenceFiles();

        // Завантажуємо довідкові дані
        referenceData = new ReferenceDataLoader().load(config);
    }

    @Test
    @Order(1)
    @DisplayName("Повний цикл: створення Excel → обробка → генерація звітів")
    void shouldProcessCompleteWorkflow() throws IOException {
        // Given: створюємо тестові Excel файли
        createTestExcelFiles();

        // When: обробляємо дані за 15 число
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then: перевіряємо що створені файли звітів
        Path textReport = outputDir.resolve("15травня.txt");
        assertThat(textReport).exists();
        assertThat(Files.size(textReport)).isGreaterThan(0);

        // Перевіряємо JSON звіти
        Path jsonDir = outputDir.resolve("json");
        assertThat(jsonDir).isDirectory();
        
        List<Path> jsonFiles = Files.list(jsonDir)
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());
        assertThat(jsonFiles).isNotEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("Обробка кількох днів підряд")
    void shouldProcessMultipleDays() throws IOException {
        // Given
        createTestExcelFiles();

        // When: обробляємо 3 дні (15, 16, 17)
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        for (int day = 15; day <= 17; day++) {
            processor.process(day);
        }

        // Then: перевіряємо що створені звіти для кожного дня
        assertThat(outputDir.resolve("15травня.txt")).exists();
        assertThat(outputDir.resolve("16травня.txt")).exists();
        assertThat(outputDir.resolve("17травня.txt")).exists();

        // Перевіряємо кількість JSON файлів (3 дні * 3 підрозділи)
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());
        assertThat(jsonFiles.size()).isGreaterThanOrEqualTo(9);
    }

    @Test
    @Order(3)
    @DisplayName("Обробка з приданими підрозділами")
    void shouldProcessAddedUnits() throws IOException {
        // Given
        createTestExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then: перевіряємо вміст текстового звіту
        Path textReport = outputDir.resolve("15травня.txt");
        String content = Files.readString(textReport);

        // Має містити секції з приданими підрозділами
        assertThat(content).contains("Приданий особовий склад");
        assertThat(content).contains("1 мр");
        assertThat(content).contains("2 мр");
        assertThat(content).contains("3 мр");
    }

    @Test
    @Order(4)
    @DisplayName("Валідація структури текстового звіту")
    void shouldGenerateCorrectTextReportStructure() throws IOException {
        // Given
        createTestExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = outputDir.resolve("15травня.txt");
        String content = Files.readString(textReport);

        // Перевіряємо структуру звіту
        assertThat(content).contains("Особовий склад підрозділу:");
        assertThat(content).contains("БП «70» (приведені):");
        assertThat(content).contains("100% (облікові):");
        assertThat(content).contains("30% (приведені):");
        assertThat(content).contains("_____________________________________________________");
    }

    @Test
    @Order(5)
    @DisplayName("Валідація структури JSON звіту")
    void shouldGenerateCorrectJsonReportStructure() throws IOException {
        // Given
        createTestExcelFiles();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        List<Path> jsonFiles = Files.list(outputDir.resolve("json"))
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());

        assertThat(jsonFiles).isNotEmpty();

        // Перевіряємо вміст першого JSON файлу
        Path firstJson = jsonFiles.get(0);
        String jsonContent = Files.readString(firstJson);

        // JSON має містити основні поля
        assertThat(jsonContent).contains("\"division\"");
        assertThat(jsonContent).contains("\"reportDate\"");
        assertThat(jsonContent).contains("\"list1\"");
        assertThat(jsonContent).contains("\"list2\"");
        assertThat(jsonContent).contains("\"position\"");
        assertThat(jsonContent).contains("\"rank\"");
        assertThat(jsonContent).contains("\"name\"");
    }

    @Test
    @Order(6)
    @DisplayName("Обробка порожніх позицій")
    void shouldHandleEmptyPositions() throws IOException {
        // Given: створюємо Excel з порожніми рядками
        createExcelWithEmptyRows();

        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        
        // Then: не має кидати exception
        assertThatCode(() -> processor.process(15))
            .doesNotThrowAnyException();
    }

    /**
     * Створює тестові Excel файли з даними
     */
    private void createTestExcelFiles() throws IOException {
        createMainExcelFile();
        createNumbersExcelFile();
    }

    /**
     * Створює основний Excel файл з даними про підрозділи
     */
    private void createMainExcelFile() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.MAY, 15);

            // Створюємо листи для 3 підрозділів
            String[] divisions = {"1 мр", "2 мр", "3 мр"};
            
            for (String division : divisions) {
                Sheet sheet = workbook.createSheet(division);
                
                // Заголовковий рядок з датами
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

                // Рядок командира
                Row commanderRow = sheet.createRow(1);
                commanderRow.createCell(0).setCellValue("підполковник");
                commanderRow.createCell(1).setCellValue("Іванов Іван");
                commanderRow.createCell(15).setCellValue("100");

                // Додаємо тестові дані підрозділів
                addTestUnits(sheet, workbook);
            }

            // Лист з приданими підрозділами
            Sheet addedSheet = workbook.createSheet("Придані");
            createAddedUnitsSheet(addedSheet, workbook);

            // Зберігаємо файл
            try (FileOutputStream fos = new FileOutputStream(dataDir.resolve("1.xlsx").toFile())) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Додає тестові підрозділи на лист
     */
    private void addTestUnits(Sheet sheet, Workbook workbook) {
        String[][] testData = {
            {"майор", "Петров Петро", "70"},
            {"капітан", "Сидоренко Сергій", "100"},
            {"підполковник", "Коваленко Костянтин", "30"},
            {"капітан", "Мельник Михайло", "70"},
            {"майор", "Шевченко Шевченко", "100"}
        };

        int rowNum = 2;
        for (String[] unitData : testData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(unitData[0]); // звання
            row.createCell(1).setCellValue(unitData[1]); // ім'я
            row.createCell(15).setCellValue(unitData[2]); // позиція для 15 числа
        }
    }

    /**
     * Створює лист з приданими підрозділами
     */
    private void createAddedUnitsSheet(Sheet sheet, Workbook workbook) {
        Row headerRow = sheet.createRow(0);
        for (int day = 1; day <= 31; day++) {
            headerRow.createCell(day).setCellValue(day);
        }

        // Додаємо 2 приданих підрозділи
        Row row1 = sheet.createRow(2);
        row1.createCell(1).setCellValue("Петров Петро"); // ім'я
        row1.createCell(15).setCellValue("1 мр"); // звідки
        row1.createCell(16).setCellValue("2 мр"); // куди

        Row row2 = sheet.createRow(3);
        row2.createCell(1).setCellValue("Мельник Михайло");
        row2.createCell(15).setCellValue("2 мр");
        row2.createCell(16).setCellValue("3 мр");
    }

    /**
     * Створює файл з номерами звітів
     */
    private void createNumbersExcelFile() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Numbers");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.MAY, 15);

            String[] divisions = {"1 мр", "2 мр", "3 мр"};
            
            int rowNum = 0;
            for (int day = 15; day <= 20; day++) {
                for (String division : divisions) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(String.format("%03d-%s", day, division));
                    
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

    /**
     * Створює Excel з порожніми рядками для тестування обробки порожніх даних
     */
    private void createExcelWithEmptyRows() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("1 мр");
            
            // Заголовковий рядок
            Row headerRow = sheet.createRow(0);
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.MAY, 15);
            Cell cell = headerRow.createCell(15);
            cell.setCellValue(cal.getTime());

            // Рядок командира
            Row commanderRow = sheet.createRow(1);
            commanderRow.createCell(0).setCellValue("підполковник");
            commanderRow.createCell(1).setCellValue("Тестовий Тест");
            commanderRow.createCell(15).setCellValue("100");

            // Порожні рядки
            sheet.createRow(2);
            sheet.createRow(3);
            
            // Один валідний рядок
            Row validRow = sheet.createRow(4);
            validRow.createCell(0).setCellValue("капітан");
            validRow.createCell(1).setCellValue("Іванов І.");
            validRow.createCell(15).setCellValue("70");

            try (FileOutputStream fos = new FileOutputStream(dataDir.resolve("1.xlsx").toFile())) {
                workbook.write(fos);
            }
        }

        createNumbersExcelFile();
    }

    /**
     * Створює довідкові файли конфігурації
     */
    private void createReferenceFiles() throws IOException {
        // positions70.txt
        Files.write(configDir.resolve("positions70.txt"), 
            List.of("70", "71", "72", "73", "74", "75"));

        // positions100.txt
        Files.write(configDir.resolve("positions100.txt"), 
            List.of("100", "101", "102", "103"));

        // commanders.txt
        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир 1 мр",
            "2 мр:Командир 2 мр",
            "3 мр:Командир 3 мр"
        ));
    }
}
