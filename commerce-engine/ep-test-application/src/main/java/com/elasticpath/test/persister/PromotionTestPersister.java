/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.test.persister;

import java.util.Date;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.rules.RuleSet;
import com.elasticpath.domain.rules.RuleSetLoadTuner;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.catalog.CatalogService;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.RuleSetService;
import com.elasticpath.service.rules.impl.DBCompilingRuleEngineImpl;
import com.elasticpath.service.sellingcontext.SellingContextService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.tags.service.TagConditionService;
import com.elasticpath.test.common.exception.TestApplicationException;

/**
 * Persister allows to create and save into database promotion rules.
 */
public class PromotionTestPersister {

	private final DBCompilingRuleEngineImpl ruleEngine;

	// Services for saving and updating persisted objects.

	private final RuleService ruleService;

	private final RuleSetService ruleSetService;

	private final StoreService storeService;

	private final CatalogService catalogService;

	private final BeanFactory beanFactory;

	private final SellingContextService sellingContextService;

	private final TagConditionService tagConditionService;

	private final TestApplicationContext tac;

	/**
	 * Promotion test persister constructor.
	 *
	 * @param beanFactory the bean factory
	 */
	public PromotionTestPersister(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
		tac = beanFactory.getSingletonBean(ContextIdNames.TEST_APPLICATION_CONTEXT, TestApplicationContext.class);
		ruleService = beanFactory.getSingletonBean(ContextIdNames.RULE_SERVICE, RuleService.class);
		ruleSetService = beanFactory.getSingletonBean(ContextIdNames.RULE_SET_SERVICE, RuleSetService.class);
		storeService = beanFactory.getSingletonBean(ContextIdNames.STORE_SERVICE, StoreService.class);
		catalogService = beanFactory.getSingletonBean(ContextIdNames.CATALOG_SERVICE, CatalogService.class);
		sellingContextService = beanFactory.getSingletonBean(ContextIdNames.SELLING_CONTEXT_SERVICE, SellingContextService.class);
		tagConditionService = beanFactory.getSingletonBean(ContextIdNames.TAG_CONDITION_SERVICE, TagConditionService.class);
		ruleEngine = beanFactory.getSingletonBean(ContextIdNames.EP_RULE_ENGINE, DBCompilingRuleEngineImpl.class);
		ruleEngine.resetRuleBaseCache();
	}

	/**
	 * Create catalog promotion rule.
	 *
	 * @param promotionName the promotion name
	 * @param catalogCode   the catalog code
	 * @return rule instance
	 */
	public Rule createCatalogPromotion(final String promotionName, final String catalogCode) {

		Catalog catalog = catalogService.findByCode(catalogCode);
		if (catalog == null) {
			throw new TestApplicationException("Catalog with name " + catalogCode + " doesn't exists in DB.");
		}

		RuleSetLoadTuner ruleSetLoadTuner = beanFactory.getPrototypeBean(ContextIdNames.RULE_SET_LOAD_TUNER, RuleSetLoadTuner.class);
		RuleSet ruleSet = ruleSetService.findByScenarioId(RuleScenarios.CATALOG_BROWSE_SCENARIO, ruleSetLoadTuner);
		final Rule promotionRule = createPromotion(promotionName, ruleSet);
		promotionRule.setCatalog(catalog);
		return promotionRule;
	}

	/**
	 * Create catalog promotion rule, but with the specified code as promo code.
	 *
	 * @param promotionName the promotion name
	 * @param promoCode     the catalog code
	 * @param catalogCode   rule instance
	 * @return created promotion rule.
	 */
	public Rule createCatalogPromotion(final String promotionName, final String promoCode, final String catalogCode) {
		Catalog catalog = catalogService.findByCode(catalogCode);
		if (catalog == null) {
			throw new TestApplicationException("Catalog with name " + catalogCode + " doesn't exists in DB.");
		}
		RuleSetLoadTuner ruleSetLoadTuner = beanFactory.getPrototypeBean(ContextIdNames.RULE_SET_LOAD_TUNER, RuleSetLoadTuner.class);
		RuleSet ruleSet = ruleSetService.findByScenarioId(RuleScenarios.CATALOG_BROWSE_SCENARIO, ruleSetLoadTuner);
		final Rule promotionRule = createPromotion(promotionName, promoCode, ruleSet, false);
		promotionRule.setCatalog(catalog);
		return promotionRule;
	}

	/**
	 * Create shopping cart promotion rule.
	 *
	 * @param promotionName the promotion name
	 * @param storeCode     the store code
	 * @return configured rule instance
	 */
	public Rule createLimitedUsagePromotion(final String promotionName, final String storeCode) {
		RuleSetLoadTuner ruleSetLoadTuner = beanFactory.getPrototypeBean(ContextIdNames.RULE_SET_LOAD_TUNER, RuleSetLoadTuner.class);
		RuleSet ruleSet = ruleSetService.findByScenarioId(RuleScenarios.CART_SCENARIO, ruleSetLoadTuner);
		final Rule promotionRule = createPromotion(promotionName, ruleSet);

		return promotionRule;
	}

	/**
	 * Create shopping cart promotion rule.
	 *
	 * @param promotionName the promotion name
	 * @param storeCode     the store code
	 * @param code          promo code (not a coupon code)
	 * @return configured rule instance
	 */
	public Rule createShoppingCartPromotion(final String promotionName, final String storeCode, final String code) {

		final Store store = storeService.findStoreWithCode(storeCode);
		if (store == null) {
			throw new TestApplicationException("Store with code " + storeCode + " doesn't exists in DB.");
		}

		RuleSetLoadTuner ruleSetLoadTuner = beanFactory.getPrototypeBean(ContextIdNames.RULE_SET_LOAD_TUNER, RuleSetLoadTuner.class);
		RuleSet ruleSet = ruleSetService.findByScenarioId(RuleScenarios.CART_SCENARIO, ruleSetLoadTuner);
		final Rule promotionRule = createPromotion(promotionName, code, ruleSet, false);

		promotionRule.setStore(store);
		promotionRule.setCatalog(store.getCatalog());
		return promotionRule;
	}

	/**
	 * Create shopping cart promotion rule.
	 *
	 * @param promotionName the promotion name
	 * @param storeCode     the store code
	 * @param code          promo code (not a coupon code)
	 * @param couponEnabled enabled for coupons
	 * @return configured rule instance
	 */
	public Rule createShoppingCartPromotion(final String promotionName, final String storeCode, final String code, final boolean couponEnabled) {

		final Store store = storeService.findStoreWithCode(storeCode);
		if (store == null) {
			throw new TestApplicationException("Store with code " + storeCode + " doesn't exists in DB.");
		}

		RuleSetLoadTuner ruleSetLoadTuner = beanFactory.getPrototypeBean(ContextIdNames.RULE_SET_LOAD_TUNER, RuleSetLoadTuner.class);
		RuleSet ruleSet = ruleSetService.findByScenarioId(RuleScenarios.CART_SCENARIO, ruleSetLoadTuner);
		final Rule promotionRule = createPromotion(promotionName, code, ruleSet, couponEnabled);

		promotionRule.setStore(store);
		promotionRule.setCatalog(store.getCatalog());
		return promotionRule;
	}

	private Rule createPromotion(final String promotionName, final RuleSet ruleSet) {
		return createPromotion(promotionName, null, ruleSet, false);
	}

	/**
	 * Create promotion.
	 *
	 * @param promotionName
	 * @param code
	 * @param ruleSet       we have to know what kind of promotion need to be created
	 * @return rule
	 */
	private Rule createPromotion(final String promotionName, final String code,
								 final RuleSet ruleSet, final boolean couponEnabled) {
		final Rule promotionRule = beanFactory.getPrototypeBean(ContextIdNames.PROMOTION_RULE, Rule.class);
		promotionRule.setRuleSet(ruleSet);
		promotionRule.initialize();

		if (ruleSet.getScenario() == RuleScenarios.CATALOG_BROWSE_SCENARIO) {
			//shopping cart scenario must use selling context for set the date ranges
			promotionRule.setStartDate(new Date());
		}


		promotionRule.setCmUser(tac.getPersistersFactory().getStoreTestPersister().getCmUser());
		promotionRule.setName(promotionName);
		if (code != null) {
			promotionRule.setCode(code);
		}
		promotionRule.setCouponEnabled(couponEnabled);
		promotionRule.setEnabled(true);
		promotionRule.setDescription("Simple shopping cart promotion rule: discount for the given product sku");
		return promotionRule;
	}

	/**
	 * Save configured rule into a database.
	 *
	 * @param promotionRule to be saved
	 * @return persisted rule
	 */
	public Rule persistPromotionRule(final Rule promotionRule) {
		Rule persistedPromotionRule = ruleService.add(promotionRule);
		ruleEngine.recompileRuleBase();
		return persistedPromotionRule;
	}

	/**
	 * Returns a shopping cart promotion with the given name.
	 *
	 * @param promotionName the promotion name
	 * @return rule with the given name.
	 */
	public Rule findShoppingCartPromotionByName(final String promotionName) {
		return ruleService.findByName(promotionName);
	}

	/**
	 * Update a given promotion rule.
	 *
	 * @param promotionRule promotion rule to update
	 * @return updated promotion rule
	 */
	public Rule updatePromotionRule(final Rule promotionRule) {
		Rule updatedPromotionRule = ruleService.update(promotionRule);
		ruleEngine.recompileRuleBase();
		return updatedPromotionRule;
	}

	/**
	 * Creates and persists a simple (10% off subtotal) shopping cart promotion.
	 *
	 * @param promotionName the name of the promotion
	 * @param storeCode     the store code
	 * @param code          the promoCode (not coupon code)
	 * @return rule
	 */
	public Rule createAndPersistSimpleShoppingCartPromotion(final String promotionName, final String storeCode, final String code) {
		return createAndPersistSimpleShoppingCartPromotion(promotionName, storeCode, code, false);
	}

	/**
	 * Creates and persists a simple (10% off subtotal) shopping cart promotion.
	 *
	 * @param promotionName the name of the promotion
	 * @param storeCode     the store code
	 * @param code          the promoCode (not coupon code)
	 * @param couponEnabled whether the promo is coupon enabled
	 * @return rule
	 */
	public Rule createAndPersistSimpleShoppingCartPromotion(final String promotionName, final String storeCode,
															final String code, final boolean couponEnabled) {
		RuleParameter param = beanFactory.getPrototypeBean(ContextIdNames.RULE_PARAMETER, RuleParameter.class);
		param.setKey(RuleParameter.DISCOUNT_PERCENT_KEY);
		param.setValue("10");

		RuleAction action = beanFactory.getPrototypeBean(ContextIdNames.CART_SUBTOTAL_PERCENT_DISCOUNT_ACTION, RuleAction.class);
		action.getParameters().clear();
		action.addParameter(param);

		Rule rule = createShoppingCartPromotion(promotionName, storeCode, code, couponEnabled);
		rule.addAction(action);

		return persistPromotionRule(rule);
	}

}
