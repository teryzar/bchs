package org.ombr24.mb1.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилітний клас для читання даних з Excel комірок
 * Централізує логіку обробки різних типів комірок
 */
public class ExcelCellReader {

    private static final Logger logger = LoggerFactory.getLogger(ExcelCellReader.class);
    private final DataFormatter formatter = new DataFormatter();

    /**
     * Читає текстове значення з комірки
     * 
     * @param row рядок Excel
     * @param columnIndex індекс колонки
     * @return текстове значення або порожній рядок якщо комірка порожня
     */
    public String readString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : cell.getStringCellValue().trim();
    }

    /**
     * Форматує значення комірки за допомогою DataFormatter
     * Автоматично визначає тип даних та форматує відповідно
     * 
     * @param row рядок Excel
     * @param columnIndex індекс колонки
     * @return форматоване значення або порожній рядок
     */
    public String formatCell(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    /**
     * Універсальний метод читання значення комірки
     * Спочатку намагається прочитати як текст, потім як число
     * 
     * @param row рядок Excel
     * @param columnIndex індекс колонки
     * @return значення комірки як рядок або порожній рядок
     */
    public String getCellValueAsString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }

        try {
            return cell.getStringCellValue().trim();
        } catch (IllegalStateException e) {
            // Якщо комірка не містить текст, спробувати інші типи
            try {
                return String.valueOf(cell.getNumericCellValue());
            } catch (IllegalStateException ex) {
                logger.warn("Не вдалося прочитати значення комірки [{}:{}]", 
                    row.getRowNum(), columnIndex);
                return "";
            }
        }
    }

    /**
     * Читає числове значення з комірки
     * 
     * @param row рядок Excel
     * @param columnIndex індекс колонки
     * @return числове значення або 0 якщо комірка порожня/некоректна
     */
    public double readNumeric(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return 0;
        }

        try {
            return cell.getNumericCellValue();
        } catch (IllegalStateException e) {
            logger.warn("Не вдалося прочитати числове значення з комірки [{}:{}]", 
                row.getRowNum(), columnIndex);
            return 0;
        }
    }

    /**
     * Перевіряє чи комірка порожня
     * 
     * @param row рядок Excel
     * @param columnIndex індекс колонки
     * @return true якщо комірка порожня або null
     */
    public boolean isCellEmpty(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return true;
        }

        return formatter.formatCellValue(cell).trim().isEmpty();
    }
}
