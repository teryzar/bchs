package org.ombr24.mb1.report;

import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.utils.EngagementContext;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PositionListReportWriter implements ReportWriter {

    private final List<String> positions;

    public PositionListReportWriter(List<String> positions) {
        this.positions = positions;
    }

    @Override
    public void write(String outputFile, EngagementContext context) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            for (String position : positions) {
                for (BattleUnit unit :
                        context.getAssignments().getOrDefault(position, new ArrayList<>())) {
                    writer.println(unit.getName() + "\t" + position);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write position list report", e);
        }
    }
}
