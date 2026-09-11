package org.ombr24.mb1.excel;

import org.apache.poi.ss.usermodel.*;
import org.ombr24.mb1.domain.ParsedRow;

public class ExcelRowMapper {

    private final DataFormatter formatter = new DataFormatter();

    public ParsedRow map(Row row, int posIndex) {
        return new ParsedRow(
                read(row, 0),
                read(row, 1),
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
