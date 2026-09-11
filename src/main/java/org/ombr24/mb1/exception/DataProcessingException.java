package org.ombr24.mb1.exception;

/**
 * Виняток для помилок обробки даних
 */
public class DataProcessingException extends RuntimeException {

    public DataProcessingException(String message) {
        super(message);
    }

    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
