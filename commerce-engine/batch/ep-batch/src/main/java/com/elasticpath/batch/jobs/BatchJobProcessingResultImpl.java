/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs;

/**
 * This class encapsulates the number of (un)successfully processed records and the error, per batch.
 */
public final class BatchJobProcessingResultImpl implements BatchProcessingResult {

	private int numberOfSuccessfullyProcessedRecords;
	private int numberOfFailedRecords;
	//the number of skipped records during pre-processing phase or the batch size in case of errors
	private final int numberOfSkippedRecords;

	private final BatchJobProcessingError error;

	private BatchJobProcessingResultImpl(final int batchSize, final int numberOfSkippedRecords, final BatchJobProcessingError batchProcessingError) {
		if (batchProcessingError == null) {
			this.numberOfSuccessfullyProcessedRecords =  batchSize - numberOfSkippedRecords;
			this.numberOfSkippedRecords = numberOfSkippedRecords;
		} else {
			this.numberOfFailedRecords = batchSize;
			this.numberOfSkippedRecords = numberOfFailedRecords;
		}

		error = batchProcessingError;
	}
	/**
	 * Static method for returning a new instance with initial parameters.
	 *
	 * @param numberOfProcessedRecords the number of processed records
	 * @param numberOfSkippedRecords the number of skipped records
	 * @param batchProcessingError the batch processing error
	 * @return a new instance of  {@link BatchJobProcessingResultImpl}
	 */
	@SuppressWarnings("PMD.ShortMethodName")
	public static BatchJobProcessingResultImpl of(final int numberOfProcessedRecords, final int numberOfSkippedRecords,
												  final BatchJobProcessingError batchProcessingError) {

		return new BatchJobProcessingResultImpl(numberOfProcessedRecords, numberOfSkippedRecords, batchProcessingError);
	}

	@Override
	public int getNumberOfSuccessfullyProcessedRecords() {
		return this.numberOfSuccessfullyProcessedRecords;
	}

	@Override
	public BatchJobProcessingError getError() {
		return this.error;
	}

	@Override
	public int getNumberOfFailedRecords() {
		return this.numberOfFailedRecords;
	}

	@Override
	public int getNumberOfSkippedRecords() {
		return numberOfSkippedRecords;
	}
}
