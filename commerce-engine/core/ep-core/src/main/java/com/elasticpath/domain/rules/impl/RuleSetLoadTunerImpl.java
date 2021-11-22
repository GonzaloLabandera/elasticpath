/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.domain.rules.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.RULES;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.openjpa.persistence.FetchPlan;

import com.elasticpath.domain.impl.AbstractEpDomainImpl;
import com.elasticpath.domain.rules.RuleSetLoadTuner;
import com.elasticpath.persistence.api.LoadTuner;

/**
 * Represents a tuner to control rule set load. A rule set load tuner can be used in some services to fine control what data to be loaded for a
 * rule set. The main purpose is to achieve maximum performance for some specific performance-critical scenarios.
 */
public class RuleSetLoadTunerImpl extends AbstractEpDomainImpl implements RuleSetLoadTuner {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private boolean loadingRules;

	/**
	 * Default constructor.
	 */
	public RuleSetLoadTunerImpl() {
		// do nothing
	}

	/**
	 * Return <code>true</code> if rules are requested.
	 *
	 * @return <code>true</code> if rules are requested.
	 */
	@Override
	public boolean isLoadingRules() {
		return loadingRules;
	}

	@Override
	public void setLoadingRules(final boolean flag) {
		this.loadingRules = flag;
	}

	@Override
	public void configure(final FetchPlan fetchPlan) {
		if (isLoadingRules()) {
			fetchPlan.addField(RuleSetImpl.class, RULES);
		}
	}

	/**
	 * Hash code. Need I say more?
	 *
	 * @return the hash code.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(loadingRules);
	}

	/**
	 * Implements equals semantics.<br>
	 * Because load tuners are concerned with field states within the class, it acts as a value type. In this case, content is crucial in the equals
	 * comparison. Using getClass() within the equals method ensures strict comparison between content state in this class where symmetry is
	 * maintained. If instanceof was used in the comparison this could potentially cause symmetry violations when extending this class.
	 *
	 * @param obj the other object to compare
	 * @return true if equal
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		final RuleSetLoadTunerImpl other = (RuleSetLoadTunerImpl) obj;
		return Objects.equals(loadingRules, other.loadingRules);
	}

	@Override
	public boolean contains(final LoadTuner loadTuner) {
		if (!(loadTuner instanceof RuleSetLoadTuner)) {
			return false;
		}
		return contains((RuleSetLoadTuner) loadTuner);
	}

	@Override
	public boolean contains(final RuleSetLoadTuner ruleSetLoadTuner) {
		// same load tuner
		if (this == ruleSetLoadTuner) {
			return true;
		}

		// Any load tuner contains an empty one
		if (ruleSetLoadTuner == null) {
			return true;
		}

		return loadingRules || !ruleSetLoadTuner.isLoadingRules();
	}

	@Override
	public LoadTuner merge(final LoadTuner loadTuner) {
		if (!(loadTuner instanceof RuleSetLoadTuner)) {
			return this;
		}
		return merge((RuleSetLoadTuner) loadTuner);
	}

	/**
	 * Merges the given rule set load tuner with this one and returns the merged rule set load tuner.
	 *
	 * @param ruleSetLoadTuner the rule set load tuner
	 * @return the merged rule set load tuner
	 */
	@Override
	public RuleSetLoadTuner merge(final RuleSetLoadTuner ruleSetLoadTuner) {
		if (ruleSetLoadTuner == null) {
			return this;
		}

		// Do not need to create a new load tuner if the given one contains this.
		if (ruleSetLoadTuner.contains(this)) {
			return ruleSetLoadTuner;
		}

		final RuleSetLoadTunerImpl mergedRuleSetLoadTuner = new RuleSetLoadTunerImpl();
		mergedRuleSetLoadTuner.loadingRules = loadingRules || ruleSetLoadTuner.isLoadingRules();

		return mergedRuleSetLoadTuner;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
			.append("loadingRules", isLoadingRules())
			.toString();
	}
}
