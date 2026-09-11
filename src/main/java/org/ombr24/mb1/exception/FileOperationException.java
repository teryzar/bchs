package org.ombr24.mb1.exception;

/**
 * Виняток для помилок роботи з файлами
 */
public class FileOperationException extends RuntimeException {

    public FileOperationException(String message) {
        super(message);
    }

    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
