package org.ombr24.mb1.config;

import java.util.List;
import java.util.Map;

public class ReferenceData {

    private final List<String> positions70;
    private final List<String> positions100;
    private final Map<String, String> commanderPositions;

    public ReferenceData(
            List<String> positions70,
            List<String> positions100,
            Map<String, String> commanderPositions
    ) {
        this.positions70 = positions70;
        this.positions100 = positions100;
        this.commanderPositions = commanderPositions;
    }

    public List<String> getPositions70() {
        return positions70;
    }

    public List<String> getPositions100() {
        return positions100;
    }

    public Map<String, String> getCommanderPositions() {
        return commanderPositions;
    }
}
