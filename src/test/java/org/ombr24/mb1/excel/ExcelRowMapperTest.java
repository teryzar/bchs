package org.ombr24.mb1.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ombr24.mb1.domain.ParsedRow;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для ExcelRowMapper
 */
class ExcelRowMapperTest {

    private ExcelRowMapper mapper;
    private Workbook workbook;
    private Sheet sheet;

    @BeforeEach
    void setUp() {
        mapper = new ExcelRowMapper();
        workbook = new XSSFWorkbook();
        sheet = workbook.createSheet("Test");
    }

    @Test
    void shouldMapRowWithAllFields() {
        // Given
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("підполковник");
        row.createCell(1).setCellValue("Іванов Іван");
        row.createCell(2).setCellValue("70");
        
        // When
        ParsedRow result = mapper.map(row, 2);
        
        // Then
        assertThat(result.getRank()).isEqualTo("підполковник");
        assertThat(result.getName()).isEqualTo("Іванов Іван");
        assertThat(result.getPosition()).isEqualTo("70");
        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    void shouldHandleNumericPosition() {
        // Given
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("майор");
        row.createCell(1).setCellValue("Петров");
        row.createCell(2).setCellValue(100.0);
        
        // When
        ParsedRow result = mapper.map(row, 2);
        
        // Then
        assertThat(result.getPosition()).isEqualTo("100");
    }

    @Test
    void shouldHandleEmptyCells() {
        // Given
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("");
        // cell 1 is null
        row.createCell(2).setCellValue("");
        
        // When
        ParsedRow result = mapper.map(row, 2);
        
        // Then
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldTrimWhitespace() {
        // Given
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("  капітан  ");
        row.createCell(1).setCellValue("  Сидоренко  ");
        row.createCell(2).setCellValue("  50  ");
        
        // When
        ParsedRow result = mapper.map(row, 2);
        
        // Then
        assertThat(result.getRank()).isEqualTo("капітан");
        assertThat(result.getName()).isEqualTo("Сидоренко");
        assertThat(result.getPosition()).isEqualTo("50");
    }

    @Test
    void shouldHandleNullCells() {
        // Given
        Row row = sheet.createRow(0);
        // Всі комірки null
        
        // When
        ParsedRow result = mapper.map(row, 2);
        
        // Then
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getRank()).isEmpty();
        assertThat(result.getName()).isEmpty();
        assertThat(result.getPosition()).isEmpty();
    }

    @Test
    void shouldMapWithDifferentPositionIndex() {
        // Given
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue("полковник");
        row.createCell(1).setCellValue("Коваленко");
        row.createCell(3).setCellValue("70");
        row.createCell(4).setCellValue("100");
        
        // When
        ParsedRow result1 = mapper.map(row, 3);
        ParsedRow result2 = mapper.map(row, 4);
        
        // Then
        assertThat(result1.getPosition()).isEqualTo("70");
        assertThat(result2.getPosition()).isEqualTo("100");
    }
}
