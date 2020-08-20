/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.batch.jobs;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Abstract batch job that provides correct execution flow and the methods to achieve optimal performance.
 *
 * @param <ENT> the entity type being processed.
 */
public abstract class AbstractBatchJob<ENT> {
	private static final Logger LOG = LoggerFactory.getLogger(AbstractBatchJob.class);

	//injected via Spring
	private PersistenceEngine persistenceEngine;
	private AbstractBatchProcessor<ENT> batchProcessor;
	private int configBatchSize;

	//counters
	private int firstResultIndex; //the pointer for getting a next batch in case of errors
	private int numOfProcessedRecords; //holds the total of (successfully or not) processed records
	private int numOfSuccessfullyProcessedRecords;
	private int numOfSkippedRecords;
	private int numOfFailedRecords;
	/**
	 * Return the job name.
	 * @return the job name.
	 */
	protected abstract String getJobName();

	/**
	 * For queries with IN (:list) clause, this method must be implemented to provide a collection of list-parameter values.
	 *
	 * @param <LISTPARAM> the type of list parameter values.
	 * @return the collection of list parameter values.
	 */
	protected <LISTPARAM> Collection<LISTPARAM> getListParameterValues() {
		return null;
	}

	/**
	 * For queries that require external parameters this method must be implemented.
	 *
	 * @return an array of external parameters
	 */
	protected Object[] getParameters() {
		return null;
	}

	/**
	 * Provide the JPQL query that returns a list of records to process.
	 * @return the JPQL query
	 */
	protected abstract String getBatchJPQLQuery();

	/**
	 * This method is called by Quartz and executes a job.
	 */
	public void execute() {
		final long startTime = System.currentTimeMillis();

		initCounters();

		LOG.debug("Start {} quartz job at: {} ", getJobName(), new Date(startTime));

		try {
			while (true) {
				List<ENT> batchToProcess = fetchBatchOfRecords();

				int currentBatchSize = batchToProcess.size();

				if (currentBatchSize == 0) {
					break;
				}

				processBatch(batchToProcess);

				if (currentBatchSize < configBatchSize) {
					break;
				}
			}

		} catch (Exception e) {
			LOG.error(String.format("%s quartz job failed", getJobName()), e);
		}

		printDebugInfo(startTime);
	}

	/*
	 * Retrieve a batch of records to process.
	 */
	private List<ENT> fetchBatchOfRecords() {
		if (getListParameterValues() == null) {
			return getPersistenceEngine().retrieveByNamedQuery(getBatchJPQLQuery(), getParameters(), firstResultIndex, configBatchSize);
		}

		return getPersistenceEngine()
				.retrieveByNamedQueryWithList(getBatchJPQLQuery(), LIST_PARAMETER_NAME, getListParameterValues(),
						getParameters(), firstResultIndex, configBatchSize);
	}

	private void processBatch(final List<ENT> batch) {
		/* Processing a batch may fail due to various reasons but we don't want to stop processing.
		   Simply, log the error and move on
		 */

		//preserve the correct batch size, because the batch can be modified in the "preProcessBatch" method
		int currentBatchSize = batch.size();

		BatchProcessingResult batchProcessingResult;
		int numOfSkippedRecordsPerBatch = 0;

		try {
			numOfSkippedRecordsPerBatch = batchProcessor.processBatch(batch);
		} catch (Exception e) {
			batchProcessor.createBatchError(e.getMessage(), currentBatchSize, batch);

			LOG.error("Error occurred while processing a batch", e);
			LOG.error("Batch result error info\n {}", batchProcessor.getBatchProcessingError());
		} finally {
			batchProcessingResult = batchProcessor.createBatchResult(currentBatchSize, numOfSkippedRecordsPerBatch);
		}

		/*The batch processing may fail due to a temporary problem (lost db connection, network etc) and we don't want to
		 fail the whole job. The failed batch will be picked-up in the next run
		*/
		firstResultIndex += batchProcessingResult.getNumberOfSkippedRecords();
		numOfProcessedRecords += batchProcessingResult.getTotalProcessed();
		numOfSuccessfullyProcessedRecords += batchProcessingResult.getNumberOfSuccessfullyProcessedRecords();
		numOfSkippedRecords += batchProcessingResult.getNumberOfSkippedRecords();
		numOfFailedRecords += batchProcessingResult.getNumberOfFailedRecords();
	}

	private void printDebugInfo(final long startTime) {
		LOG.info("{} quartz job completed in {}ms\nProcessed {} records\nNumber of successfully processed records: {}\nNumber of skipped records: "
						+ "{}\nNumber of failed records: {}",
				getJobName(), (System.currentTimeMillis() - startTime), numOfProcessedRecords, numOfSuccessfullyProcessedRecords,
				numOfSkippedRecords, numOfFailedRecords);
	}

	private void initCounters() {
		numOfProcessedRecords = 0;
		firstResultIndex = 0;
		numOfSuccessfullyProcessedRecords = 0;
		numOfSkippedRecords = 0;
		numOfFailedRecords = 0;
	}

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	public PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	public void setConfigBatchSize(final SettingValueProvider<Integer> batchSizeProvider) {
		this.configBatchSize = batchSizeProvider.get();
	}

	public void setBatchProcessor(final AbstractBatchProcessor<ENT> batchProcessor) {
		this.batchProcessor = batchProcessor;
	}
}
