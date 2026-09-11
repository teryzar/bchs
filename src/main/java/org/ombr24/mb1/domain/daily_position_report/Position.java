package org.ombr24.mb1.domain.daily_position_report;

import java.util.List;

public class Position {
    private String positionName;
    private List<Unit> units;

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }
}
