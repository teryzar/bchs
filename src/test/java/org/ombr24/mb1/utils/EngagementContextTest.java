package org.ombr24.mb1.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ombr24.mb1.domain.BattleUnit;
import org.ombr24.mb1.domain.ParsedNumbersRow;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для EngagementContext
 */
class EngagementContextTest {

    private EngagementContext context;

    @BeforeEach
    void setUp() {
        context = new EngagementContext();
    }

    @Test
    void shouldAddAssignmentToPosition() {
        // Given
        BattleUnit unit = new BattleUnit("майор", "Іванов", "70", false, null, null);
        
        // When
        context.addAssignment("70", unit);
        
        // Then
        assertThat(context.getAssignments()).containsKey("70");
        assertThat(context.getAssignments().get("70")).containsExactly(unit);
    }

    @Test
    void shouldAddMultipleUnitsToSamePosition() {
        // Given
        BattleUnit unit1 = new BattleUnit("майор", "Іванов", "70", false, null, null);
        BattleUnit unit2 = new BattleUnit("капітан", "Петров", "70", false, null, null);
        
        // When
        context.addAssignment("70", unit1);
        context.addAssignment("70", unit2);
        
        // Then
        assertThat(context.getAssignments().get("70"))
            .hasSize(2)
            .containsExactly(unit1, unit2);
    }

    @Test
    void shouldAddUnitToDivision() {
        // Given
        BattleUnit unit = new BattleUnit("полковник", "Коваленко", "100", false, null, null);
        
        // When
        context.addUnitToDivision("1 мр", unit);
        
        // Then
        assertThat(context.getInfantryByDivision()).containsKey("1 мр");
        assertThat(context.getInfantryByDivision().get("1 мр")).containsExactly(unit);
    }

    @Test
    void shouldSortDivisionsByName() {
        // Given
        BattleUnit unit1 = new BattleUnit("майор", "А", "70", false, null, null);
        BattleUnit unit2 = new BattleUnit("майор", "Б", "70", false, null, null);
        BattleUnit unit3 = new BattleUnit("майор", "В", "70", false, null, null);
        
        // When
        context.addUnitToDivision("3 мр", unit3);
        context.addUnitToDivision("1 мр", unit1);
        context.addUnitToDivision("2 мр", unit2);
        
        // Then
        List<String> divisions = List.copyOf(context.getInfantryByDivision().keySet());
        assertThat(divisions).containsExactly("1 мр", "2 мр", "3 мр");
    }

    @Test
    void shouldSetAndGetReportDate() {
        // Given
        String date = "15.05.2024";
        
        // When
        context.setReportDate(date);
        
        // Then
        assertThat(context.getReportDate()).isEqualTo(date);
    }

    @Test
    void shouldSetAndGetParsedNumberList() {
        // Given
        ParsedNumbersRow row1 = new ParsedNumbersRow();
        row1.setReportNr("001");
        row1.setDivision("1 мр");
        
        ParsedNumbersRow row2 = new ParsedNumbersRow();
        row2.setReportNr("002");
        row2.setDivision("2 мр");
        
        List<ParsedNumbersRow> numbers = List.of(row1, row2);
        
        // When
        context.setParsedNumberList(numbers);
        
        // Then
        assertThat(context.getParsedNumberList())
            .hasSize(2)
            .containsExactly(row1, row2);
    }

    @Test
    void shouldStoreCommanderInfo() {
        // Given
        String division = "1 мр";
        String rank = "полковник";
        String name = "Шевченко Т.";
        
        // When
        context.getCommanderRank().put(division, rank);
        context.getCommanderName().put(division, name);
        
        // Then
        assertThat(context.getCommanderRank().get(division)).isEqualTo(rank);
        assertThat(context.getCommanderName().get(division)).isEqualTo(name);
    }

    @Test
    void shouldInitializeEmptyCollections() {
        // Then
        assertThat(context.getAssignments()).isEmpty();
        assertThat(context.getInfantryByDivision()).isEmpty();
        assertThat(context.getCommanderRank()).isEmpty();
        assertThat(context.getCommanderName()).isEmpty();
        assertThat(context.getReportDate()).isNull();
        assertThat(context.getParsedNumberList()).isNull();
    }
}
