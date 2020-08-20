/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.batch.jobs.impl.tokens;

import com.elasticpath.batch.jobs.AbstractBatchJob;
import com.elasticpath.service.misc.TimeService;

/**
 * Job to remove expired tokens.
 */
public class ExpiredOAuth2TokensCleanupJob extends AbstractBatchJob<Long> {

	private TimeService timeService;

	@Override
	protected String getJobName() {
		return "Expired OAuth2 Tokens Cleanup";
	}

	@Override
	protected String getBatchJPQLQuery() {
		return "FIND_TOKENS_BY_DATE";
	}

	@Override
	protected Object[] getParameters() {
		return new Object[] { getTimeService().getCurrentTime() };
	}

	public TimeService getTimeService() {
		return timeService;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}
}
