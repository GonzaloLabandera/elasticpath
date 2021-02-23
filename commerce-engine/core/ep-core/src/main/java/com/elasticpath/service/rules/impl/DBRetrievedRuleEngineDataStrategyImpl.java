/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.service.rules.impl;

import java.util.List;

import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.SellingContextRuleSummary;

/**
 * Rule engine data strategy that retrieves data from the database.
 */
public class DBRetrievedRuleEngineDataStrategyImpl implements RuleEngineDataStrategy {

	private RuleService ruleService;

	@Override
	public List<SellingContextRuleSummary> findActiveRuleIdSellingContext(final int scenario, final String code) {
		switch (scenario) {
			case RuleScenarios.CART_SCENARIO:
				return findActiveRuleIdSellingContextByScenarioAndStore(RuleScenarios.CART_SCENARIO, code);
			case RuleScenarios.CATALOG_BROWSE_SCENARIO:
				return findActiveRuleIdSellingContextByScenarioAndCatalog(RuleScenarios.CATALOG_BROWSE_SCENARIO, code);
			default:
				throw new IllegalArgumentException("Invalid scenario");
		}
	}

	/**
	 * Finds active rules within scenario and store.
	 *
	 * @param scenario  scenario.
	 * @param storeCode store code.
	 * @return list of entries with rule id and its selling context
	 */
	private List<SellingContextRuleSummary> findActiveRuleIdSellingContextByScenarioAndStore(final int scenario, final String storeCode) {
		return getRuleService().findActiveRuleIdSellingContextByScenarioAndStore(scenario, storeCode);
	}

	/**
	 * Finds active rules within scenario and catalog.
	 *
	 * @param scenario    scenario.
	 * @param catalogCode catalog code.
	 * @return list of entries with rule id and its selling context
	 */
	private List<SellingContextRuleSummary> findActiveRuleIdSellingContextByScenarioAndCatalog(final int scenario, final String catalogCode) {
		return getRuleService().findActiveRuleIdSellingContextByScenarioAndCatalog(scenario, catalogCode);
	}

	/**
	 * Sets the rule service.
	 *
	 * @param ruleService the ruleService to set
	 */
	public void setRuleService(final RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * Gets the rule service.
	 *
	 * @return the ruleService
	 */
	public RuleService getRuleService() {
		return ruleService;
	}

}
