package org.ombr24.mb1.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.ombr24.mb1.config.*;
import org.ombr24.mb1.domain.*;
import org.ombr24.mb1.excel.AddedUnitMapper;
import org.ombr24.mb1.excel.ExcelRowMapper;
import org.ombr24.mb1.report.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class XlsProcessor {

    private static final Set<String> SKIP =
            new HashSet<>(Arrays.asList("ПРИМІТКИ", "Управління", "Прикомандировані", "БРЕЗ", "оперативне підпорядкування"));

    private final AppConfig config;
    private final ReferenceData refData;
    private final EngagementContext context = new EngagementContext();
    private final ExcelRowMapper excelRowMapper = new ExcelRowMapper();
    private final AddedUnitMapper addedUnitMapper = new AddedUnitMapper();

    public XlsProcessor(AppConfig config, ReferenceData refData) {
        this.config = config;
        this.refData = refData;
    }

    public void process(int day) {


        try (XSSFWorkbook workbook = new XSSFWorkbook(config.getDataDir() + "/numbers.xlsx")) {
            List<ParsedNumbersRow> parsedNumbersList = new ArrayList<>();
            XSSFSheet sheetAt = workbook.getSheetAt(0);
            for (Row row : sheetAt) {
                ParsedNumbersRow parsedNumbersRow = new ParsedNumbersRow();
                parsedNumbersRow.setReportNr(row.getCell(0).getStringCellValue());
                SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
                parsedNumbersRow.setReportDate(formatter.format(row.getCell(1).getDateCellValue()));
                parsedNumbersRow.setDivision(row.getCell(2).getStringCellValue());
                parsedNumbersList.add(parsedNumbersRow);
            }
            context.setParsedNumberList(parsedNumbersList);
        } catch (IOException e) {
            throw new IllegalStateException("Numbers excel read error", e);
        }
        try (XSSFWorkbook workbook = new XSSFWorkbook(config.inputExcel())) {
            Map<String, List<BattleUnit>> originUnitMap = new HashMap<String, List<BattleUnit>>();
            Map<String, AddedBattleUnit> addedUnitsInfoMap = readAddedUnits(workbook.getSheet("оперативне підпорядкування"), day);
            Map<String, List<BattleUnit>> addedUnitsMap = new HashMap<String, List<BattleUnit>>();
            Date date = workbook.getSheet("1 мр").getRow(0).getCell(day + 1).getDateCellValue();
            SimpleDateFormat sdfDestination = new SimpleDateFormat("dd.MM.yyyy");
            String destDate = sdfDestination.format(date);
            context.setReportDate(destDate);

            for (Sheet sheet : workbook) {
                if (SKIP.contains(sheet.getSheetName())) continue;


                context.getCommanderRank().put(sheet.getSheetName(), sheet.getRow(1).getCell(0).getStringCellValue());

                context.getCommanderName().put(sheet.getSheetName(), formatName(sheet.getRow(1).getCell(1).getStringCellValue()));

                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    ParsedRow parsed = excelRowMapper.map(row, day + 1);
                    if (parsed.isEmpty()) continue;

                    boolean isAdded = addedUnitsInfoMap.containsKey(parsed.getName());
                    String addedFrom = isAdded ? addedUnitsInfoMap.get(parsed.getName()).getAddedFrom() : null;
                    String addedTo = isAdded ? addedUnitsInfoMap.get(parsed.getName()).getAddedTo() : null;

                    BattleUnit unit =
                            new BattleUnit(parsed.getRank(), parsed.getName(), parsed.getPosition(),
                                    isAdded, addedFrom, addedTo);
                    originUnitMap.computeIfAbsent(sheet.getSheetName(), k -> new ArrayList<>()).add(unit);

                    context.addAssignment(parsed.getPosition(), unit);
                }


            }

            //create not added unit list
            for (Map.Entry<String, List<BattleUnit>> entry : originUnitMap.entrySet()) {
                for (BattleUnit unit : entry.getValue()) {
                    if(!unit.isAdded()) {
                        context.addUnitToDivision(entry.getKey(), unit);
                    } else {
                        addedUnitsMap.computeIfAbsent(unit.getAddedTo(), k -> new ArrayList<>()).add(unit);
                    }
                }
            }

            //add added units
            for (Map.Entry<String, List<BattleUnit>> battleUnitList : addedUnitsMap.entrySet()) {
                for (BattleUnit battleUnit : battleUnitList.getValue()) {
                    context.addUnitToDivision(battleUnitList.getKey(), battleUnit);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Excel read error", e);
        }

//        new EngagementReportWriter().write(config.engagementReport(day), context);
        new DivisionReportWriter(refData).write(config.divisionReport(day), context);
        new JsonReportWriter(refData).write(config.divisionJsonReport(day), context);
//        new PositionListReportWriter(refData.getPosition70())
//                .write(config.ropReport(day), context);
//        new PositionListReportWriter(refData.getPositions100())
//                .write(config.separateReport(day), context);
    }

    private Map<String, AddedBattleUnit> readAddedUnits(XSSFSheet sheet, int day) {
        Map<String, AddedBattleUnit> addedUnits = new HashMap<>();
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            ParsedAddedUnitRow parsed = addedUnitMapper.map(row, day + 1);
            if (parsed.isEmpty()) continue;

            AddedBattleUnit unit =
                    new AddedBattleUnit(parsed.getName(), parsed.getAddedFrom(), parsed.getAddedTo());

            addedUnits.put(unit.getName(), unit);
        }

        return addedUnits;
    }

    private String formatName(String name) {
        String[] s = name.split(" ");
        return s[1] + " " + s[0].toUpperCase();
    }
}
