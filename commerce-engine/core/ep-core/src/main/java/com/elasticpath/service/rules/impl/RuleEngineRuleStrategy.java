/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.rules.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.elasticpath.commons.util.SimpleCache;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.rules.SellingContextRuleSummary;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.service.ConditionEvaluatorService;

/**
 * Helper that evaluates and returns applicable rules for the {@link AbstractRuleEngineImpl} class.
 */
public class RuleEngineRuleStrategy {

	/**
	 * Key used to put the rules into the shopper for cart.
	 */
	public static final String CART_RULE_IDS = "CART_RULE_IDS";

	/**
	 * Key used to put the rules into the shopper for catalog.
	 */
	public static final String CATALOG_RULE_IDS = "CATALOG_RULE_IDS";

	private ConditionEvaluatorService conditionEvaluatorService;

	private RuleEngineDataStrategy dataStrategy;

	/**
	 * Uses groovy engine to fire all who conditions that are applicable to catalog rules.
	 *
	 * @param catalog the catalog
	 * @param tagSet  set of tags within customer session
	 * @return list of uidPks of rules that are applicable.
	 */
	public List<Long> evaluateApplicableRules(final Catalog catalog, final TagSet tagSet) {
		final String catalogCode = catalog.getCode();

		return evaluateApplicableRules(catalogCode, CATALOG_RULE_IDS, tagSet);
	}

	/**
	 * Uses groovy engine to fire all who conditions that are applicable to shopping cart rules.
	 *
	 * @param shoppingCart the shopper
	 * @return list of uidPks of rules that are applicable.
	 */
	public List<Long> evaluateApplicableRules(final ShoppingCart shoppingCart) {
		final Shopper shopper = shoppingCart.getShopper();
		final SimpleCache simpleCache = shopper.getCache();

		if (!simpleCache.isInvalidated(CART_RULE_IDS)) {
			return simpleCache.getItem(CART_RULE_IDS);
		}

		final String storeCode = shopper.getShopperMemento().getStoreCode();

		final List<Long> result = evaluateApplicableRules(storeCode, CART_RULE_IDS, shopper.getTagSet());

		cacheRuleIdsWithKey(shopper, result);

		return result;
	}

	private List<Long> evaluateApplicableRules(final String code, final String ruleIdKey, final TagSet tagSet) {

		final int ruleScenario = ruleIdKey.equals(CATALOG_RULE_IDS)
				? RuleScenarios.CATALOG_BROWSE_SCENARIO
				: RuleScenarios.CART_SCENARIO;

		final List<SellingContextRuleSummary> sellingContextsWithRuleUidPks = getSellingContextWithRuleUidpk(code, ruleScenario);

		return getActiveRules(sellingContextsWithRuleUidPks, tagSet);
	}

	private List<Long> getActiveRules(final List<SellingContextRuleSummary> sellingContextsWithRuleUidPks, final TagSet tagSet) {
		final List<Long> ruleIds = new ArrayList<>();

		for (final SellingContextRuleSummary data : sellingContextsWithRuleUidPks) {
			final SellingContext context = data.getSellingContext();
			if (Objects.isNull(context) || context.isSatisfied(conditionEvaluatorService, tagSet,
					TagDictionary.DICTIONARY_PROMOTIONS_SHOPPER_GUID,
					TagDictionary.DICTIONARY_TIME_GUID).isSuccess()) {

				final Long ruleUidpk = data.getRuleUidPk();
				ruleIds.add(ruleUidpk);
			}
		}

		return ruleIds;
	}

	private void cacheRuleIdsWithKey(final Shopper shopper, final List<Long> ruleIds) {
		shopper.getCache().putItem(CART_RULE_IDS, ruleIds);
	}

	/**
	 * Method to extract selling context associated with rule and the rule uidPk.
	 *
	 * @param code         store/catalog code
	 * @param ruleScenario rule scenario
	 * @return list containing objects of rule uidpk and selling context.
	 */
	protected List<SellingContextRuleSummary> getSellingContextWithRuleUidpk(final String code, final int ruleScenario) {

		final List<SellingContextRuleSummary> result = getDataStrategy().findActiveRuleIdSellingContext(ruleScenario, code);

		if (Objects.isNull(result)) {
			return Collections.emptyList();
		}

		return result;
	}

	public void setConditionEvaluatorService(final ConditionEvaluatorService conditionEvaluatorService) {
		this.conditionEvaluatorService = conditionEvaluatorService;
	}

	protected ConditionEvaluatorService getConditionEvaluatorService() {
		return conditionEvaluatorService;
	}

	public void setDataStrategy(final RuleEngineDataStrategy dataStrategy) {
		this.dataStrategy = dataStrategy;
	}

	protected RuleEngineDataStrategy getDataStrategy() {
		return dataStrategy;
	}
}
