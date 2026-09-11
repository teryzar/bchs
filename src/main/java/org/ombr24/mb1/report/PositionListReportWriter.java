package org.ombr24.mb1.report;

import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.exception.FileOperationException;
import org.ombr24.mb1.utils.EngagementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 * Генератор звітів зі списком позицій
 */
public class PositionListReportWriter implements ReportWriter {

    private static final Logger logger = LoggerFactory.getLogger(PositionListReportWriter.class);
    
    private final List<String> positions;

    public PositionListReportWriter(List<String> positions) {
        this.positions = positions != null ? positions : Collections.emptyList();
    }

    @Override
    public void write(String outputFile, EngagementContext context) {
        logger.info("Генерація звіту зі списком позицій: {}", outputFile);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            int totalUnits = 0;

            for (String position : positions) {
                List<BattleUnit> units = context.getAssignments()
                        .getOrDefault(position, Collections.emptyList());

                for (BattleUnit unit : units) {
                    writer.println(formatUnit(unit, position));
                    totalUnits++;
                }
            }

            logger.info("Згенеровано звіт: {} позицій, {} підрозділів", positions.size(), totalUnits);

        } catch (IOException e) {
            throw new FileOperationException("Не вдалося записати звіт зі списком позицій: " + outputFile, e);
        }
    }

    /**
     * Форматує інформацію про підрозділ з позицією
     */
    private String formatUnit(BattleUnit unit, String position) {
        return unit.getName() + "\t" + position;
    }
}
