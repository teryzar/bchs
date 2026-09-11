package org.ombr24.mb1.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.ombr24.mb1.domain.ParsedAddedUnitRow;

public class AddedUnitMapper {
    private final DataFormatter formatter = new DataFormatter();

    public ParsedAddedUnitRow map(Row row, int posIndex) {
        return new ParsedAddedUnitRow(
                read(row, 1),
                read(row, 0),
                format(row, posIndex)
        );
    }

    private String read(Row r, int i) {
        Cell c = r.getCell(i);
        return c == null ? "" : c.getStringCellValue().trim();
    }

    private String format(Row r, int i) {
        Cell c = r.getCell(i);
        return c == null ? "" : formatter.formatCellValue(c).trim();
    }
}
