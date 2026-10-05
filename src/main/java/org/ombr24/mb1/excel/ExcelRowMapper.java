package org.ombr24.mb1.excel;

import org.apache.poi.ss.usermodel.Row;
import org.ombr24.mb1.domain.ParsedRow;

/**
 * Mapper для перетворення Excel рядків в ParsedRow об'єкти
 */
public class ExcelRowMapper {

    private final ExcelCellReader cellReader = new ExcelCellReader();

    /**
     * Перетворює рядок Excel в ParsedRow
     * 
     * @param row рядок Excel
     * @param posIndex індекс колонки з позицією
     * @return ParsedRow з даними про звання, ім'я та позицію
     */
    public ParsedRow map(Row row, int posIndex) {
        return new ParsedRow(
                cellReader.readString(row, 0),
                cellReader.readString(row, 1),
                cellReader.formatCell(row, posIndex)
        );
    }
}
