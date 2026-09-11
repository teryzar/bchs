package org.ombr24.mb1.report;

import org.ombr24.mb1.config.Constants;
import org.ombr24.mb1.config.ReferenceData;

import java.util.Arrays;
import java.util.List;

/**
 * Фабрика для створення секцій звіту по підрозділам
 */
public class PositionSectionFactory {

    private final ReferenceData referenceData;

    public PositionSectionFactory(ReferenceData referenceData) {
        this.referenceData = referenceData;
    }

    /**
     * Створює всі секції звіту в правильному порядку
     */
    public List<PositionSection> createAllSections() {
        return Arrays.asList(
                createPosition70Section(),
                createPosition100Section(),
                createPosition30Section()
        );
    }

    /**
     * Секція "На 70" - з типом позиції (ВЗ/ПВ)
     */
    public PositionSection createPosition70Section() {
        return PositionSection.builder()
                .title("На 70:")
                .filter(unit -> referenceData.getPosition70().contains(unit.getPosition()))
                .includePositionType(true)
                .build();
    }

    /**
     * Секція "На 100" - всі крім позиції 30
     */
    public PositionSection createPosition100Section() {
        return PositionSection.builder()
                .title("На 100:")
                .filter(unit -> !Constants.POSITION_30.equals(unit.getPosition()))
                .includePositionType(false)
                .build();
    }

    /**
     * Секція "На 30" - тільки позиція 30
     */
    public PositionSection createPosition30Section() {
        return PositionSection.builder()
                .title("На 30:")
                .filter(unit -> Constants.POSITION_30.equals(unit.getPosition()))
                .includePositionType(false)
                .build();
    }
}
