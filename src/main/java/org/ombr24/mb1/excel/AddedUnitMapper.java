package org.ombr24.mb1.excel;

import org.apache.poi.ss.usermodel.Row;
import org.ombr24.mb1.domain.ParsedAddedUnitRow;

/**
 * Mapper для перетворення Excel рядків приданих підрозділів в ParsedAddedUnitRow об'єкти
 */
public class AddedUnitMapper {
    
    private final ExcelCellReader cellReader = new ExcelCellReader();

    /**
     * Перетворює рядок Excel в ParsedAddedUnitRow
     * 
     * @param row рядок Excel
     * @param posIndex індекс колонки з інформацією про призначення
     * @return ParsedAddedUnitRow з даними про ім'я, звідки та куди приданий
     */
    public ParsedAddedUnitRow map(Row row, int posIndex) {
        return new ParsedAddedUnitRow(
                cellReader.readString(row, 1),  // ім'я
                cellReader.readString(row, 0),  // звідки
                cellReader.formatCell(row, posIndex)  // куди
        );
    }
}
