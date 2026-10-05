package org.ombr24.mb1.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для ParsedRow
 */
class ParsedRowTest {

    @Test
    void shouldCreateParsedRow() {
        // Given
        String rank = "підполковник";
        String name = "Іванов І.І.";
        String position = "70";
        
        // When
        ParsedRow row = new ParsedRow(rank, name, position);
        
        // Then
        assertThat(row.getRank()).isEqualTo(rank);
        assertThat(row.getName()).isEqualTo(name);
        assertThat(row.getPosition()).isEqualTo(position);
        assertThat(row.isEmpty()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
        "'', 'Іванов', '70'",
        "'майор', '', '70'",
        "'майор', 'Іванов', ''",
        "'', '', ''",
        "'', '', '70'"
    })
    void shouldDetectEmptyRows(String rank, String name, String position) {
        // Given
        ParsedRow row = new ParsedRow(rank, name, position);
        
        // When
        boolean isEmpty = row.isEmpty();
        
        // Then
        assertThat(isEmpty).isTrue();
    }

    @Test
    void shouldNotBeEmptyWhenAllFieldsFilled() {
        // Given
        ParsedRow row = new ParsedRow("капітан", "Петров П.", "100");
        
        // When
        boolean isEmpty = row.isEmpty();
        
        // Then
        assertThat(isEmpty).isFalse();
    }

    @Test
    void shouldBeImmutable() {
        // Given
        ParsedRow row = new ParsedRow("майор", "Сидоренко", "50");
        
        // Then - всі поля мають лише getters
        assertThat(row.getRank()).isEqualTo("майор");
        assertThat(row.getName()).isEqualTo("Сидоренко");
        assertThat(row.getPosition()).isEqualTo("50");
    }
}
