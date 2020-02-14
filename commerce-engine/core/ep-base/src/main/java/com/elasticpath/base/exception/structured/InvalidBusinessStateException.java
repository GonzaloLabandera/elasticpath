/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.base.exception.structured;

/**
 * Invalid business state exception represents a recoverable error and contains a structured error
 * message to give detailed information about the state which caused the error.
 */
public interface InvalidBusinessStateException extends StructuredErrorMessageException {

}
