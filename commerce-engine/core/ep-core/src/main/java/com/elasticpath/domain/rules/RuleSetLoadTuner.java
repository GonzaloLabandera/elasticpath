/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.domain.rules;

import com.elasticpath.persistence.api.LoadTuner;

/**
 * Represents a tuner to control rule set load. A rule set load tuner can be used in some services to fine control what data to be loaded for a
 * rule set. The main purpose is to achieve maximum performance for some specific performance-critical scenarios.
 */
public interface RuleSetLoadTuner extends LoadTuner {

	/**
	 * Return <code>true</code> if rules are requested.
	 *
	 * @return <code>true</code> if rules are requested.
	 */
	boolean isLoadingRules();

	/**
	 * Sets the flag of loading rules.
	 *
	 * @param flag sets it to <code>true</code> to request loading rules.
	 */
	void setLoadingRules(boolean flag);

	/**
	 * Returns <code>true</code> if this load tuner is super set of the given load tuner, otherwise, <code>false</code>.
	 *
	 * @param ruleSetLoadTuner the rule set load tuner
	 * @return <code>true</code> if this load tuner is super set of the given load tuner, otherwise, <code>false</code>
	 */
	boolean contains(RuleSetLoadTuner ruleSetLoadTuner);

	/**
	 * Merges the given load tuner with this one and returns the merged load tuner.
	 *
	 * @param ruleSetLoadTuner the rule set load tuner
	 * @return the merged load tuner
	 */
	RuleSetLoadTuner merge(RuleSetLoadTuner ruleSetLoadTuner);
}
