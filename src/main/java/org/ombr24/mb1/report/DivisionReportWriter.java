package org.ombr24.mb1.report;

import org.ombr24.mb1.config.ReferenceData;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.exception.FileOperationException;
import org.ombr24.mb1.utils.EngagementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Генератор текстових звітів по підрозділам
 * 
 * Використовує патерни:
 * - Builder (для створення секцій)
 * - Factory (для створення конфігурацій секцій)
 * - Strategy (для фільтрації підрозділів)
 */
public class DivisionReportWriter implements ReportWriter {

    private static final Logger logger = LoggerFactory.getLogger(DivisionReportWriter.class);
    
    private static final String ORIGINAL_UNITS_HEADER = "Особовий склад підрозділу:";
    private static final String ADDED_UNITS_HEADER = "Приданий особовий склад (в оперативне підпорядкування):";
    private static final String DIVISION_SEPARATOR = "_____________________________________________________";

    private final PositionSectionFactory sectionFactory;
    private final UnitFormatter unitFormatter;

    public DivisionReportWriter(ReferenceData referenceData) {
        this.sectionFactory = new PositionSectionFactory(referenceData);
        this.unitFormatter = new UnitFormatter();
    }

    @Override
    public void write(String outputFile, EngagementContext context) {
        logger.info("Генерація текстового звіту по підрозділам: {}", outputFile);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            int divisionCount = 0;
            int totalUnits = 0;

            for (var entry : context.getInfantryByDivision().entrySet()) {
                String division = entry.getKey();
                List<BattleUnit> units = entry.getValue();

                writeDivisionReport(writer, division, units);
                divisionCount++;
                totalUnits += units.size();
            }

            logger.info("Згенеровано звіт: {} підрозділів, {} бойових одиниць", divisionCount, totalUnits);

        } catch (IOException e) {
            throw new FileOperationException("Не вдалося записати звіт по підрозділам: " + outputFile, e);
        }
    }

    /**
     * Записує звіт для одного підрозділу
     * 
     * @param writer PrintWriter для запису
     * @param division назва підрозділу
     * @param units список бойових одиниць
     */
    private void writeDivisionReport(PrintWriter writer, String division, List<BattleUnit> units) {
        writer.println(division);

        // Розділяємо на оригінальні та придані підрозділи
        UnitGroups unitGroups = UnitGroups.from(units);

        // Генеруємо всі секції звіту
        List<PositionSection> sections = sectionFactory.createAllSections();
        for (PositionSection section : sections) {
            writePositionSection(writer, section, unitGroups);
        }

        writer.println(DIVISION_SEPARATOR);
        writer.println();
    }

    /**
     * Записує секцію для певної категорії позицій
     * 
     * @param writer PrintWriter для запису
     * @param section конфігурація секції
     * @param unitGroups групи підрозділів (оригінальні та придані)
     */
    private void writePositionSection(PrintWriter writer, PositionSection section, UnitGroups unitGroups) {
        writer.println(section.getTitle());

        // Оригінальний особовий склад
        writer.println(ORIGINAL_UNITS_HEADER);
        writeUnits(writer, unitGroups.getOriginalUnits(), section);

        // Приданий особовий склад
        writer.println(ADDED_UNITS_HEADER);
        writeUnits(writer, unitGroups.getAddedUnits(), section);

        writer.println();
    }

    /**
     * Записує список підрозділів
     * 
     * @param writer PrintWriter для запису
     * @param units список бойових одиниць
     * @param section конфігурація секції з фільтром та налаштуваннями форматування
     */
    private void writeUnits(PrintWriter writer, List<BattleUnit> units, PositionSection section) {
        units.stream()
                .filter(section.getFilter())
                .map(unit -> unitFormatter.format(unit, section.shouldIncludePositionType()))
                .forEach(writer::println);
    }
}
