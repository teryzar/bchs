package org.ombr24.mb1.report;

import org.ombr24.mb1.domain.BattleUnit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Представляє групи підрозділів (оригінальні та придані)
 */
public class UnitGroups {
    
    private final List<BattleUnit> originalUnits;
    private final List<BattleUnit> addedUnits;

    private UnitGroups(List<BattleUnit> originalUnits, List<BattleUnit> addedUnits) {
        this.originalUnits = originalUnits;
        this.addedUnits = addedUnits;
    }

    /**
     * Створює групи підрозділів з загального списку
     */
    public static UnitGroups from(List<BattleUnit> units) {
        List<BattleUnit> originalUnits = units.stream()
                .filter(unit -> !unit.isAdded())
                .collect(Collectors.toList());

        List<BattleUnit> addedUnits = units.stream()
                .filter(BattleUnit::isAdded)
                .collect(Collectors.toList());

        return new UnitGroups(originalUnits, addedUnits);
    }

    public List<BattleUnit> getOriginalUnits() {
        return originalUnits;
    }

    public List<BattleUnit> getAddedUnits() {
        return addedUnits;
    }

}
