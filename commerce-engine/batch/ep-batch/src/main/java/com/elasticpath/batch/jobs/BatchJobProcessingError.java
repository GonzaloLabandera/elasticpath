/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs;

import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class encapsulates an error that occurred while processing one batch of records.
 */
public final class BatchJobProcessingError {
	private final String errorMessage;
	private final String csvIDs;

	private BatchJobProcessingError(final String errorMessage, final String csvIDs) {
		this.errorMessage = errorMessage;
		this.csvIDs = csvIDs;
	}
	/**
	 * Create a new instance of the error.
	 *
	 * @param errorMessage the actual error message.
	 * @param csvShipmentNumbers the list of CSV ids (e.g. uidPk, guid etc).
	 * @return a new error instance
	 */
	@SuppressWarnings("PMD.ShortMethodName")
	public static BatchJobProcessingError of(final String errorMessage, final String csvShipmentNumbers) {
		return new BatchJobProcessingError(errorMessage, csvShipmentNumbers);
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getCsvIDs() {
		return csvIDs;
	}

	@Override
	public String toString() {
		return new org.apache.commons.lang.builder.ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
				.append("errorMessage", getErrorMessage())
				.append("csvIDs", getCsvIDs())
				.toString();
	}
}
