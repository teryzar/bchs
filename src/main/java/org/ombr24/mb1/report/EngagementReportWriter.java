package org.ombr24.mb1.report;

import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.utils.EngagementContext;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

public class EngagementReportWriter implements ReportWriter {

    @Override
    public void write(String outputFile, EngagementContext context) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            context.getAssignments().forEach((position, units) -> {
                writer.println(position + ":");
                for (BattleUnit unit : units) {
                    writer.println(unit.getRank() + " " + unit.getName());
                }
                writer.println();
            });
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write engagement report", e);
        }
    }
}
