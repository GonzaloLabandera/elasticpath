/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.exception;

/**
 * Thrown when amount of attempts is more than a specified threshold.
 */
public class OverflowAttemptsException extends RuntimeException {

    /**
     * Serial version id.
     */
    private static final long serialVersionUID = 5000000001L;

    /**
     * Constructor.
     *
     * @param message - error message of exception.
     * @param throwable parameter, that added to stacktrace of exception.
     */
    public OverflowAttemptsException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
