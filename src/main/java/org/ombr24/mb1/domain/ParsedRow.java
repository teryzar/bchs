package org.ombr24.mb1.domain;

public class ParsedRow {

    private final String rank;
    private final String name;
    private final String position;

    public ParsedRow(String rank, String name, String position) {
        this.rank = rank;
        this.name = name;
        this.position = position;
    }

    public boolean isEmpty() {
        return rank.isEmpty() || name.isEmpty() || position.isEmpty();
    }

    public String getRank() { return rank; }
    public String getName() { return name; }
    public String getPosition() { return position; }
}
