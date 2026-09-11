package org.ombr24.mb1.exception;

/**
 * Виняток для некоректних вхідних даних
 */
public class InvalidInputException extends RuntimeException {

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
