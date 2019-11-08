/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch.job;

import static com.elasticpath.catalog.batch.CatalogJobRunnerImpl.CLEAN_UP_DATABASE_FLAG;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

import com.elasticpath.catalog.spi.service.CatalogService;

/**
 * Tests for {@link DatabaseCleanupTasklet}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DatabaseCleanupTaskletTest {

	private static final String PROJECTION_TYPE = "option";

	@Test
	public void testShouldCallCatalogServiceRemoveAllWhenCleanUpDatabaseFlagIsTrue() throws Exception {
		final CatalogService catalogService = mock(CatalogService.class);
		final StepContribution stepContribution = mock(StepContribution.class);
		final ChunkContext chunkContext = mockChunkContext(Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, "true"));

		final DatabaseCleanupTasklet databaseCleanupTasklet = new DatabaseCleanupTasklet(catalogService, PROJECTION_TYPE);

		databaseCleanupTasklet.execute(stepContribution, chunkContext);

		verify(catalogService).removeAll(PROJECTION_TYPE);
	}

	@Test
	public void testShouldNotCallCatalogServiceRemoveAllWhenCleanUpDatabaseFlagIsFalse() throws Exception {
		final CatalogService catalogService = mock(CatalogService.class);
		final StepContribution stepContribution = mock(StepContribution.class);
		final ChunkContext chunkContext = mockChunkContext(Collections.singletonMap(CLEAN_UP_DATABASE_FLAG, "false"));

		final DatabaseCleanupTasklet databaseCleanupTasklet = new DatabaseCleanupTasklet(catalogService, PROJECTION_TYPE);

		databaseCleanupTasklet.execute(stepContribution, chunkContext);

		verify(catalogService, never()).removeAll(PROJECTION_TYPE);
	}

	private ChunkContext mockChunkContext(final Map<String, String> parameters) {
		final JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		parameters.forEach(jobParametersBuilder::addString);

		final JobParameters jobParameters = jobParametersBuilder.toJobParameters();

		final JobExecution jobExecution = mock(JobExecution.class);
		when(jobExecution.getJobParameters()).thenReturn(jobParameters);

		final StepExecution stepExecution = mock(StepExecution.class);
		when(stepExecution.getJobExecution()).thenReturn(jobExecution);

		final StepContext stepContext = mock(StepContext.class);
		when(stepContext.getStepExecution()).thenReturn(stepExecution);

		final ChunkContext chunkContext = mock(ChunkContext.class);
		when(chunkContext.getStepContext()).thenReturn(stepContext);

		return chunkContext;
	}

}