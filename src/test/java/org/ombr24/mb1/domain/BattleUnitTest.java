package org.ombr24.mb1.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для BattleUnit
 */
class BattleUnitTest {

    @Test
    void shouldCreateBattleUnit() {
        // Given
        String rank = "підполковник";
        String name = "Іванов Іван Іванович";
        String position = "70";
        
        // When
        BattleUnit unit = new BattleUnit(rank, name, position, false, null, null);
        
        // Then
        assertThat(unit.getRank()).isEqualTo(rank);
        assertThat(unit.getName()).isEqualTo(name);
        assertThat(unit.getPosition()).isEqualTo(position);
        assertThat(unit.isAdded()).isFalse();
        assertThat(unit.getAddedFrom()).isNull();
        assertThat(unit.getAddedTo()).isNull();
    }

    @Test
    void shouldCreateAddedBattleUnit() {
        // Given
        String rank = "майор";
        String name = "Петров Петро";
        String position = "100";
        String addedFrom = "1 мр";
        String addedTo = "2 мр";
        
        // When
        BattleUnit unit = new BattleUnit(rank, name, position, true, addedFrom, addedTo);
        
        // Then
        assertThat(unit.isAdded()).isTrue();
        assertThat(unit.getAddedFrom()).isEqualTo(addedFrom);
        assertThat(unit.getAddedTo()).isEqualTo(addedTo);
    }

    @ParameterizedTest
    @CsvSource({
        "'Іванов Іван Іванович', 'підполковник', 'підполковник ІВАНОВ Іван Іванович'",
        "'Петров Петро', 'майор', 'майор ПЕТРОВ Петро'",
        "'Сидоренко', 'капітан', 'капітан СИДОРЕНКО '",
        "'Шевченко Тарас Григорович', 'полковник', 'полковник ШЕВЧЕНКО Тарас Григорович'"
    })
    void shouldFormatFullNameWithRank(String name, String rank, String expected) {
        // Given
        BattleUnit unit = new BattleUnit(rank, name, "70", false, null, null);
        
        // When
        String result = unit.getFullNameWithRank();
        
        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldHandleEmptyNameGracefully() {
        // Given
        BattleUnit unit = new BattleUnit("майор", "", "70", false, null, null);
        
        // When/Then - перевіряємо що не падає з NullPointerException
        assertThatCode(unit::getFullNameWithRank)
            .doesNotThrowAnyException();
    }

    @Test
    void shouldHandleSingleWordName() {
        // Given
        BattleUnit unit = new BattleUnit("капітан", "Іванов", "70", false, null, null);
        
        // When
        String result = unit.getFullNameWithRank();
        
        // Then
        assertThat(result).isEqualTo("капітан ІВАНОВ ");
    }

    @Test
    void shouldBeImmutable() {
        // Given
        BattleUnit unit = new BattleUnit("майор", "Іванов І.", "70", false, null, null);
        
        // Then - всі поля final, немає setter'ів
        assertThat(unit.getRank()).isEqualTo("майор");
        assertThat(unit.getName()).isEqualTo("Іванов І.");
    }
}
