package org.ombr24.mb1.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Тести для exception класів
 */
class ExceptionTest {

    @Test
    void shouldCreateDataProcessingExceptionWithMessage() {
        // Given
        String message = "Помилка обробки даних";
        
        // When
        DataProcessingException exception = new DataProcessingException(message);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateDataProcessingExceptionWithCause() {
        // Given
        String message = "Помилка обробки даних";
        Throwable cause = new RuntimeException("Внутрішня помилка");
        
        // When
        DataProcessingException exception = new DataProcessingException(message, cause);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void shouldCreateFileOperationExceptionWithMessage() {
        // Given
        String message = "Файл не знайдено";
        
        // When
        FileOperationException exception = new FileOperationException(message);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void shouldCreateFileOperationExceptionWithCause() {
        // Given
        String message = "Помилка читання файлу";
        Throwable cause = new java.io.IOException("Немає доступу");
        
        // When
        FileOperationException exception = new FileOperationException(message, cause);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getCause()).isInstanceOf(java.io.IOException.class);
    }

    @Test
    void shouldCreateInvalidInputExceptionWithMessage() {
        // Given
        String message = "Невірний ввід";
        
        // When
        InvalidInputException exception = new InvalidInputException(message);
        
        // Then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void shouldBeRuntimeExceptions() {
        // Then
        assertThat(new DataProcessingException("test"))
            .isInstanceOf(RuntimeException.class);
        assertThat(new FileOperationException("test"))
            .isInstanceOf(RuntimeException.class);
        assertThat(new InvalidInputException("test"))
            .isInstanceOf(RuntimeException.class);
    }
}
