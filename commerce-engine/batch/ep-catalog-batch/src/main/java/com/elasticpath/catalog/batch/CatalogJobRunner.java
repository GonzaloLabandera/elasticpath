/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.batch;

import java.util.Map;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

/**
 * Represents an interface for Catalog Batch jobs runner.
 */
public interface CatalogJobRunner {

	/**
	 * Runs job for particular event type.
	 *
	 * @param jobName    name of job.
	 * @param parameters is map of job parameters.
	 * @return JobExecution object
	 * @throws JobExecutionAlreadyRunningException if the JobInstance identified
	 *                                             by the properties already has an execution running.
	 * @throws IllegalArgumentException            if the job or jobInstanceProperties are
	 *                                             null.
	 * @throws JobRestartException                 if the job has been run before and
	 *                                             circumstances that preclude a re-start.
	 * @throws JobInstanceAlreadyCompleteException if the job has been run
	 *                                             before with the same parameters and completed successfully
	 * @throws JobParametersInvalidException       if the parameters are not valid for
	 *                                             this job
	 */
	JobExecution run(String jobName, Map<String, Object> parameters) throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
			JobRestartException,
			JobInstanceAlreadyCompleteException;

}
