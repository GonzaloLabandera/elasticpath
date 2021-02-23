/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.rules.impl;

import java.util.List;

import com.elasticpath.service.rules.SellingContextRuleSummary;

/**
 * Retrieval strategy for data required by the rule engine.
 */
public interface RuleEngineDataStrategy {

	/**
	 * Finds active rules in context to scenario and store/catalog code.
	 *
	 * @param scenario rule scenario
	 * @param code store/catalog code
	 * @return list of entries with rule id and its selling context
	 */
	List<SellingContextRuleSummary> findActiveRuleIdSellingContext(int scenario, String code);

}
