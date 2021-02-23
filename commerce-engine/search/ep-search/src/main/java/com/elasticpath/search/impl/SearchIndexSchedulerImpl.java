/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.search.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.quartz.Trigger;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * Search Index Scheduler Impl.
 */
public class SearchIndexSchedulerImpl extends SchedulerFactoryBean {

	private static final String PRIMARY = "PRIMARY";

	private static final String EP_SEARCH_MODE = "ep.search.mode";

	private static final Logger LOG = Logger.getLogger(SearchIndexSchedulerImpl.class);

	private List<Trigger> fallbackSearchTriggers;

	private Map<String, List<Trigger>> modeSearchTriggers;

	@Override
	public void afterPropertiesSet() throws Exception {
		Optional<String> searchMode = getSearchModeOptional();

		if (searchMode.isPresent()) {
			setTriggersBasedOnSearchMode(searchMode.get());
		} else {
			setFallbackTriggers();
		}
		super.afterPropertiesSet();
	}

	private void setFallbackTriggers() {
		LOG.warn("Deprecated ‘ep.search.triggers’ ep.properties value should be replaced with ‘ep.search.mode’ JVM system property");
		setTriggers(fallbackSearchTriggers.toArray(new Trigger[fallbackSearchTriggers.size()]));
	}

	private void setTriggersBasedOnSearchMode(final String searchMode) {
		if (modeSearchTriggers.keySet().contains(searchMode)) {
			setTriggers(modeSearchTriggers.get(searchMode).toArray(new Trigger[modeSearchTriggers.get(searchMode).size()]));
		} else {
			setDefaultTriggers();
		}
	}

	private void setDefaultTriggers() {
		setTriggers(modeSearchTriggers.get(PRIMARY).toArray(new Trigger[modeSearchTriggers.get(PRIMARY).size()]));
	}

	private Optional<String> getSearchModeOptional() {
		return Optional.ofNullable(System.getProperty(EP_SEARCH_MODE));
	}

	public void setFallbackSearchTriggers(final List<Trigger> fallbackSearchTriggers) {
		this.fallbackSearchTriggers = fallbackSearchTriggers;
	}

	public void setModeSearchTriggers(final Map<String, List<Trigger>> modeSearchTriggers) {
		this.modeSearchTriggers = modeSearchTriggers;
	}

	protected List<Trigger> getFallbackSearchTriggers() {
		return fallbackSearchTriggers;
	}

	protected Map<String, List<Trigger>> getModeSearchTriggers() {
		return modeSearchTriggers;
	}
}
