package com.suspend.core.exception;

public class SuspendException extends RuntimeException {

    public SuspendException(String message) {
        super( message );
    }

    /**
     * Constructs a {@code SuspendException} using the given message and underlying cause.
     *
     * @param cause The underlying cause.
     */
    public SuspendException(Throwable cause) {
        super( cause );
    }

    /**
     * Constructs a {@code SuspendException} using the given message and underlying cause.
     *
     * @param message The message explaining the reason for the exception.
     * @param cause The underlying cause.
     */
    public SuspendException(String message, Throwable cause) {
        super( message, cause );
    }
}
