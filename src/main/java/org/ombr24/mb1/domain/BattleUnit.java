package org.ombr24.mb1.domain;

public class BattleUnit {

    private final String rank;
    private final String name;
    private final String position;
    private final boolean isAdded;
    private final String addedFrom;
    private final String addedTo;

    public BattleUnit(String rank, String name, String position, boolean isAdded,
                      String addedFrom, String addedTo) {
        this.rank = rank;
        this.name = name;
        this.position = position;
        this.isAdded = isAdded;
        this.addedFrom = addedFrom;
        this.addedTo = addedTo;
    }

    public String getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public String getPosition() {
        return position;
    }

    public boolean isAdded() {
        return isAdded;
    }

    public String getAddedFrom() {
        return addedFrom;
    }

    public String getAddedTo() {
        return addedTo;
    }

    public String getFullNameWithRank() {
        String[] s = name.split(" ");
        String formattedName = "";
        for (int i = 0; i < s.length; i++) {
            if (i == 0) {
                formattedName = formattedName + s[i].toUpperCase() + " ";
            } else if (i == s.length - 1) {
                formattedName = formattedName + s[i];
            } else {
                formattedName = formattedName + s[i] + " ";
            }
        }
        return rank + " " + formattedName;

    }
}
