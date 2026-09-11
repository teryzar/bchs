package org.ombr24.mb1.utils;

import org.ombr24.mb1.config.AppConfig;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.domain.ParsedNumbersRow;

import java.util.*;

public class EngagementContext {


    private final Map<String, List<BattleUnit>> assignments = new HashMap<>();
    private final Map<String, List<BattleUnit>> infantryByDivision = new TreeMap<>();
    private Map<String, String> commanderRank = new HashMap<>();
    private Map<String, String> commanderName = new HashMap<>();
    private String reportDate;
    private List<ParsedNumbersRow> parsedNumberList;

    public void addAssignment(String pos, BattleUnit unit) {
        assignments.computeIfAbsent(pos, k -> new ArrayList<>()).add(unit);
    }

    public void addUnitToDivision(String division, BattleUnit unit) {
        infantryByDivision.computeIfAbsent(division, k -> new ArrayList<>()).add(unit);
    }

    public Map<String, List<BattleUnit>> getAssignments() {
        return assignments;
    }

    public Map<String, List<BattleUnit>> getInfantryByDivision() {
        return infantryByDivision;
    }

    public Map<String, String> getCommanderRank() {
        return commanderRank;
    }

    public Map<String, String> getCommanderName() {
        return commanderName;
    }

    public String getReportDate() {
        return reportDate;
    }

    public void setReportDate(String reportDate) {
        this.reportDate = reportDate;
    }

    public List<ParsedNumbersRow> getParsedNumberList() {
        return parsedNumberList;
    }

    public void setParsedNumberList(List<ParsedNumbersRow> parsedNumberList) {
        this.parsedNumberList = parsedNumberList;
    }
}
