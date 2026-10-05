package org.ombr24.mb1.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ombr24.mb1.config.*;
import org.ombr24.mb1.domain.*;
import org.ombr24.mb1.excel.AddedUnitMapper;
import org.ombr24.mb1.excel.ExcelCellReader;
import org.ombr24.mb1.excel.ExcelRowMapper;
import org.ombr24.mb1.exception.DataProcessingException;
import org.ombr24.mb1.exception.FileOperationException;
import org.ombr24.mb1.report.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Процесор для обробки Excel файлів з бойовими звітами
 */
public class XlsProcessor {

    private static final Logger logger = LoggerFactory.getLogger(XlsProcessor.class);

    private final AppConfig config;
    private final ReferenceData referenceData;
    private final EngagementContext context = new EngagementContext();
    private final ExcelRowMapper excelRowMapper = new ExcelRowMapper();
    private final AddedUnitMapper addedUnitMapper = new AddedUnitMapper();
    private final ExcelCellReader cellReader = new ExcelCellReader();

    public XlsProcessor(AppConfig config, ReferenceData referenceData) {
        this.config = config;
        this.referenceData = referenceData;
    }

    /**
     * Обробляє дані за вказаний день місяця
     */
    public void process(int day) {
        logger.info("Початок обробки даних за {} число", day);

        processNumbersSheet(day);
        processMainDataSheets(day);
        generateReports(day);

        logger.info("Обробку даних за {} число завершено", day);
    }

    /**
     * Обробляє лист з номерами звітів
     */
    private void processNumbersSheet(int day) {
        String numbersFile = config.getDataDir() + "/" + Constants.NUMBERS_SHEET;
        logger.debug("Читання файлу номерів: {}", numbersFile);

        validateFileExists(numbersFile);

        try (XSSFWorkbook workbook = new XSSFWorkbook(numbersFile)) {
            List<ParsedNumbersRow> parsedNumbersList = parseNumbersRows(workbook.getSheetAt(0));
            context.setParsedNumberList(parsedNumbersList);
            logger.debug("Прочитано {} записів з номерами звітів", parsedNumbersList.size());

        } catch (IOException e) {
            throw new FileOperationException("Помилка читання файлу з номерами звітів: " + numbersFile, e);
        }
    }

    /**
     * Парсить рядки з листа номерів
     */
    private List<ParsedNumbersRow> parseNumbersRows(XSSFSheet sheet) {
        List<ParsedNumbersRow> parsedNumbersList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);

        for (Row row : sheet) {
            try {
                ParsedNumbersRow parsedNumbersRow = new ParsedNumbersRow();
                parsedNumbersRow.setReportNr(cellReader.getCellValueAsString(row, Constants.REPORT_NR_COLUMN));
                parsedNumbersRow.setReportDate(formatter.format(row.getCell(Constants.REPORT_DATE_COLUMN).getDateCellValue()));
                parsedNumbersRow.setDivision(cellReader.getCellValueAsString(row, Constants.DIVISION_COLUMN));
                parsedNumbersList.add(parsedNumbersRow);
            } catch (Exception e) {
                logger.warn("Не вдалося обробити рядок {}: {}", row.getRowNum(), e.getMessage());
            }
        }

        return parsedNumbersList;
    }

    /**
     * Обробляє основні листи з даними
     */
    private void processMainDataSheets(int day) {
        String inputFile = config.inputExcel();
        logger.debug("Читання основного файлу даних: {}", inputFile);

        validateFileExists(inputFile);

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputFile)) {
            Map<String, List<BattleUnit>> originalUnitsByDivision = new HashMap<>();
            Map<String, AddedBattleUnit> addedUnitsInfoMap = readAddedUnits(
                    workbook.getSheet(Constants.ADDED_UNITS_SHEET), day);
            Map<String, List<BattleUnit>> addedUnitsByDivision = new HashMap<>();

            String reportDate = extractReportDate(workbook, day);
            context.setReportDate(reportDate);
            logger.debug("Дата звіту: {}", reportDate);

            int processedSheets = 0;
            for (Sheet sheet : workbook) {
                if (Constants.SKIP_SHEETS.contains(sheet.getSheetName())) {
                    logger.trace("Пропущено лист: {}", sheet.getSheetName());
                    continue;
                }

                processSheet(sheet, day, addedUnitsInfoMap, originalUnitsByDivision);
                processedSheets++;
            }

            logger.debug("Оброблено {} листів", processedSheets);

            // Створити список підрозділів без доданих
            distributeUnits(originalUnitsByDivision, addedUnitsByDivision);

            // Додати придані підрозділи
            addAssignedUnits(addedUnitsByDivision);

        } catch (IOException e) {
            throw new FileOperationException("Помилка читання основного Excel файлу: " + inputFile, e);
        }
    }

    /**
     * Обробляє окремий лист Excel
     */
    private void processSheet(Sheet sheet, int day, Map<String, AddedBattleUnit> addedUnitsInfoMap,
                              Map<String, List<BattleUnit>> originalUnitsByDivision) {
        String sheetName = sheet.getSheetName();
        logger.trace("Обробка листа: {}", sheetName);

        // Читання інформації про командира
        Row commanderRow = sheet.getRow(Constants.COMMANDER_ROW);
        if (commanderRow != null) {
            String commanderRank = cellReader.getCellValueAsString(commanderRow, Constants.RANK_COLUMN);
            String commanderName = cellReader.getCellValueAsString(commanderRow, Constants.NAME_COLUMN);

            context.getCommanderRank().put(sheetName, commanderRank);
            context.getCommanderName().put(sheetName, formatName(commanderName));
        }

        // Обробка рядків з підрозділами
        int unitCount = 0;
        for (Row row : sheet) {
            if (row.getRowNum() == Constants.HEADER_ROW) continue;

            ParsedRow parsed = excelRowMapper.map(row, day + 1);
            if (parsed.isEmpty()) continue;

            BattleUnit unit = createBattleUnit(parsed, addedUnitsInfoMap);
            originalUnitsByDivision.computeIfAbsent(sheetName, k -> new ArrayList<>()).add(unit);
            context.addAssignment(parsed.getPosition(), unit);
            unitCount++;
        }

        logger.trace("Оброблено {} підрозділів на листі {}", unitCount, sheetName);
    }

    /**
     * Створює об'єкт бойового підрозділу
     */
    private BattleUnit createBattleUnit(ParsedRow parsed, Map<String, AddedBattleUnit> addedUnitsInfoMap) {
        boolean isAdded = addedUnitsInfoMap.containsKey(parsed.getName());
        String addedFrom = isAdded ? addedUnitsInfoMap.get(parsed.getName()).getAddedFrom() : null;
        String addedTo = isAdded ? addedUnitsInfoMap.get(parsed.getName()).getAddedTo() : null;

        return new BattleUnit(parsed.getRank(), parsed.getName(), parsed.getPosition(),
                isAdded, addedFrom, addedTo);
    }

    /**
     * Розподіляє підрозділи на оригінальні та придані
     */
    private void distributeUnits(Map<String, List<BattleUnit>> originalUnitsByDivision,
                                 Map<String, List<BattleUnit>> addedUnitsByDivision) {
        for (Map.Entry<String, List<BattleUnit>> entry : originalUnitsByDivision.entrySet()) {
            for (BattleUnit unit : entry.getValue()) {
                if (!unit.isAdded()) {
                    context.addUnitToDivision(entry.getKey(), unit);
                } else {
                    addedUnitsByDivision.computeIfAbsent(unit.getAddedTo(), k -> new ArrayList<>()).add(unit);
                }
            }
        }
    }

    /**
     * Додає придані підрозділи до відповідних дивізій
     */
    private void addAssignedUnits(Map<String, List<BattleUnit>> addedUnitsByDivision) {
        for (Map.Entry<String, List<BattleUnit>> entry : addedUnitsByDivision.entrySet()) {
            for (BattleUnit unit : entry.getValue()) {
                context.addUnitToDivision(entry.getKey(), unit);
            }
        }
    }

    /**
     * Витягує дату звіту з Excel
     */
    private String extractReportDate(XSSFWorkbook workbook, int day) {
        try {
            Sheet sheet = workbook.getSheet(Constants.FIRST_DATA_SHEET);
            if (sheet == null) {
                throw new DataProcessingException("Не знайдено лист: " + Constants.FIRST_DATA_SHEET);
            }

            Row headerRow = sheet.getRow(Constants.HEADER_ROW);
            if (headerRow == null) {
                throw new DataProcessingException("Не знайдено заголовковий рядок на листі: " + Constants.FIRST_DATA_SHEET);
            }

            Cell dateCell = headerRow.getCell(day + 1);
            if (dateCell == null) {
                throw new DataProcessingException("Не знайдено дату для дня: " + day);
            }

            Date date = dateCell.getDateCellValue();
            SimpleDateFormat formatter = new SimpleDateFormat(Constants.DATE_FORMAT);
            return formatter.format(date);

        } catch (Exception e) {
            throw new DataProcessingException("Помилка витягування дати звіту для дня " + day, e);
        }
    }

    /**
     * Генерує звіти на основі зібраних даних
     */
    private void generateReports(int day) {
        logger.debug("Генерація звітів за {} число...", day);

        try {
            new DivisionReportWriter(referenceData).write(config.divisionReport(day), context);
            logger.info("Згенеровано текстовий звіт: {}", config.divisionReport(day));

            new JsonReportWriter(referenceData).write(config.divisionJsonReport(day), context);
            logger.info("Згенеровано JSON звіти: {}", config.divisionJsonReport(day));

        } catch (Exception e) {
            throw new DataProcessingException("Помилка генерації звітів за день " + day, e);
        }
    }

    /**
     * Читає інформацію про придані підрозділи з окремого листа
     */
    private Map<String, AddedBattleUnit> readAddedUnits(XSSFSheet sheet, int day) {
        if (sheet == null) {
            logger.warn("Лист з приданими підрозділами не знайдено");
            return new HashMap<>();
        }

        Map<String, AddedBattleUnit> addedUnits = new HashMap<>();
        int unitCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == Constants.HEADER_ROW) continue;

            try {
                ParsedAddedUnitRow parsed = addedUnitMapper.map(row, day + 1);
                if (parsed.isEmpty()) continue;

                AddedBattleUnit unit = new AddedBattleUnit(
                        parsed.getName(), 
                        parsed.getAddedFrom(), 
                        parsed.getAddedTo()
                );

                addedUnits.put(unit.getName(), unit);
                unitCount++;

            } catch (Exception e) {
                logger.warn("Не вдалося обробити рядок {} на листі приданих підрозділів: {}", 
                        row.getRowNum(), e.getMessage());
            }
        }

        logger.debug("Прочитано {} приданих підрозділів", unitCount);
        return addedUnits;
    }

    /**
     * Форматує ім'я командира (прізвище + ініціал)
     */
    private String formatName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Отримано порожнє ім'я для форматування");
            return "";
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length < 2) {
            logger.warn("Некоректний формат імені: {}", name);
            return name;
        }

        return parts[1] + " " + parts[0].toUpperCase();
    }

    /**
     * Перевіряє існування файлу
     */
    private void validateFileExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileOperationException("Файл не існує: " + filePath);
        }
        if (!file.canRead()) {
            throw new FileOperationException("Файл не доступний для читання: " + filePath);
        }
    }
}
