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
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

/**
 * End-to-End інтеграційні тести для повного життєвого циклу додатку
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EndToEndIntegrationTest {

    @TempDir
    Path tempDir;

    private AppConfig config;
    private ReferenceData referenceData;

    @BeforeEach
    void setUp() throws IOException {
        // Створюємо повну структуру проекту
        Path dataDir = tempDir.resolve("data");
        Path outputDir = tempDir.resolve("output");
        Path configDir = tempDir.resolve("config");

        Files.createDirectories(dataDir);
        Files.createDirectories(outputDir);
        Files.createDirectories(outputDir.resolve("json"));
        Files.createDirectories(configDir);

        config = new AppConfig(
            dataDir.toString(),
            outputDir.toString(),
            configDir.toString()
        );

        // Створюємо всі необхідні файли
        createCompleteProjectStructure();
        
        // Завантажуємо конфігурацію
        referenceData = new ReferenceDataLoader().load(config);
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Повний цикл від Excel до звітів")
    void shouldCompleteFullProcessingCycle() throws IOException {
        // Given: всі файли створені в setUp()
        
        // When: обробляємо дані
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then: перевіряємо всі результати
        
        // 1. Текстовий звіт створено
        Path textReport = Path.of(config.divisionReport(15));
        assertThat(textReport).exists();
        assertThat(Files.size(textReport)).isGreaterThan(0);

        // 2. JSON звіти створено
        Path jsonDir = Path.of(config.divisionJsonReport(15));
        List<Path> jsonFiles = Files.list(jsonDir)
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());
        assertThat(jsonFiles).hasSizeGreaterThanOrEqualTo(3);

        // 3. Вміст звітів коректний
        String textContent = Files.readString(textReport);
        assertThat(textContent)
            .contains("1 мр")
            .contains("2 мр")
            .contains("3 мр")
            .contains("Особовий склад підрозділу:")
            .contains("Приданий особовий склад");

        // 4. JSON файли містять валідний JSON
        for (Path jsonFile : jsonFiles) {
            String jsonContent = Files.readString(jsonFile);
            assertThat(jsonContent)
                .startsWith("{")
                .endsWith("}")
                .contains("\"division\"")
                .contains("\"reportDate\"");
        }
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Обробка місячного періоду")
    void shouldProcessEntireMonth() throws IOException {
        // Given
        int startDay = 1;
        int endDay = 5;

        // When: обробляємо 5 днів
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        for (int day = startDay; day <= endDay; day++) {
            processor.process(day);
        }

        // Then: перевіряємо що створені звіти для всіх днів
        for (int day = startDay; day <= endDay; day++) {
            Path textReport = Path.of(config.divisionReport(day));
            assertThat(textReport)
                .withFailMessage("Звіт за %d число не створено", day)
                .exists();
        }

        // JSON звіти для всіх днів
        Path jsonDir = Path.of(config.divisionJsonReport(1));
        List<Path> allJsonFiles = Files.list(jsonDir)
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());
        
        // 5 днів × 3 підрозділи = 15 файлів мінімум
        assertThat(allJsonFiles.size()).isGreaterThanOrEqualTo(15);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Обробка з різними типами позицій")
    void shouldHandleAllPositionTypes() throws IOException {
        // Given: Excel з різними типами позицій (70, 100, 30, ТЗ)
        
        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = Path.of(config.divisionReport(15));
        String content = Files.readString(textReport);

        // Перевіряємо всі секції позицій
        assertThat(content)
            .contains("БП «70» (приведені):")
            .contains("100% (облікові):")
            .contains("30% (приведені):");
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Обробка приданих підрозділів між дивізіями")
    void shouldHandleUnitTransfersBetweenDivisions() throws IOException {
        // Given: Excel з приданими підрозділами
        
        // When
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // Then
        Path textReport = Path.of(config.divisionReport(15));
        String content = Files.readString(textReport);

        // Перевіряємо що придані підрозділи відображаються
        assertThat(content).contains("Приданий особовий склад");
        
        // JSON має містити список приданих підрозділів
        Path jsonDir = Path.of(config.divisionJsonReport(15));
        List<Path> jsonFiles = Files.list(jsonDir)
            .filter(p -> p.toString().endsWith(".json"))
            .collect(Collectors.toList());

        boolean hasAddedUnits = false;
        for (Path jsonFile : jsonFiles) {
            String jsonContent = Files.readString(jsonFile);
            if (jsonContent.contains("\"list2\"") && !jsonContent.contains("\"list2\": []")) {
                hasAddedUnits = true;
                break;
            }
        }
        
        assertThat(hasAddedUnits)
            .withFailMessage("JSON звіти не містять приданих підрозділів")
            .isTrue();
    }

    @Test
    @Order(5)
    @DisplayName("E2E: Перевірка цілісності даних")
    void shouldMaintainDataIntegrity() throws IOException {
        // Given
        XlsProcessor processor = new XlsProcessor(config, referenceData);
        processor.process(15);

        // When: читаємо всі згенеровані звіти
        Path textReport = Path.of(config.divisionReport(15));
        String textContent = Files.readString(textReport);

        Path jsonDir = Path.of(config.divisionJsonReport(15));
        List<String> jsonContents = Files.list(jsonDir)
            .filter(p -> p.toString().endsWith(".json"))
            .map(path -> {
                try {
                    return Files.readString(path);
                } catch (IOException e) {
                    return "";
                }
            })
            .collect(Collectors.toList());

        // Then: перевіряємо консистентність
        // Кожен підрозділ з текстового звіту має бути в JSON
        String[] divisions = {"1 мр", "2 мр", "3 мр"};
        for (String division : divisions) {
            assertThat(textContent)
                .withFailMessage("Підрозділ %s відсутній в текстовому звіті", division)
                .contains(division);

            boolean foundInJson = jsonContents.stream()
                .anyMatch(json -> json.contains("\"division\": \"" + division + "\""));
            
            assertThat(foundInJson)
                .withFailMessage("Підрозділ %s відсутній в JSON звітах", division)
                .isTrue();
        }
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Продуктивність обробки")
    void shouldProcessDataWithinReasonableTime() throws IOException {
        // Given
        XlsProcessor processor = new XlsProcessor(config, referenceData);

        // When
        long startTime = System.currentTimeMillis();
        processor.process(15);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Then: обробка має завершитись менш ніж за 5 секунд
        assertThat(duration)
            .withFailMessage("Обробка зайняла %d мс, що перевищує очікувані 5000 мс", duration)
            .isLessThan(5000);
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Ідемпотентність обробки")
    void shouldBeIdempotent() throws IOException {
        // Given
        XlsProcessor processor = new XlsProcessor(config, referenceData);

        // When: обробляємо дані двічі
        processor.process(15);
        String firstRunText = Files.readString(Path.of(config.divisionReport(15)));
        
        // Очищаємо вихідні файли
        Files.delete(Path.of(config.divisionReport(15)));
        Files.list(Path.of(config.divisionJsonReport(15)))
            .filter(p -> p.toString().endsWith(".json"))
            .forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    // ignore
                }
            });

        processor.process(15);
        String secondRunText = Files.readString(Path.of(config.divisionReport(15)));

        // Then: результати мають бути однаковими
        assertThat(secondRunText).isEqualTo(firstRunText);
    }

    /**
     * Створює повну структуру проекту з усіма необхідними файлами
     */
    private void createCompleteProjectStructure() throws IOException {
        createMainExcelFile();
        createNumbersExcelFile();
        createReferenceFiles();
    }

    private void createMainExcelFile() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.MAY, 1);

            String[] divisions = {"1 мр", "2 мр", "3 мр"};
            
            for (String division : divisions) {
                Sheet sheet = workbook.createSheet(division);
                
                // Заголовок з датами (всі дні місяця)
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
                
                // Позиція командира для всіх днів
                for (int day = 1; day <= 31; day++) {
                    commanderRow.createCell(day).setCellValue("100");
                }

                // Підрозділи
                addRealisticTestData(sheet, division);
            }

            // Лист з приданими підрозділами
            createAddedUnitsSheet(workbook);

            // Зберігаємо файл
            Path excelFile = Path.of(config.getDataDir(), "1.xlsx");
            try (FileOutputStream fos = new FileOutputStream(excelFile.toFile())) {
                workbook.write(fos);
            }
        }
    }

    private void addRealisticTestData(Sheet sheet, String division) {
        // Різні підрозділи для кожної дивізії
        String[][] baseData = {
            {"майор", "Петров Петро", "70"},
            {"капітан", "Сидоренко Сергій", "71"},
            {"підполковник", "Коваленко Костянтин", "100"},
            {"лейтенант", "Мельник Михайло", "101"},
            {"капітан", "Шевченко Тарас", "30"},
            {"майор", "Бондаренко Борис", "72"},
            {"підполковник", "Ткаченко Олег", "102"}
        };

        int rowNum = 2;
        for (String[] unitData : baseData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(unitData[0]);
            row.createCell(1).setCellValue(unitData[1] + " (" + division + ")");
            
            // Позиції для всіх днів місяця
            for (int day = 1; day <= 31; day++) {
                row.createCell(day).setCellValue(unitData[2]);
            }
        }
    }

    private void createAddedUnitsSheet(Workbook workbook) {
        Sheet sheet = workbook.createSheet("Придані");
        
        Row headerRow = sheet.createRow(0);
        for (int day = 1; day <= 31; day++) {
            headerRow.createCell(day).setCellValue(day);
        }

        // Приданий підрозділ: з 1 мр до 2 мр
        Row row1 = sheet.createRow(2);
        row1.createCell(1).setCellValue("Петров Петро (1 мр)");
        for (int day = 1; day <= 31; day++) {
            row1.createCell(day + 31).setCellValue("1 мр");  // AddedFrom
            row1.createCell(day + 62).setCellValue("2 мр");  // AddedTo
        }

        // Приданий підрозділ: з 2 мр до 3 мр
        Row row2 = sheet.createRow(3);
        row2.createCell(1).setCellValue("Мельник Михайло (2 мр)");
        for (int day = 1; day <= 31; day++) {
            row2.createCell(day + 31).setCellValue("2 мр");
            row2.createCell(day + 62).setCellValue("3 мр");
        }
    }

    private void createNumbersExcelFile() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Numbers");
            
            Calendar cal = Calendar.getInstance();
            cal.set(2024, Calendar.MAY, 1);

            String[] divisions = {"1 мр", "2 мр", "3 мр"};
            
            int rowNum = 0;
            for (int day = 1; day <= 31; day++) {
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

            Path numbersFile = Path.of(config.getDataDir(), "numbers.xlsx");
            try (FileOutputStream fos = new FileOutputStream(numbersFile.toFile())) {
                workbook.write(fos);
            }
        }
    }

    private void createReferenceFiles() throws IOException {
        Path configDir = Path.of(config.divisionReport(1)).getParent().getParent().resolve("config");
        
        Files.write(configDir.resolve("positions70.txt"), 
            List.of("70", "71", "72", "73", "74", "75"));

        Files.write(configDir.resolve("positions100.txt"), 
            List.of("100", "101", "102", "103", "104"));

        Files.write(configDir.resolve("commanders.txt"), List.of(
            "1 мр:Командир першого моторизованого батальйону",
            "2 мр:Командир другого моторизованого батальйону",
            "3 мр:Командир третього моторизованого батальйону"
        ));
    }
}
