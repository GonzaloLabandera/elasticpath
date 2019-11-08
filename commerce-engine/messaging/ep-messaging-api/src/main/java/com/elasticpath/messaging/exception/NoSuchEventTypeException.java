/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.messaging.exception;

/**
 * Exception that throws if EventType is not found.
 */
public class NoSuchEventTypeException extends RuntimeException {

    private static final long serialVersionUID = 5000000001L;


    /**
     * Creates instance of {@link NoSuchEventTypeException}.
     *
     * @param cause {@link Throwable}.
     */
    public NoSuchEventTypeException(final Throwable cause) {
        super(cause);
    }
}