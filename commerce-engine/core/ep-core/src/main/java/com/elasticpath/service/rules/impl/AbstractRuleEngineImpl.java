/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.service.rules.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.drools.QueryResult;
import org.drools.QueryResults;
import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseFactory;
import org.drools.SessionConfiguration;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.impl.EnvironmentFactory;

import com.elasticpath.cache.SimpleTimeoutCache;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.SimpleCache;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.discounts.Discount;
import com.elasticpath.domain.discounts.DiscountItemContainer;
import com.elasticpath.domain.discounts.ShoppingCartDiscountItemContainer;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.rules.RuleSet;
import com.elasticpath.domain.rules.impl.ActiveRuleImpl;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.rules.EpRuleEngine;
import com.elasticpath.service.rules.PromotionRuleDelegate;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.domain.TagDictionary;
import com.elasticpath.tags.service.ConditionEvaluatorService;

/**
 * Abstract class for exposing common methods for all rule engines.
 */
public abstract class AbstractRuleEngineImpl implements EpRuleEngine {

	/**
	 * Key used to put the rules into the Shopper.
	 */
	public static final String RULE_IDS_KEY = "RULE_IDS_KEY";

	private RuleService ruleService;

	private RuleEngineDataStrategy dataStrategy;

	private PromotionRuleDelegate promotionRuleDelegate;

	private static final Logger LOG = Logger.getLogger(AbstractRuleEngineImpl.class);

	private static final String SESSION_CONFIGURATION_ID = "SESSION_CONFIGURATION_ID";

	//injected via Spring
	private SimpleTimeoutCache<String, SessionConfiguration> statefulSessionConfiguration;

	private ConditionEvaluatorService conditionEvaluatorService;

	private BeanFactory beanFactory;

	@Override
	public void fireCatalogPromotionRules(final Collection<? extends Product> products, final Currency activeCurrency,
											final Store store, final Map<String, List<Price>> prices) {
		if (store == null) {
			throw new IllegalArgumentException("Store must not be null");
		}

		if (products.isEmpty()) {
			return;
		}

		SessionConfiguration sessionConfiguration = statefulSessionConfiguration.get(SESSION_CONFIGURATION_ID);
		if (sessionConfiguration == null) {
			sessionConfiguration = new SessionConfiguration();
			statefulSessionConfiguration.put(SESSION_CONFIGURATION_ID, sessionConfiguration);
		}

		StatefulSession workingMemory = getCatalogRuleBase(store).newStatefulSession(sessionConfiguration, EnvironmentFactory.newEnvironment());

		try {
			assertObject(workingMemory, promotionRuleDelegate);
			assertObject(workingMemory, activeCurrency);

			assertObject(workingMemory, prices);

			for (Product product : products) {
				assertObject(workingMemory, product);
			}

			workingMemory.setFocus(RuleAction.DEFAULT_AGENDA_GROUP);
			workingMemory.fireAllRules();
		} finally {
			workingMemory.dispose();
		}
	}

	@Override
	public void fireOrderPromotionRules(final ShoppingCart shoppingCart, final CustomerSession customerSession) {
		firePromotionRulesForGroup(shoppingCart, customerSession, RuleAction.DEFAULT_AGENDA_GROUP);
	}

	@Override
	public void fireOrderPromotionSubtotalRules(final ShoppingCart shoppingCart,
												final CustomerSession customerSession) {
		firePromotionRulesForGroup(shoppingCart, customerSession, RuleAction.SUBTOTAL_DEPENDENT_AGENDA_GROUP);
	}

	private void firePromotionRulesForGroup(final ShoppingCart shoppingCart,
											final CustomerSession customerSession,
											final String agendaGroup) {
		if (shoppingCart == null) {
			throw new IllegalArgumentException("Shopping cart cannot be null");
		}

		final Currency activeCurrency = customerSession.getCurrency();
		final List<Long> uidPks = evaluateApplicableRules(shoppingCart);

		// check if the shopping cart has a store set
		if (shoppingCart.getStore() != null && !uidPks.isEmpty()) {

			SessionConfiguration sessionConfiguration = statefulSessionConfiguration.get(SESSION_CONFIGURATION_ID);
			if (sessionConfiguration == null) {
				sessionConfiguration = new SessionConfiguration();
				statefulSessionConfiguration.put(SESSION_CONFIGURATION_ID, sessionConfiguration);
			}

			StatefulSession workingMemory = getCartRuleBase(shoppingCart.getStore()).newStatefulSession(sessionConfiguration,
																										EnvironmentFactory.newEnvironment());
			final DiscountItemContainer shoppingCartDiscountItemContainer = createShoppingCartDiscountItemContainer(shoppingCart, activeCurrency);

			try {
				assertObject(workingMemory, promotionRuleDelegate);
				assertObject(workingMemory, shoppingCart);
				assertObject(workingMemory, activeCurrency);
				assertObject(workingMemory, shoppingCartDiscountItemContainer);
				for (long ruleId : uidPks) {
					assertObject(workingMemory, new ActiveRuleImpl(ruleId));
				}
				workingMemory.setFocus(agendaGroup);
				workingMemory.fireAllRules();
				QueryResults queryResults = workingMemory.getQueryResults(RuleSet.QUERY_NAME);
				applyDiscount(queryResults, shoppingCartDiscountItemContainer);
			} catch (IllegalArgumentException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Rule Engine could not get query results", e);
				}
			} finally {
				workingMemory.dispose();
			}
		}
	}

	/**
	 * Uses groovy engine to fire all who conditions that are applicable to rules.
	 *
	 * @param shoppingCart the cart
	 * @return list of uidPks of rules that are applicable.
	 */
	private List<Long> evaluateApplicableRules(final ShoppingCart shoppingCart) {

		final Shopper shopper = shoppingCart.getShopper();
		final SimpleCache simpleCache = shopper.getCache();

		if (!simpleCache.isInvalidated(RULE_IDS_KEY)) {
			return shopper.getCache().getItem(RULE_IDS_KEY);
		}

		final List<Object[]> sellingContextsWithRuleUidPks = getSellingContextsForRules(shoppingCart.getStore().getCode());

		final List<Long> ruleIds = new ArrayList<>();
		TagSet tagSet = shopper.getTagSet();
		for (Object[] obj : sellingContextsWithRuleUidPks) {
			final SellingContext scxt = (SellingContext) obj[1];
			if (scxt == null || scxt.isSatisfied(
					conditionEvaluatorService, tagSet, TagDictionary.DICTIONARY_PROMOTIONS_SHOPPER_GUID, TagDictionary.DICTIONARY_TIME_GUID)) {
				ruleIds.add((Long) obj[0]);
			}
		}

		shopper.getCache().putItem(RULE_IDS_KEY, ruleIds);

		return ruleIds;
	}

	/**
	 * Method to extract selling context associated with rule and the rule uidPk.
	 *
	 * @param storeCode store code
	 * @return object[sellingContext, ruleUidPk].
	 */
	protected List<Object[]> getSellingContextsForRules(final String storeCode) {
		List<Object[]> result = getDataStrategy().findActiveRuleIdSellingContextByScenarioAndStore(RuleScenarios.CART_SCENARIO, storeCode);
		if (result == null) {
			return Collections.emptyList();
		}
		return result;
	}

	/**
	 * Apply the discount. Default behaviour is to apply all.
	 *
	 * @param queryResults query result that consist of discount objects in working memory.
	 * @param discountItemContainer the discountItemContainer to apply discount.
	 */
	protected void applyDiscount(final QueryResults queryResults, final DiscountItemContainer discountItemContainer) {
		if (queryResults == null) {
			return;
		}
		for (Iterator<QueryResult> it = queryResults.iterator(); it.hasNext();) {
			Discount discount = (Discount) it.next().get(RuleSet.DISCOUNT_NAME);
			discount.apply(discountItemContainer);
		}
	}

	/**
	 * Get the promotion receiver.
	 *
	 * @param shoppingCart the shopping cart that receives the promotion
	 * @param activeCurrency the currency
	 * @return promotion receiver.
	 */
	private DiscountItemContainer createShoppingCartDiscountItemContainer(final ShoppingCart shoppingCart, final Currency activeCurrency) {
		final ShoppingCartDiscountItemContainer discountItemContainer = beanFactory.getBean(ContextIdNames.SHOPPING_CART_DISCOUNT_ITEM_CONTAINER);
		discountItemContainer.setShoppingCart(shoppingCart);
		discountItemContainer.setCurrency(activeCurrency);
		return discountItemContainer;
	}

	/**
	 * Creates a new Drools <code>RuleBase</code> which will be based on the configuration returned
	 * {@link #createRuleConfiguration()}.
	 *
	 * @return the newly created Drools rulebase
	 */
	protected RuleBase createRuleBase() {
		return RuleBaseFactory.newRuleBase(createRuleConfiguration());
	}

	/**
	 * Creates a new Drools <code>RuleConfiguration</code> which will be based on the class loader of this class.
	 *
	 * In an OSGi environment this means that Drools will have access to all classes in core. In
	 * a web application environment this will mean that Drools has access to all classes available
	 * to the web application class loader.
	 *
	 * @return the newly created Drools <code>RuleConfiguration</code>
	 */
	protected RuleBaseConfiguration createRuleConfiguration() {
		return new RuleBaseConfiguration(getClass().getClassLoader());
	}

	/**
	 * Gets the catalog {@link RuleBase} associated with the given {@link Store} s catalog.
	 *
	 * @param store the store to get the catalog from
	 * @return a {@link RuleBase} for the given {@link Store}
	 */
	protected abstract RuleBase getCatalogRuleBase(Store store);

	/**
	 * Gets the cart {@link RuleBase} associated with the given {@link Store}.
	 *
	 * @param store the store to get the rule base for
	 * @return a {@link RuleBase} for the given {@link Store}
	 */
	protected abstract RuleBase getCartRuleBase(Store store);

	/**
	 * Assert the given object into the given working memory.
	 * <p>
	 * Note: don't try to do refactoring by removing this method. This method is put here to make profiling easier.
	 *
	 * @param workingMemory the working memory
	 * @param object the object
	 */
	private void assertObject(final WorkingMemory workingMemory, final Object object) {
		workingMemory.insert(object);
	}

	/**
	 * Sets the {@link PromotionRuleDelegate} instance to use.
	 *
	 * @param promotionRuleDelegate the {@link PromotionRuleDelegate} instance to use
	 */
	public void setPromotionRuleDelegate(final PromotionRuleDelegate promotionRuleDelegate) {
		this.promotionRuleDelegate = promotionRuleDelegate;
	}

	/**
	 * @return the rule service
	 */
	protected RuleService getRuleService() {
		return ruleService;
	}

	/**
	 * Sets the {@link RuleService} instance to use.
	 *
	 * @param ruleService the {@link RuleService} instance to use
	 */
	public void setRuleService(final RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * @param dataStrategy the dataStrategy to set
	 */
	public void setDataStrategy(final RuleEngineDataStrategy dataStrategy) {
		this.dataStrategy = dataStrategy;
	}

	/**
	 * @return the dataStrategy
	 */
	public RuleEngineDataStrategy getDataStrategy() {
		return dataStrategy;
	}

	public void setConditionEvaluatorService(final ConditionEvaluatorService conditionEvaluatorService) {
		this.conditionEvaluatorService = conditionEvaluatorService;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public SimpleTimeoutCache<String, SessionConfiguration> getStatefulSessionConfiguration() {
		return statefulSessionConfiguration;
	}

	public void setStatefulSessionConfiguration(final SimpleTimeoutCache<String, SessionConfiguration> statefulSessionConfiguration) {
		this.statefulSessionConfiguration = statefulSessionConfiguration;
	}
}
