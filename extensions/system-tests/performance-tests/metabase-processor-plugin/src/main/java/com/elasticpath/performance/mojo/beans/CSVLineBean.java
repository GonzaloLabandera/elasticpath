/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.beans;

import static com.elasticpath.performance.mojo.utils.Constants.FIVE;
import static com.elasticpath.performance.mojo.utils.Constants.FOUR;
import static com.elasticpath.performance.mojo.utils.Constants.ONE;
import static com.elasticpath.performance.mojo.utils.Constants.SEVEN;
import static com.elasticpath.performance.mojo.utils.Constants.SIX;
import static com.elasticpath.performance.mojo.utils.Constants.THREE;
import static com.elasticpath.performance.mojo.utils.Constants.TWO;
import static com.elasticpath.performance.mojo.utils.Constants.ZERO;

/**
    Wraps a csv line into a bean.

	Sample line:

	Test Id, Application, Scenario, Total number of DB selects,Total number of DB inserts,Total number of DB updates,Total number of DB deletes,
	DB Time delta

	46,cortex,Zoom with a big query on a cart with many items should not create DB overhead,1417,0,0,0,300
 */
public class CSVLineBean {
	private final String testId;
	private final String testName;
	private final String application;
	private final int totalDbSelects;
	private final int totalDbInserts;
	private final int totalDbUpdates;
	private final int totalDbDeletes;
	private final int totalDbTime;

	/**
	 * Custom constructor that excepts a csv line.
	 *
	 * @param csvLine the csv line.
	 */
	public CSVLineBean(final String csvLine) {
		String[] tokens = csvLine.split(",");
		this.testId = tokens[ZERO];
		this.application = tokens[ONE];
		this.testName = tokens[TWO];
		this.totalDbSelects = Integer.parseInt(tokens[THREE]);
		this.totalDbInserts = Integer.parseInt(tokens[FOUR]);
		this.totalDbUpdates = Integer.parseInt(tokens[FIVE]);
		this.totalDbDeletes = Integer.parseInt(tokens[SIX]);
		this.totalDbTime = Integer.parseInt(tokens[SEVEN]);
	}

	public String getTestId() {
		return testId;
	}

	public String getTestName() {
		return testName;
	}

	public String getApplication() {
		return application;
	}

	public int getTotalDbSelects() {
		return totalDbSelects;
	}

	public int getTotalDbInserts() {
		return totalDbInserts;
	}

	public int getTotalDbUpdates() {
		return totalDbUpdates;
	}

	public int getTotalDbDeletes() {
		return totalDbDeletes;
	}

	public int getTotalDbTime() {
		return totalDbTime;
	}
}
