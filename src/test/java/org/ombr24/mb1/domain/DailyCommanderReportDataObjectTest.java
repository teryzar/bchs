package org.ombr24.mb1.domain;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для DailyCommanderReportDataObject
 */
class DailyCommanderReportDataObjectTest {

    @Test
    void shouldCreateAndSetAllFields() {
        // Given
        DailyCommanderReportDataObject report = new DailyCommanderReportDataObject();
        List<String> list1 = Arrays.asList("Офіцер 1", "Офіцер 2");
        List<String> list2 = Arrays.asList("Офіцер 3");
        
        // When
        report.setDivision("1 мр");
        report.setReportDate("15.05.2024");
        report.setBrNr("BR001");
        report.setList1(list1);
        report.setList2(list2);
        report.setList3(Arrays.asList());
        report.setList4(Arrays.asList());
        report.setList5(Arrays.asList());
        report.setList6(Arrays.asList());
        report.setPosition("Командир батальйону");
        report.setRank("підполковник");
        report.setName("Іванов І.І.");
        report.setNumber("001");
        report.setReporter("Звітувач");
        
        // Then
        assertThat(report.getDivision()).isEqualTo("1 мр");
        assertThat(report.getReportDate()).isEqualTo("15.05.2024");
        assertThat(report.getBrNr()).isEqualTo("BR001");
        assertThat(report.getList1()).hasSize(2).containsExactly("Офіцер 1", "Офіцер 2");
        assertThat(report.getList2()).hasSize(1).containsExactly("Офіцер 3");
        assertThat(report.getList3()).isEmpty();
        assertThat(report.getPosition()).isEqualTo("Командир батальйону");
        assertThat(report.getRank()).isEqualTo("підполковник");
        assertThat(report.getName()).isEqualTo("Іванов І.І.");
        assertThat(report.getNumber()).isEqualTo("001");
        assertThat(report.getReporter()).isEqualTo("Звітувач");
    }

    @Test
    void shouldAllowNullValues() {
        // Given
        DailyCommanderReportDataObject report = new DailyCommanderReportDataObject();
        
        // When
        report.setDivision(null);
        report.setReportDate(null);
        report.setList1(null);
        
        // Then
        assertThat(report.getDivision()).isNull();
        assertThat(report.getReportDate()).isNull();
        assertThat(report.getList1()).isNull();
    }

    @Test
    void shouldBeDefaultNull() {
        // Given
        DailyCommanderReportDataObject report = new DailyCommanderReportDataObject();
        
        // Then
        assertThat(report.getDivision()).isNull();
        assertThat(report.getReportDate()).isNull();
        assertThat(report.getBrNr()).isNull();
        assertThat(report.getList1()).isNull();
        assertThat(report.getList2()).isNull();
        assertThat(report.getList3()).isNull();
        assertThat(report.getList4()).isNull();
        assertThat(report.getList5()).isNull();
        assertThat(report.getList6()).isNull();
        assertThat(report.getPosition()).isNull();
        assertThat(report.getRank()).isNull();
        assertThat(report.getName()).isNull();
        assertThat(report.getNumber()).isNull();
        assertThat(report.getReporter()).isNull();
    }

    @Test
    void shouldHandleEmptyLists() {
        // Given
        DailyCommanderReportDataObject report = new DailyCommanderReportDataObject();
        
        // When
        report.setList1(Arrays.asList());
        report.setList2(Arrays.asList());
        report.setList3(Arrays.asList());
        report.setList4(Arrays.asList());
        report.setList5(Arrays.asList());
        report.setList6(Arrays.asList());
        
        // Then
        assertThat(report.getList1()).isEmpty();
        assertThat(report.getList2()).isEmpty();
        assertThat(report.getList3()).isEmpty();
        assertThat(report.getList4()).isEmpty();
        assertThat(report.getList5()).isEmpty();
        assertThat(report.getList6()).isEmpty();
    }
}
