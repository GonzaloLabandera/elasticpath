/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch;

import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

/**
 * Represents an implementation of {@link CatalogJobRunner} for Option events.
 */
public class CatalogJobRunnerImpl implements CatalogJobRunner {

	/**
	 * Clean up database flag.
	 */
	public static final String CLEAN_UP_DATABASE_FLAG = "cleanUpDatabase";

	private final Map<String, Job> jobMap;
	private final JobLauncher jobLauncher;

	/**
	 * Constructor.
	 *
	 * @param jobMap      map of jobs. EventMessage guid is key, job is value.
	 * @param jobLauncher Spring Batch job launcher.
	 */
	public CatalogJobRunnerImpl(final Map<String, Job> jobMap, final JobLauncher jobLauncher) {
		this.jobMap = jobMap;
		this.jobLauncher = jobLauncher;
	}

	@Override
	public JobExecution run(final String jobName, final Map<String, Object> parameters) throws JobParametersInvalidException,
			JobExecutionAlreadyRunningException,
			JobRestartException, JobInstanceAlreadyCompleteException {
		final Job job = jobMap.get(jobName);

		final JobParameters jobParameters = convertToJobParameters(parameters);

		return jobLauncher.run(job, jobParameters);
	}

	private JobParameters convertToJobParameters(final Map<String, Object> parameters) {
		final String cleanUpDatabase = parameters.getOrDefault(CLEAN_UP_DATABASE_FLAG, false).toString();

		return new JobParametersBuilder()
				.addString(CLEAN_UP_DATABASE_FLAG, cleanUpDatabase)
				.addLong("time", System.currentTimeMillis())
				.toJobParameters();
	}

}
