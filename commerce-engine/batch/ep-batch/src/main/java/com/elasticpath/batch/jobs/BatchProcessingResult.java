/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs;

/**
 * This structure contains everything pertinent to the result of batch processing.
 */
public interface BatchProcessingResult {

	/**
	 * The number of successfully processed records per batch.
	 *
	 * @return the number of processed records.
	 */
	int getNumberOfSuccessfullyProcessedRecords();

	/**
	 * The number of records that couldn't be processed. It corresponds to the configured batch size.
	 *
	 * @return the number of failed records.
	 */
	int getNumberOfFailedRecords();

	/**
	 * During the preprocessing phase it's possible that one or more records can't be processed (due to additional filtering).
	 * To avoid an infinite loop of processing of non-processable records, the first-record index must be incremented in the next select.
	 *
	 * @return the number of skipped records.
	 */
	int getNumberOfSkippedRecords();

	/**
	 * Return the error.
	 * @return {@link BatchJobProcessingError}.
	 */
	BatchJobProcessingError getError();

	default int getTotalProcessed() {
		return getNumberOfSuccessfullyProcessedRecords() + getNumberOfFailedRecords();
	}
}
