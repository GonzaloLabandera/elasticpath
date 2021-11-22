/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.job;

import static com.elasticpath.catalog.batch.CatalogJobRunnerImpl.CLEAN_UP_DATABASE_FLAG;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * Database cleanup tasklet. Removes projections from projection database of configured type if requested.
 */
public class DatabaseCleanupTasklet implements Tasklet {

	private static final Logger LOGGER = LogManager.getLogger(DatabaseCleanupTasklet.class);

	private final CatalogService catalogService;
	private final String projectionType;

	/**
	 * Constructor.
	 *
	 * @param catalogService catalog service.
	 * @param projectionType type of projection.
	 */
	public DatabaseCleanupTasklet(final CatalogService catalogService, final String projectionType) {
		this.catalogService = catalogService;
		this.projectionType = projectionType;
	}

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
		final String cleanUpDatabase = getJobParameter(chunkContext);

		if (Boolean.parseBoolean(cleanUpDatabase)) {
			int removed = catalogService.removeAll(projectionType);
			LOGGER.debug(removed + " records have been removed of type " + projectionType);
		}

		return RepeatStatus.FINISHED;
	}

	/**
	 * Extracts job parameter with parameterName from chunkContext.
	 *
	 * @param chunkContext chunk context.
	 * @return string value of job parameter.
	 */
	private String getJobParameter(final ChunkContext chunkContext) {
		return chunkContext.getStepContext().getStepExecution().getJobExecution().getJobParameters().getString(CLEAN_UP_DATABASE_FLAG);
	}

}
