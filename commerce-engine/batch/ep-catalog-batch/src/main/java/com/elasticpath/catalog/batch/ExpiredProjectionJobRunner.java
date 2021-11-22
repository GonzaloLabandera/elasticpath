/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.batch;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import com.elasticpath.catalog.spi.service.CatalogService;
import com.elasticpath.service.misc.TimeService;

/**
 * Run expired job and schedule next expired job.
 */
@SuppressWarnings("rawtypes")
public class ExpiredProjectionJobRunner implements ApplicationListener, Runnable {
	private static final Logger LOGGER = LogManager.getLogger(ExpiredProjectionJobRunner.class);

	private final Job job;
	private final JobLauncher jobLauncher;
	private final TimeService timeService;
	private final CatalogService catalogService;
	private final int maxDelay;
	private final int minDelay;
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private JobExecution previousJobStatus;

	/**
	 * Constructor.
	 *
	 * @param job            is expired job to launch.
	 * @param jobLauncher    Spring Batch job launcher.
	 * @param timeService    {@link TimeService}.
	 * @param catalogService {@link CatalogService}.
	 * @param maxDelay       is max delay expired job previousJobStatus in minutes.
	 * @param minDelay       is min delay expired job previousJobStatus in minutes.
	 */
	public ExpiredProjectionJobRunner(final Job job, final JobLauncher jobLauncher, final TimeService timeService,
									  final CatalogService catalogService, final int maxDelay, final int minDelay) {
		this.job = job;
		this.jobLauncher = jobLauncher;
		this.timeService = timeService;
		this.catalogService = catalogService;
		this.maxDelay = maxDelay;
		this.minDelay = minDelay;
	}

	@Override
	public void onApplicationEvent(final ApplicationEvent event) {
		if (event instanceof ContextRefreshedEvent) {
			run();
		}

		if (event instanceof ContextClosedEvent) {
			executor.shutdownNow();
		}
	}

	@Override
	public void run() {
		try {
			if (Objects.isNull(previousJobStatus) || !previousJobStatus.isRunning()) {
				previousJobStatus = jobLauncher.run(job, new JobParametersBuilder().addDate("time", timeService.getCurrentTime()).toJobParameters());
			}
		} catch (Exception e) {
			LOGGER.error("Exception during Projection expired job", e);
		} finally {
			scheduleNextJob();
		}
	}

	private void scheduleNextJob() {
		long delay = 0;

		try {
			final Optional<Date> expiredTime = catalogService.readNearestExpiredTime();
			final Date upperBound = DateUtils.addMinutes(timeService.getCurrentTime(), maxDelay);
			final Date lowerBound = DateUtils.addMinutes(timeService.getCurrentTime(), minDelay);

			Date scheduled = upperBound;

			if (expiredTime.isPresent() && expiredTime.get().after(lowerBound) && expiredTime.get().before(upperBound)) {
				scheduled = expiredTime.get();
			}

			if (expiredTime.map(date -> date.before(lowerBound)).orElse(false)) {
				scheduled = lowerBound;
			}

			delay = Math.abs(scheduled.getTime() - timeService.getCurrentTime().getTime());

		} catch (Exception e) {
			LOGGER.error("Exception during scheduling of Projection expired job", e);
		} finally {
			final long resultDelay = delay == 0
					? TimeUnit.MINUTES.toMillis(minDelay)
					: delay;

			executor.schedule(this, resultDelay, TimeUnit.MILLISECONDS);
		}
	}
}
