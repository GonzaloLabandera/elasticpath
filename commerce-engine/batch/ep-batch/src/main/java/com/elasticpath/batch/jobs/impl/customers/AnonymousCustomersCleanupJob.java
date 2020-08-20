/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.batch.jobs.impl.customers;

import java.util.Date;

import org.apache.commons.lang.time.DateUtils;

import com.elasticpath.batch.jobs.AbstractBatchJob;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.settings.provider.SettingValueProvider;

/**
 * Job for purging expired anonymous customers without orders.
 */
public class AnonymousCustomersCleanupJob extends AbstractBatchJob<Long> {

	private TimeService timeService;
	private int maxDaysHistory;

	@Override
	protected String getBatchJPQLQuery() {
		return "FIND_EXPIRED_ANONYMOUS_CUSTOMERS_UIDS_WITHOUT_ORDERS";
	}

	@Override
	protected Object[] getParameters() {
		return new Object[] { getCandidateRemovalDate() };
	}

	@Override
	protected String getJobName() {
		return "Anonymous Customers Cleanup";
	}

	/**
	 * Gets the candidate removal date.
	 *
	 * @return the candidate removal date
	 */
	protected Date getCandidateRemovalDate() {
		return DateUtils.addDays(getTimeService().getCurrentTime(), -maxDaysHistory);
	}

	/**
	 * Set maximum history days using {@link SettingValueProvider}.
	 *
	 * @param maxDaysHistoryProvider the maximum history days setting provider.
	 */
	public void setMaxDaysHistory(final SettingValueProvider<Integer> maxDaysHistoryProvider) {
		this.maxDaysHistory = maxDaysHistoryProvider.get();
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}

	public TimeService getTimeService() {
		return timeService;
	}
}
