/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.batch.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;

import com.elasticpath.catalog.batch.ExpiredProjectionJobRunner;
import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * Tests for {@link ExpiredProjectionJobRunner}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ExpiredProjectionJobRunnerTest {

	private static final int WANTED_NUMBER_OF_INVOCATIONS = 4;
	private static final int MAX_DELAY = 100;
	private static final int MIN_DELAY = 5;
	private static final long JOB_ID = 0L;
	@Mock
	private Job job;
	@Mock
	private JobLauncher jobLauncher;
	@Mock
	private TimeService timeService;
	@Mock
	private CatalogService catalogService;

	@Test
	public void runJobTest() throws Exception {
		when(jobLauncher.run(eq(job), any(JobParameters.class))).thenReturn(new JobExecution(JOB_ID));
		when(timeService.getCurrentTime()).thenReturn(new Date());

		new ExpiredProjectionJobRunner(job, jobLauncher, timeService, catalogService, MAX_DELAY, MIN_DELAY).run();

		verify(jobLauncher).run(eq(job), any(JobParameters.class));
		verify(timeService, times(WANTED_NUMBER_OF_INVOCATIONS)).getCurrentTime();

	}
}