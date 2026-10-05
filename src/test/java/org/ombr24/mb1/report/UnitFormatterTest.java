package org.ombr24.mb1.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ombr24.mb1.config.TZPositions;
import org.ombr24.mb1.domain.BattleUnit;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для UnitFormatter
 */
class UnitFormatterTest {

    private UnitFormatter formatter;

    @BeforeEach
    void setUp() {
        formatter = new UnitFormatter();
    }

    @Test
    void shouldFormatUnitWithoutPositionType() {
        // Given
        BattleUnit unit = new BattleUnit("майор", "Іванов Іван", "100", false, null, null);
        
        // When
        String result = formatter.format(unit, false);
        
        // Then
        assertThat(result).isEqualTo("майор ІВАНОВ Іван;");
    }

    @Test
    void shouldFormatUnitWithPositionTypeBP() {
        // Given
        BattleUnit unit = new BattleUnit("капітан", "Петров Петро", "70", false, null, null);
        
        // When
        String result = formatter.format(unit, true);
        
        // Then
        assertThat(result).isEqualTo("капітан ПЕТРОВ Петро (БП «70»);");
    }

    @Test
    void shouldFormatUnitWithPositionTypeTZ() {
        // Given
        BattleUnit unit = new BattleUnit("підполковник", "Сидоренко С.", "Спектра", false, null, null);
        
        // When
        String result = formatter.format(unit, true);
        
        // Then
        assertThat(result).contains("(ТЗ «Спектра»);");
    }

    @Test
    void shouldFormatAddedUnit() {
        // Given
        BattleUnit unit = new BattleUnit("полковник", "Коваленко К.", "100", true, "1 мр", "2 мр");
        
        // When
        String result = formatter.format(unit, false);
        
        // Then
        assertThat(result).isEqualTo("полковник КОВАЛЕНКО К. (1 мр);");
    }

    @Test
    void shouldFormatAddedUnitWithPositionType() {
        // Given
        BattleUnit unit = new BattleUnit("майор", "Шевченко Ш.", "70", true, "3 мр", "4 мр");
        
        // When
        String result = formatter.format(unit, true);
        
        // Then
        assertThat(result).isEqualTo("майор ШЕВЧЕНКО Ш. (БП «70») (3 мр);");
    }

    @Test
    void shouldAlwaysEndWithSemicolon() {
        // Given
        BattleUnit unit1 = new BattleUnit("капітан", "А", "70", false, null, null);
        BattleUnit unit2 = new BattleUnit("майор", "Б", "100", true, "1 мр", "2 мр");
        
        // When
        String result1 = formatter.format(unit1, false);
        String result2 = formatter.format(unit2, true);
        
        // Then
        assertThat(result1).endsWith(";");
        assertThat(result2).endsWith(";");
    }

    @Test
    void shouldHandleTZPositionSamsung() {
        // Given
        BattleUnit unit = new BattleUnit("капітан", "Тестовий Т.", "Самсунг", false, null, null);
        
        // When
        String result = formatter.format(unit, true);
        
        // Then
        assertThat(result).contains("(ТЗ «Самсунг»);");
    }
}
