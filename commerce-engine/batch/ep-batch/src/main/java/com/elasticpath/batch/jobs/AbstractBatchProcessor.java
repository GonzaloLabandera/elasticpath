/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs;

import java.util.List;

import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * A single transactional unit for processing a single batch of records.
 *
 * @param <ENT> the type of processing entity.
 */
public abstract class AbstractBatchProcessor<ENT> {

	private PersistenceEngine persistenceEngine;
	private BatchJobProcessingResultImpl batchResult;

	/**
	 * Execute one or more bulk (DML) operations for the given batch of records.
	 * @param batch the list of records to be execute bulk operations on.
	 */
	protected abstract void executeBulkOperations(List<ENT> batch);

	/**
	 * Pre-process a batch (e.g. filter out redundant records that couldn't be filtered via JPQL query).
	 *
	 * @param batch the list of records to pre-process
	 * @return The number of skipped records. This number will be used to move the first-record index in select queries, to skip those that can't be
	 * processed.
	 */
	protected int preProcessBatch(final List<ENT> batch) {
		//pre-process the batch, if required
		return 0;
	}


	/**
	 * A managed transaction will be automatically started when this method is called from {@link AbstractBatchJob}.
	 * The batch may be pre-processed here (e.g. final filtration etc) before final execution.
	 * Any error that may occur will be handled in {@link AbstractBatchJob}.
	 *
	 * @param batch the batch of records process.
	 * @return number of skipped (non-processed batch elements) records, if any. This number will be used to move fetching index.
	 */
	protected int processBatch(final List<ENT> batch) {
		int numOfSkippedRecords = preProcessBatch(batch);
		executeBulkOperations(batch);

		return numOfSkippedRecords;
	}

	public BatchJobProcessingError getBatchProcessingError() {
		return batchResult.getError();
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	public PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	/**
	 * Create a batch error whenever an exception is thrown. The {@link BatchJobProcessingError} will be reused in the final
	 * {@link BatchProcessingResult}.
	 *
	 * @param errorMessage the error message.
	 * @param batchSize the actual batch size.
	 * @param batch the batch.
	 */
	protected void createBatchError(final String errorMessage, final int batchSize, final List<ENT> batch) {
		BatchJobProcessingError processingError = BatchJobProcessingError.of(errorMessage, batch.toString());
		batchResult = BatchJobProcessingResultImpl.of(batchSize, 0, processingError);
	}

	/**
	 * Create a batch processing result. In case that error has occurred previously, it will be included in the result.
	 *
	 * @param batchSize the actual batch size.
	 * @param numberOfSkippedRecords the number of skipped records.
	 * @return {@link BatchProcessingResult}
	 */
	protected BatchProcessingResult createBatchResult(final int batchSize, final int numberOfSkippedRecords) {
		BatchJobProcessingError processingError = batchResult == null
				? null
				: batchResult.getError();

		return BatchJobProcessingResultImpl.of(batchSize, numberOfSkippedRecords, processingError);
	}
}
