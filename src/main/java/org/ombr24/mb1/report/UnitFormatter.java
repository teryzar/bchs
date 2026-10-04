package org.ombr24.mb1.report;

import org.ombr24.mb1.config.TZPositions;
import org.ombr24.mb1.config.Constants;
import org.ombr24.mb1.domain.BattleUnit;

/**
 * Форматувальник для підрозділів у текстових звітах
 */
public class UnitFormatter {

    /**
     * Форматує підрозділ для виводу у звіт
     * 
     * @param unit підрозділ
     * @param includePositionType чи додавати тип позиції (ВЗ/ПВ)
     * @return відформатований рядок
     */
    public String format(BattleUnit unit, boolean includePositionType) {
        StringBuilder sb = new StringBuilder();
        
        sb.append(unit.getFullNameWithRank());
        
        if (includePositionType) {
            appendPositionType(sb, unit);
        }
        
        if (unit.isAdded()) {
            appendAddedFrom(sb, unit);
        }
        
        sb.append(";");
        
        return sb.toString();
    }

    /**
     * Додає тип позиції (ВЗ або ПВ) та саму позицію
     */
    private void appendPositionType(StringBuilder sb, BattleUnit unit) {
        String positionType = getPositionType(unit.getPosition());
        sb.append(" ")
          .append(positionType)
          .append(unit.getPosition())
          .append("»)");
    }

    /**
     * Додає інформацію про те, звідки приданий підрозділ
     */
    private void appendAddedFrom(StringBuilder sb, BattleUnit unit) {
        sb.append(" (")
          .append(unit.getAddedFrom())
          .append(")");
    }

    /**
     * Визначає тип позиції (ВЗ або ПВ)
     */
    private String getPositionType(String position) {
        return TZPositions.POSITION_TYPE_MAP.contains(position)
                ? Constants.POSITION_TYPE_TZ
                : Constants.POSITION_TYPE_BP;
    }
}
