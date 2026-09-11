package org.ombr24.mb1.domain;

public class AddedBattleUnit {
    private final String name;
    private final String addedFrom;
    private final String addedTo;

    public AddedBattleUnit(String name, String addedFrom, String addedTo) {
        this.name = name;
        this.addedFrom = addedFrom;
        this.addedTo = addedTo;
    }

    public String getName() {
        return name;
    }

    public String getAddedFrom() {
        return addedFrom;
    }

    public String getAddedTo() {
        return addedTo;
    }
}
