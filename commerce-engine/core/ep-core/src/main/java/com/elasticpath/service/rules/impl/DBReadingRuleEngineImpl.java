/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */

package com.elasticpath.service.rules.impl;

import static com.elasticpath.domain.rules.RuleScenarios.CART_SCENARIO;
import static com.elasticpath.domain.rules.RuleScenarios.CATALOG_BROWSE_SCENARIO;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.antkorwin.xsync.XSync;
import org.kie.api.KieBase;

import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.misc.TimeService;

/**
 * Provides the ability to read rules from the database. Take note that this does not write
 * anything to the database. This rules engine is implemented using Drools Rules 3.0.
 */
public class DBReadingRuleEngineImpl extends AbstractRuleEngineImpl {

	private final Map<String, EpRuleBase> cartRuleBases = new ConcurrentHashMap<>();
	private final Map<String, EpRuleBase> catalogRuleBases = new ConcurrentHashMap<>();
	private final XSync<String> storeSync = new XSync<>();
	private final XSync<String> catalogSync = new XSync<>();
	private TimeService timeService;

	@Override
	protected KieBase getCartRuleBase(final Store store) {
		final String code = store.getCode();
		// We use XSync here to ensure that we don't have multiple threads trying to retrieve the rule base
		// at the same time before the map is populated with the results.
		return storeSync.evaluate(code, () -> {
			EpRuleBase ruleBase = cartRuleBases.get(code);
			if (ruleBase == null) {
				ruleBase = getRuleService().findRuleBaseByScenario(store, null, CART_SCENARIO);
			} else {
				Date modifiedDate = getRuleService().getModifiedDateForRuleBase(ruleBase.getUidPk());
				if (modifiedDate == null) {
					ruleBase = null;
				} else if (modifiedDate.after(ruleBase.getLastModifiedDate())) {
					ruleBase = getRuleService().findRuleBaseByScenario(store, null, CART_SCENARIO);
				}
			}

			if (ruleBase == null) {
				ruleBase = new EpRuleBaseNotPresentImpl(timeService.getCurrentTime(), createRuleBase());
			}

			cartRuleBases.put(code, ruleBase);
			return ruleBase.getRuleBase();
		});
	}

	@Override
	protected KieBase getCatalogRuleBase(final Store store) {
		final String code = store.getCatalog().getCode();
		// We use XSync here to ensure that we don't have multiple threads trying to retrieve the rule base
		// at the same time before the map is populated with the results.
		return catalogSync.evaluate(code, () -> {
			EpRuleBase ruleBase = catalogRuleBases.get(code);
			if (ruleBase == null) {
				ruleBase = getRuleService().findRuleBaseByScenario(null, store.getCatalog(), CATALOG_BROWSE_SCENARIO);
			} else {
				Date modifiedDate = getRuleService().getModifiedDateForRuleBase(ruleBase.getUidPk());
				if (modifiedDate == null) {
					ruleBase = null;
				} else if (modifiedDate.after(ruleBase.getLastModifiedDate())) {
					ruleBase = getRuleService().findRuleBaseByScenario(null, store.getCatalog(), CATALOG_BROWSE_SCENARIO);
				}
			}

			if (ruleBase == null) {
				ruleBase = new EpRuleBaseNotPresentImpl(timeService.getCurrentTime(), createRuleBase());
			}

			catalogRuleBases.put(code, ruleBase);
			return ruleBase.getRuleBase();
		});
	}

	protected TimeService getTimeService() {
		return timeService;
	}

	public void setTimeService(final TimeService timeService) {
		this.timeService = timeService;
	}
}