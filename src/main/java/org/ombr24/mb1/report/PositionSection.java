package org.ombr24.mb1.report;

import org.ombr24.mb1.domain.BattleUnit;

import java.util.function.Predicate;

/**
 * Представляє секцію звіту для певної категорії позицій
 */
public class PositionSection {
    
    private final String title;
    private final Predicate<BattleUnit> filter;
    private final boolean includePositionType;

    private PositionSection(String title, Predicate<BattleUnit> filter, boolean includePositionType) {
        this.title = title;
        this.filter = filter;
        this.includePositionType = includePositionType;
    }

    public String getTitle() {
        return title;
    }

    public Predicate<BattleUnit> getFilter() {
        return filter;
    }

    public boolean shouldIncludePositionType() {
        return includePositionType;
    }

    /**
     * Builder для створення секцій звіту
     */
    public static class Builder {
        private String title;
        private Predicate<BattleUnit> filter;
        private boolean includePositionType = false;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder filter(Predicate<BattleUnit> filter) {
            this.filter = filter;
            return this;
        }

        public Builder includePositionType(boolean includePositionType) {
            this.includePositionType = includePositionType;
            return this;
        }

        public PositionSection build() {
            if (title == null || filter == null) {
                throw new IllegalStateException("Title and filter are required");
            }
            return new PositionSection(title, filter, includePositionType);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
