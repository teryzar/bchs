package org.ombr24.mb1.report;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.ombr24.mb1.config.Constants;
import org.ombr24.mb1.config.ReferenceData;
import org.ombr24.mb1.config.Reporters;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.domain.DailyCommanderReportDataObject;
import org.ombr24.mb1.domain.ParsedNumbersRow;
import org.ombr24.mb1.exception.DataProcessingException;
import org.ombr24.mb1.exception.FileOperationException;
import org.ombr24.mb1.utils.EngagementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Генератор JSON звітів для кожного підрозділу
 */
public class JsonReportWriter implements ReportWriter {

    private static final Logger logger = LoggerFactory.getLogger(JsonReportWriter.class);

    private final ReferenceData referenceData;
    private final Gson gson;
    private final UnitFormatter unitFormatter = new UnitFormatter();

    public JsonReportWriter(ReferenceData referenceData) {
        this.referenceData = referenceData;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public void write(String outputFile, EngagementContext context) {
        logger.info("Генерація JSON звітів: {}", outputFile);
        int reportCount = 0;

        for (var entry : context.getInfantryByDivision().entrySet()) {
            String division = entry.getKey();
            List<BattleUnit> units = entry.getValue();

            try {
                DailyCommanderReportDataObject report = buildReport(division, units, context);
                writeJsonFile(outputFile, report);
                reportCount++;
                logger.debug("JSON звіт для {} створено успішно", division);

            } catch (Exception e) {
                logger.error("Помилка створення JSON звіту для {}", division, e);
                throw new DataProcessingException("Помилка створення JSON звіту для підрозділу: " + division, e);
            }
        }

        logger.info("Згенеровано {} JSON звітів", reportCount);
    }

    /**
     * Будує об'єкт звіту для підрозділу
     */
    private DailyCommanderReportDataObject buildReport(String division, List<BattleUnit> units, 
                                                       EngagementContext context) {
        DailyCommanderReportDataObject report = new DailyCommanderReportDataObject();

        // Базова інформація
        report.setReportDate(context.getReportDate());
        report.setDivision(division);

        // Розділяємо на оригінальні та придані підрозділи
        List<BattleUnit> originalUnits = units.stream()
                .filter(unit -> !unit.isAdded())
                .collect(Collectors.toList());

        List<BattleUnit> addedUnits = units.stream()
                .filter(BattleUnit::isAdded)
                .collect(Collectors.toList());

        // list1: На 70 - Особовий склад підрозділу
        report.setList1(formatUnitsForPosition70(originalUnits));

        // list2: На 70 - Приданий особовий склад
        report.setList2(formatUnitsForPosition70(addedUnits));

        // list3: На 100 - Особовий склад підрозділу
        report.setList3(formatUnitsForPosition100(originalUnits));

        // list4: На 100 - Приданий особовий склад
        report.setList4(formatUnitsForPosition100(addedUnits));

        // list5: На 30 - Особовий склад підрозділу
        report.setList5(formatUnitsForPosition30(originalUnits));

        // list6: На 30 - Приданий особовий склад
        report.setList6(formatUnitsForPosition30(addedUnits));

        // Інформація про командира
        report.setPosition(referenceData.getCommanderPositions().get(division));
        report.setRank(context.getCommanderRank().get(division));
        report.setName(context.getCommanderName().get(division));
        report.setReporter(Reporters.REPORTERS_MAP.get(division));

        // Номер звіту
        String reportNumber = findReportNumber(division, context);
        report.setNumber(reportNumber);

        return report;
    }

    /**
     * Форматує підрозділи для позиції 70 (з типом позиції ПВ)
     */
    private List<String> formatUnitsForPosition70(List<BattleUnit> units) {
        return formatUnits(units, 
            unit -> referenceData.getPositions70().contains(unit.getPosition()),
            true);
    }

    /**
     * Форматує підрозділи для позиції 100 (без позиції 30)
     */
    private List<String> formatUnitsForPosition100(List<BattleUnit> units) {
        return formatUnits(units,
            unit -> !Constants.POSITION_30.equals(unit.getPosition()),
            false);
    }

    /**
     * Форматує підрозділи для позиції 30
     */
    private List<String> formatUnitsForPosition30(List<BattleUnit> units) {
        return formatUnits(units,
            unit -> Constants.POSITION_30.equals(unit.getPosition()),
            false);
    }

    /**
     * Форматує список підрозділів за фільтром
     */
    private List<String> formatUnits(List<BattleUnit> units, Predicate<BattleUnit> filter, 
                                    boolean includePositionType) {
        return units.stream()
                .filter(filter)
                .map(unit -> unitFormatter.format(unit, includePositionType))
                .collect(Collectors.toList());
    }

    /**
     * Знаходить номер звіту для підрозділу
     */
    private String findReportNumber(String division, EngagementContext context) {
        String reportDate = context.getReportDate();
        
        ParsedNumbersRow numbersRow = context.getParsedNumberList().stream()
                .filter(row -> row.getReportDate().equals(reportDate))
                .filter(row -> row.getDivision().equals(division))
                .findFirst()
                .orElseThrow(() -> new DataProcessingException(
                    String.format("Не знайдено номер звіту для підрозділу '%s' на дату '%s'", 
                        division, reportDate)));

        return numbersRow.getReportNr();
    }

    /**
     * Записує JSON у файл
     */
    private void writeJsonFile(String baseOutputFile, DailyCommanderReportDataObject report) {
        String fileName = String.format("%s%s_%s.json", 
            baseOutputFile, report.getReportDate(), report.getDivision());

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.print(gson.toJson(report));
            logger.trace("JSON файл записано: {}", fileName);

        } catch (IOException e) {
            throw new FileOperationException("Не вдалося записати JSON файл: " + fileName, e);
        }
    }
}
