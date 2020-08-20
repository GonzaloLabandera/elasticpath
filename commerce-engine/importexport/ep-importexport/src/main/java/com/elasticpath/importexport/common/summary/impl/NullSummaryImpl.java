/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.importexport.common.summary.impl;

import java.util.List;
import java.util.Map;

import com.elasticpath.common.dto.Dto;
import com.elasticpath.importexport.common.summary.Summary;
import com.elasticpath.importexport.common.types.JobType;
import com.elasticpath.importexport.common.util.Message;
import com.elasticpath.importexport.common.util.Timer;

/**
 * Null object implementation of Summary interface.
 */
public class NullSummaryImpl implements Summary {
	@Override
	public void addToCounter(final JobType counter) {
		// Do nothing
	}

	@Override
	public void addToCounter(final JobType counter, final int qty) {
		// Do nothing
	}

	@Override
	public Timer.Time getElapsedTime() {
		return null;
	}

	@Override
	public String getStartDate() {
		return null;
	}

	@Override
	public int sumAllCounters() {
		return 0;
	}

	@Override
	public List<Message> getFailures() {
		return null;
	}

	@Override
	public List<Message> getWarnings() {
		return null;
	}

	@Override
	public List<Message> getComments() {
		return null;
	}

	@Override
	public Map<JobType, Integer> getCounters() {
		return null;
	}

	@Override
	public boolean failuresExist() {
		return false;
	}

	@Override
	public void addFailedDtos(final List<Dto> commitUnitDtos) {
		// Do nothing
	}

	@Override
	public List<Dto> getFailedDtos() {
		return null;
	}

	@Override
	public void clearFailedDtos() {
		// Do nothing
	}

	@Override
	public void addAddedToChangeSetCount(final int count) {
		// Do nothing
	}

	@Override
	public int getAddedToChangeSetCount() {
		return 0;
	}
}
