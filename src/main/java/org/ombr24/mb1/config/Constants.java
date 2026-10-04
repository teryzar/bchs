package org.ombr24.mb1.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Константи додатку для уникнення magic values
 */
public final class Constants {

    private Constants() {
        throw new UnsupportedOperationException("Utility class");
    }

    // Назви листів Excel для пропуску
    public static final Set<String> SKIP_SHEETS = new HashSet<>(Arrays.asList(
            "ПРИМІТКИ",
            "Управління",
            "Прикомандировані",
            "БРЕЗ",
            "оперативне підпорядкування"
    ));

    // Назва листа з доданими підрозділами
    public static final String ADDED_UNITS_SHEET = "оперативне підпорядкування";

    // Назва першого листа даних
    public static final String FIRST_DATA_SHEET = "1 мр";

    // Назва листа з номерами
    public static final String NUMBERS_SHEET = "numbers.xlsx";

    // Позиції для різних категорій
    public static final String POSITION_30 = "30";


    // Індекси стовпців Excel
    public static final int REPORT_NR_COLUMN = 0;
    public static final int REPORT_DATE_COLUMN = 1;
    public static final int DIVISION_COLUMN = 2;

    public static final int RANK_COLUMN = 0;
    public static final int NAME_COLUMN = 1;

    // Рядки Excel
    public static final int HEADER_ROW = 0;
    public static final int COMMANDER_ROW = 1;

    // Формати дат
    public static final String DATE_FORMAT = "dd.MM.yyyy";

    // Типи позицій для звітів
    public static final String POSITION_TYPE_TZ = "(ТЗ «";
    public static final String POSITION_TYPE_BP = "(БП «";
}
