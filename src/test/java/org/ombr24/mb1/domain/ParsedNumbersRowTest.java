package org.ombr24.mb1.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для ParsedNumbersRow
 */
class ParsedNumbersRowTest {

    @Test
    void shouldCreateAndSetAllFields() {
        // Given
        ParsedNumbersRow row = new ParsedNumbersRow();
        
        // When
        row.setReportNr("001");
        row.setReportDate("15.05.2024");
        row.setDivision("1 мр");
        
        // Then
        assertThat(row.getReportNr()).isEqualTo("001");
        assertThat(row.getReportDate()).isEqualTo("15.05.2024");
        assertThat(row.getDivision()).isEqualTo("1 мр");
    }

    @Test
    void shouldAllowNullValues() {
        // Given
        ParsedNumbersRow row = new ParsedNumbersRow();
        
        // When
        row.setReportNr(null);
        row.setReportDate(null);
        row.setDivision(null);
        
        // Then
        assertThat(row.getReportNr()).isNull();
        assertThat(row.getReportDate()).isNull();
        assertThat(row.getDivision()).isNull();
    }

    @Test
    void shouldBeDefaultNull() {
        // Given
        ParsedNumbersRow row = new ParsedNumbersRow();
        
        // Then
        assertThat(row.getReportNr()).isNull();
        assertThat(row.getReportDate()).isNull();
        assertThat(row.getDivision()).isNull();
    }
}
