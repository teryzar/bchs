package org.ombr24.mb1.domain.daily_position_report;

import java.util.List;

public class PositionReportDataObject {
    private String currentDate;
    private List<Position> positions;
    private List<Separate> separateList;
    private List<Position> commPoints;

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public List<Separate> getSeparateList() {
        return separateList;
    }

    public void setSeparateList(List<Separate> separateList) {
        this.separateList = separateList;
    }

    public List<Position> getCommPoints() {
        return commPoints;
    }

    public void setCommPoints(List<Position> commPoints) {
        this.commPoints = commPoints;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }


}
