package org.ombr24.mb1.report;

import com.google.gson.Gson;
import org.ombr24.mb1.config.ReferenceData;
import org.ombr24.mb1.config.Reporters;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.domain.DailyCommanderReportDataObject;
import org.ombr24.mb1.domain.ParsedNumbersRow;
import org.ombr24.mb1.utils.EngagementContext;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonReportWriter implements ReportWriter {

    private final ReferenceData referenceData;

    public JsonReportWriter(ReferenceData referenceData) {
        this.referenceData = referenceData;
    }

    @Override
    public void write(String outputFile, EngagementContext context) {
        DailyCommanderReportDataObject reportDataObject = new DailyCommanderReportDataObject();
        Gson gson = new Gson();

        context.getInfantryByDivision().forEach((division, units) -> {
            reportDataObject.setReportDate(context.getReportDate());

            reportDataObject.setDivision(division);
            //на 70
            List<BattleUnit> originUnits = units.stream().filter(unit -> !unit.isAdded()).collect(Collectors.toList());
            List<BattleUnit> addedUnits = units.stream().filter(BattleUnit::isAdded).collect(Collectors.toList());

            //ос підрозділу
            ArrayList<String> list1 = new ArrayList<>();
            for (BattleUnit unit : originUnits) {
                if (referenceData.getPosition70().contains(unit.getPosition())) {
                    list1.add(unit.getFullNameWithRank() + " (ПВ «" + unit.getPosition() + "»)" +
                            (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");

                }
            }
            reportDataObject.setList1(list1);

            //Приданий особовий склад (в оперативне підпорядкування)
            ArrayList<String> list2 = new ArrayList<>();
            for (BattleUnit unit : addedUnits) {
                if (referenceData.getPosition70().contains(unit.getPosition())) {
                    list2.add(unit.getFullNameWithRank() + " (ПВ «" + unit.getPosition() + "»)" +
                            (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                }
            }
            reportDataObject.setList2(list2);


            //на 100
            //Особовий склад підрозділу
            ArrayList<String> list3 = new ArrayList<>();
            for (BattleUnit unit : originUnits) {
                if (!"30".equals(unit.getPosition())) {
                    list3.add(unit.getFullNameWithRank() +
                            (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                }
            }
            reportDataObject.setList3(list3);

            //Приданий особовий склад (в оперативне підпорядкування
            ArrayList<String> list4 = new ArrayList<>();
            for (BattleUnit unit : addedUnits) {
                if (!"30".equals(unit.getPosition())) {
                    list4.add(unit.getFullNameWithRank() +
                            (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                }
            }

            reportDataObject.setList4(list4);

            //На 30
            //Особовий склад підрозділу
            ArrayList<String> list5 = new ArrayList<>();
            for (BattleUnit unit : originUnits) {
                if ("30".equals(unit.getPosition())) {
                    list5.add(unit.getFullNameWithRank() +
                            (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                }
            }
            reportDataObject.setList5(list5);

            //Приданий особовий склад (в оперативне підпорядкування
            ArrayList<String> list6 = new ArrayList<>();
            for (BattleUnit unit : addedUnits) {
                if ("30".equals(unit.getPosition())) {
                    list6.add(unit.getFullNameWithRank() +
                            (unit.isAdded() ? " (" + unit.getAddedFrom() + ")" : "") + ";");
                }
            }

            reportDataObject.setList6(list6);

            reportDataObject.setPosition(referenceData.getCommanderPosiitions().get(division));
            reportDataObject.setRank(context.getCommanderRank().get(division));
            reportDataObject.setName(context.getCommanderName().get(division));
            reportDataObject.setReporter(Reporters.REPORTERS_MAP.get(division));
//            System.out.println(context.getReportDate());
//            System.out.println(division + " start");
            ParsedNumbersRow parsedNumbersRow = context.getParsedNumberList().stream().filter(r -> r.getReportDate().equals(context.getReportDate())).
                    filter(r -> r.getDivision().equals(division)).collect(Collectors.toList()).get(0);
            reportDataObject.setNumber(parsedNumbersRow.getReportNr());

            String json = gson.toJson(reportDataObject);
//            System.out.println(division + " finish");
            try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile + reportDataObject.getReportDate() + "_" + division ))) {
                writer.print(json);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
