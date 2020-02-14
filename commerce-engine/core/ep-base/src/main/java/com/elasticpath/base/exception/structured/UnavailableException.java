/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.base.exception.structured;

/**
 * Unavailable exception represents an error when an object being looked up cannot be found or is
 * not available, and contains a structured error message to give detailed information about the error.
 */
public interface UnavailableException extends StructuredErrorMessageException {

}
