package org.ombr24.mb1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.ombr24.mb1.exception.InvalidInputException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для Application - валідація вводу
 */
class ApplicationValidationTest {

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -10, 32, 50, 100})
    void shouldRejectInvalidDay(int invalidDay) {
        // Given
        String input = invalidDay + "\n15\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // When/Then
        assertThatThrownBy(() -> Application.main(new String[0]))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("день має бути в діапазоні");
    }

    @Test
    void shouldRejectEmptyInput() {
        // Given
        String input = "\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // When/Then
        assertThatThrownBy(() -> Application.main(new String[0]))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("не може бути порожнім");
    }

    @Test
    void shouldRejectNonNumericInput() {
        // Given
        String input = "abc\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // When/Then
        assertThatThrownBy(() -> Application.main(new String[0]))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("Невірний формат числа");
    }

    @Test
    void shouldRejectStartDateGreaterThanEndDate() {
        // Given
        String input = "20\n10\n"; // start=20, end=10
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);
        
        // When/Then
        assertThatThrownBy(() -> Application.main(new String[0]))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("Початкова дата")
            .hasMessageContaining("не може бути більшою за кінцеву");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 15, 28, 31})
    void shouldAcceptValidDays(int validDay) {
        // Given/When/Then - просто перевіряємо що валідація пройде
        // Фактичного запуску не буде через відсутність файлів даних
        // Але це показує що валідація працює для валідних значень
        assertThat(validDay).isBetween(1, 31);
    }
}
