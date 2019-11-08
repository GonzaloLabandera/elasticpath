/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.persistence.api;

import org.apache.openjpa.persistence.FetchPlan;

/**
 * A tagging interface for a load tuner.
 */
public interface LoadTuner {
	/**
	 * Returns <code>true</code> if this load tuner is super set of the given load tuner, otherwise, <code>false</code>.
	 *
	 * @param loadTuner the load tuner
	 * @return <code>true</code> if this load tuner is super set of the given load tuner, otherwise, <code>false</code>
	 */
	boolean contains(LoadTuner loadTuner);

	/**
	 * Merges the given load tuner with this one and returns the merged load tuner.
	 *
	 * @param loadTuner the load tuner
	 * @return the merged load tuner
	 */
	LoadTuner merge(LoadTuner loadTuner);

	/**
	 * Load tuners are responsible for fetch plan configuration.
	 *
	 * @param fetchPlan the fetch plan to configure.
	 */
	default void configure(FetchPlan fetchPlan) {
		//do nothing
	}
}
