package org.ombr24.mb1.report;

import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.exception.FileOperationException;
import org.ombr24.mb1.utils.EngagementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Генератор звітів по призначеннях (assignments)
 */
public class EngagementReportWriter implements ReportWriter {

    private static final Logger logger = LoggerFactory.getLogger(EngagementReportWriter.class);

    @Override
    public void write(String outputFile, EngagementContext context) {
        logger.info("Генерація звіту по призначеннях: {}", outputFile);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            int positionCount = 0;
            int totalUnits = 0;

            for (var entry : context.getAssignments().entrySet()) {
                String position = entry.getKey();
                var units = entry.getValue();

                writePositionSection(writer, position, units);
                positionCount++;
                totalUnits += units.size();
            }

            logger.info("Згенеровано звіт: {} позицій, {} підрозділів", positionCount, totalUnits);

        } catch (IOException e) {
            throw new FileOperationException("Не вдалося записати звіт по призначеннях: " + outputFile, e);
        }
    }

    /**
     * Записує секцію для однієї позиції
     */
    private void writePositionSection(PrintWriter writer, String position, Iterable<BattleUnit> units) {
        writer.println(position + ":");

        for (BattleUnit unit : units) {
            writer.println(formatUnit(unit));
        }

        writer.println();
    }

    /**
     * Форматує інформацію про підрозділ
     */
    private String formatUnit(BattleUnit unit) {
        return unit.getRank() + " " + unit.getName();
    }
}
