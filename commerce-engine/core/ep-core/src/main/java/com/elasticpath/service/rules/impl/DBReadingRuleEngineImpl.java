/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */

package com.elasticpath.service.rules.impl;

import static com.elasticpath.domain.rules.RuleScenarios.CART_SCENARIO;
import static com.elasticpath.domain.rules.RuleScenarios.CATALOG_BROWSE_SCENARIO;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.kie.api.KieBase;

import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.store.Store;

/**
 * Provides the ability to read rules from the database. Take note that this does not write
 * anything to the database. This rules engine is implemented using Drools Rules 3.0.
 */
public class DBReadingRuleEngineImpl extends AbstractRuleEngineImpl {

	private final Map<String, EpRuleBase> cartRuleBases = new ConcurrentHashMap<>();

	private final Map<String, EpRuleBase> catalogRuleBases = new ConcurrentHashMap<>();

	@Override
	protected KieBase getCartRuleBase(final Store store) {
		final String code = store.getCode();
		EpRuleBase ruleBase;

		if (cartRuleBases.containsKey(code)) {
			ruleBase = cartRuleBases.get(code);

			if (ruleBase == null) {
				ruleBase = getRuleService().findRuleBaseByScenario(store, null, CART_SCENARIO);
			} else {
				Date modifiedDate = getRuleService().getModifiedDateForRuleBase(ruleBase.getUidPk());
				if (modifiedDate == null) {
					ruleBase = null;
				} else {
					ruleBase = getRuleService().findChangedStoreRuleBases(code, CART_SCENARIO,
							modifiedDate);
				}
			}
		} else {
			ruleBase = getRuleService().findRuleBaseByScenario(store, null, CART_SCENARIO);
		}

		if (ruleBase == null) {
			cartRuleBases.remove(code);
		} else {
			cartRuleBases.put(code, ruleBase);
			return ruleBase.getRuleBase();
		}

		// just in case the rule base will be modified externally
		return createRuleBase();
	}

	@Override
	protected KieBase getCatalogRuleBase(final Store store) {
		final String code = store.getCatalog().getCode();

		EpRuleBase storedRuleBase = catalogRuleBases.get(code);
		storedRuleBase = getValidRuleBase(store, code, storedRuleBase);
		if (storedRuleBase == null) {
			catalogRuleBases.remove(code);
		} else {
			catalogRuleBases.put(code, storedRuleBase);
		}

		if (storedRuleBase != null) {
			return storedRuleBase.getRuleBase();
		}

		// just in case the rule base will be modified externally
		return createRuleBase();
	}

	private EpRuleBase getValidRuleBase(final Store store, final String code, final EpRuleBase storedRuleBase) {
		EpRuleBase ruleBase;
		if (storedRuleBase == null) {
			ruleBase = getRuleService().findRuleBaseByScenario(null, store.getCatalog(), CATALOG_BROWSE_SCENARIO);
		} else {
			Date modifiedDate = getRuleService().getModifiedDateForRuleBase(storedRuleBase.getUidPk());
			if (modifiedDate == null) {
				ruleBase = null;
			} else {
				ruleBase = getRuleService().findChangedCatalogRuleBases(code, CATALOG_BROWSE_SCENARIO,
						modifiedDate);
			}
		}
		return ruleBase;
	}

}