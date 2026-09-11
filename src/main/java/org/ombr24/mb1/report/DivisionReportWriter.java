package org.ombr24.mb1.report;

import org.ombr24.mb1.config.Ambushes;
import org.ombr24.mb1.config.AppConfig;
import org.ombr24.mb1.config.ReferenceData;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.utils.EngagementContext;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DivisionReportWriter implements ReportWriter {

    private final ReferenceData referenceData;

    public DivisionReportWriter(ReferenceData referenceData) {

        this.referenceData = referenceData;
    }

    @Override
    public void write(String outputFile, EngagementContext context) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {

            context.getInfantryByDivision().forEach((division, units) -> {
                writer.println(division);
                writer.println("На 70:");
                List<BattleUnit> originUnits = units.stream().filter(unit -> !unit.isAdded()).collect(Collectors.toList());
                List<BattleUnit> addedUnits = units.stream().filter(BattleUnit::isAdded).collect(Collectors.toList());

                writer.println("Особовий склад підрозділу:");
                for (BattleUnit unit : originUnits) {
                    if (referenceData.getPosition70().contains(unit.getPosition())) {
                        writer.println(unit.getFullNameWithRank() + getPositionType(unit.getPosition()) + unit.getPosition() + "»)" +
                                (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                    }
                }

                writer.println("Приданий особовий склад (в оперативне підпорядкування):");
                for (BattleUnit unit : addedUnits) {
                    if (referenceData.getPosition70().contains(unit.getPosition())) {
                        writer.println(unit.getFullNameWithRank() + getPositionType(unit.getPosition()) + unit.getPosition() + "»)" +
                                (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                    }
                }

                writer.println();

                writer.println("На 100:");
                writer.println("Особовий склад підрозділу:");
                for (BattleUnit unit : originUnits) {
                    if (!"30".equals(unit.getPosition())) {
                        writer.println(unit.getFullNameWithRank()  +
                                (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                    }
                }

                writer.println("Приданий особовий склад (в оперативне підпорядкування):");

                for (BattleUnit unit : addedUnits) {
                    if (!"30".equals(unit.getPosition())) {
                        writer.println(unit.getFullNameWithRank()  +
                                (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                    }
                }



                writer.println("На 30:");
                writer.println("Особовий склад підрозділу:");
                for (BattleUnit unit : originUnits) {
                    if ("30".equals(unit.getPosition())) {
                        writer.println(unit.getFullNameWithRank() +
                                (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                    }
                }

                writer.println("Приданий особовий склад (в оперативне підпорядкування):");
                for (BattleUnit unit : addedUnits) {
                    if ("30".equals(unit.getPosition())) {
                        writer.println(unit.getFullNameWithRank() +
                                (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                    }
                }

                writer.println();

                writer.println("_____________________________________________________");

            });
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write division report", e);
        }
    }

    private String getPositionType(String position) {
        return Ambushes.REPORTERS_MAP.contains(position) ? "(ВЗ «" : "(ПВ «";
    }
}
