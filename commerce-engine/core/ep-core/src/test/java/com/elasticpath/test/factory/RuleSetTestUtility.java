/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.test.factory;

import java.util.HashSet;
import java.util.Set;

import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleCondition;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.rules.RuleSet;
import com.elasticpath.domain.rules.impl.BrandConditionImpl;
import com.elasticpath.domain.rules.impl.CartCategoryAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartCategoryPercentDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartCurrencyConditionImpl;
import com.elasticpath.domain.rules.impl.CartNFreeSkusActionImpl;
import com.elasticpath.domain.rules.impl.CartNthProductPercentDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartProductAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSkuAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSkuPercentDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSubtotalAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSubtotalConditionImpl;
import com.elasticpath.domain.rules.impl.CatalogCurrencyAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.LimitedUseCouponCodeConditionImpl;
import com.elasticpath.domain.rules.impl.ProductCategoryConditionImpl;
import com.elasticpath.domain.rules.impl.ProductConditionImpl;
import com.elasticpath.domain.rules.impl.ProductInCartConditionImpl;
import com.elasticpath.domain.rules.impl.PromotionRuleImpl;
import com.elasticpath.domain.rules.impl.RuleParameterImpl;
import com.elasticpath.domain.rules.impl.RuleSetImpl;
import com.elasticpath.domain.rules.impl.ShippingAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.SkuInCartConditionImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;

/**
 * Test utility for creating ruleset for testings.
 */
public final class RuleSetTestUtility {

	/**
	 * Default product_uid.
	 */
	private static final long PRODUCT_UID = 1L;

	/**
	 * Default sku_guid.
	 */
	private static final String SKU_GUID = "Sku1";

	/**
	 * Default name.
	 */
	public static final String RULE_SET_NAME = "com.elasticpath.promotionrules";

	/**
	 * Default rule_uid, use a real LONG number to check we can handle the long number.
	 */
	public static final long RULE_UID = 2176843776L;

	/**
	 * Default store.
	 */
	public static final Store STORE = getStore();

	private static final String CAD_CURRENCY = "CAD";

	/**
	 * Create a rule set containing the rule: Default rule set for catalog.
	 *
	 * @return a <code>RuleSet</code> specifying this rule.
	 */
	public static RuleSet createCatalogRuleSet() {
		final RuleSet ruleSet = createRuleSet(RULE_SET_NAME, RuleScenarios.CATALOG_BROWSE_SCENARIO);

		final Rule promotionRule = createCatalogPromotionRule("Car Sale", RuleSetTestUtility.RULE_UID, new CatalogImpl(), true);

		// Create a condition that the product is in a particular category
		RuleCondition categoryCondition = new ProductCategoryConditionImpl();
		categoryCondition.addParameter(new RuleParameterImpl(RuleParameter.CATEGORY_CODE_KEY, "8"));
		categoryCondition.addParameter(new RuleParameterImpl(RuleParameter.BOOLEAN_KEY, "true"));
		promotionRule.addCondition(categoryCondition);

		//Create a condition that constrains the product
		RuleCondition productCondition = new ProductConditionImpl();
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.PRODUCT_CODE_KEY, "8"));
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.BOOLEAN_KEY, "true"));
		promotionRule.addCondition(productCondition);

		//Create a condition that constrains the brand
		RuleCondition brandCondition = new BrandConditionImpl();
		brandCondition.addParameter(new RuleParameterImpl(RuleParameter.BRAND_CODE_KEY, "8"));
		brandCondition.addParameter(new RuleParameterImpl(RuleParameter.BOOLEAN_KEY, "true"));
		promotionRule.addCondition(brandCondition);

		// Create a condition that the currency is CAD
		RuleParameter currencyParam = new RuleParameterImpl();
		currencyParam.setKey(RuleParameter.CURRENCY_KEY);
		currencyParam.setValue(CAD_CURRENCY);

		// Create an action
		RuleAction discountAction = new CatalogCurrencyAmountDiscountActionImpl();
		RuleParameter discountParameter = new RuleParameterImpl();
		discountParameter.setKey(RuleParameter.DISCOUNT_AMOUNT_KEY);
		discountParameter.setValue("100");
		discountAction.addParameter(currencyParam);
		discountAction.addParameter(discountParameter);
		promotionRule.addAction(discountAction);

		promotionRule.setRuleSet(ruleSet);
		ruleSet.addRule(promotionRule);
		return ruleSet;
	}
	
	/**
	 * Create a rule set containing the rule: Default rule set for shopping cart.
	 *
	 * @return a <code>RuleSet</code> specifying this rule.
	 */
	public static RuleSet createShoppingCartRuleSet() { //NOPMD
		final RuleSet ruleSet = createRuleSet(RULE_SET_NAME, RuleScenarios.CART_SCENARIO);

		final Rule promotionRule = createShoppingCartPromotionRule("Order Sale", RuleSetTestUtility.RULE_UID, getStore(), true);

		// Create a condition that the product is in a particular category
		RuleCondition subtotalCondition = new CartSubtotalConditionImpl();
		RuleParameter subtotalAmountParam = new RuleParameterImpl();
		subtotalAmountParam.setKey(RuleParameter.SUBTOTAL_AMOUNT_KEY);
		subtotalAmountParam.setValue("10000");
		subtotalCondition.addParameter(subtotalAmountParam);
		promotionRule.addCondition(subtotalCondition);

		// Create a condition that the currency is CAD
		RuleCondition currencyCondition = new CartCurrencyConditionImpl();
		RuleParameter currencyParam = new RuleParameterImpl();
		currencyParam.setKey(RuleParameter.CURRENCY_KEY);
		currencyParam.setValue(CAD_CURRENCY);
		currencyCondition.addParameter(currencyParam);
		promotionRule.addCondition(currencyCondition);

		// Create an action
		RuleAction discountAction = new CartSubtotalAmountDiscountActionImpl();
		RuleParameter discountParameter = new RuleParameterImpl();
		discountParameter.setKey(RuleParameter.DISCOUNT_AMOUNT_KEY);
		discountParameter.setValue("1000");
		discountAction.addParameter(discountParameter);
		promotionRule.addAction(discountAction);

		// Create an action to discount a category
		RuleAction categoryAmountDiscountAction = new CartCategoryAmountDiscountActionImpl();
		RuleParameter discountAmountParameter = new RuleParameterImpl(RuleParameter.DISCOUNT_AMOUNT_KEY, "5");
		categoryAmountDiscountAction.addParameter(discountAmountParameter);
		RuleParameter discountCategoryParameter = new RuleParameterImpl(RuleParameter.CATEGORY_CODE_KEY, "1");
		categoryAmountDiscountAction.addParameter(discountCategoryParameter);
		RuleParameter numItemsParameter = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		categoryAmountDiscountAction.addParameter(numItemsParameter);
		promotionRule.addAction(categoryAmountDiscountAction);

		//Create a CartProductAmountDiscountAction
		RuleAction cartProductAmountDiscountAction = new CartProductAmountDiscountActionImpl();
		RuleParameter productDiscountAmountParameter = new RuleParameterImpl(RuleParameter.DISCOUNT_AMOUNT_KEY, "5");
		cartProductAmountDiscountAction.addParameter(productDiscountAmountParameter);
		RuleParameter productIdParameter = new RuleParameterImpl(RuleParameter.PRODUCT_CODE_KEY, String.valueOf(PRODUCT_UID));
		cartProductAmountDiscountAction.addParameter(productIdParameter);
		RuleParameter numProductItemsParameter = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		cartProductAmountDiscountAction.addParameter(numProductItemsParameter);
		promotionRule.addAction(cartProductAmountDiscountAction);

		//Create a CartSkuAmountDiscountAction
		RuleAction cartSkuAmountDiscountAction = new CartSkuAmountDiscountActionImpl();
		RuleParameter skuDiscountAmountParameter = new RuleParameterImpl(RuleParameter.DISCOUNT_AMOUNT_KEY, "5");
		cartSkuAmountDiscountAction.addParameter(skuDiscountAmountParameter);
		RuleParameter skuGuidParameter = new RuleParameterImpl(RuleParameter.SKU_CODE_KEY, SKU_GUID);
		cartSkuAmountDiscountAction.addParameter(skuGuidParameter);
		RuleParameter numSkusParameter = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		cartSkuAmountDiscountAction.addParameter(numSkusParameter);
		promotionRule.addAction(cartSkuAmountDiscountAction);

		//Create a CartSkuPercentDiscountAction
		RuleAction cartSkuPercentDiscountAction = new CartSkuPercentDiscountActionImpl();
		RuleParameter skuDiscountPercentParameter = new RuleParameterImpl(RuleParameter.DISCOUNT_PERCENT_KEY, "5");
		cartSkuPercentDiscountAction.addParameter(skuDiscountPercentParameter);
		RuleParameter skuGuidParameter2 = new RuleParameterImpl(RuleParameter.SKU_CODE_KEY, SKU_GUID);
		cartSkuPercentDiscountAction.addParameter(skuGuidParameter2);
		RuleParameter numSkusParameter2 = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		cartSkuPercentDiscountAction.addParameter(numSkusParameter2);
		promotionRule.addAction(cartSkuPercentDiscountAction);

		//Create an action to add free SKUs to the cart
		RuleAction freeSkusAction = new CartNFreeSkusActionImpl();
		RuleParameter freeSkuCodeParameter = new RuleParameterImpl(RuleParameter.SKU_CODE_KEY, SKU_GUID);
		freeSkusAction.addParameter(freeSkuCodeParameter);
		RuleParameter numFreeSkusParameter = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		freeSkusAction.addParameter(numFreeSkusParameter);
		promotionRule.addAction(freeSkusAction);

		promotionRule.setRuleSet(ruleSet);
		ruleSet.addRule(promotionRule);
		return ruleSet;
	}

	/**
	 * Create a rule set containing the rule: When a cart contains SKU x, apply a discount to items of category y.
	 *
	 * @return a <code>RuleSet</code>
	 */
	public static RuleSet createCartCategoryDiscountRuleSet() {
		final RuleSet ruleSet = createRuleSet(RULE_SET_NAME, RuleScenarios.CART_SCENARIO);

		final Rule promotionRule = createShoppingCartPromotionRule("Order Sale", RuleSetTestUtility.RULE_UID, getStore(), true);

		// Create a condition that the cart contains at least 3 items with a given sku
		RuleCondition skuInCartCondition = new SkuInCartConditionImpl();
		RuleParameter skuCodeParam = new RuleParameterImpl();
		skuCodeParam.setKey(RuleParameter.SKU_CODE_KEY);
		skuCodeParam.setValue("I2004FR12C");
		skuInCartCondition.addParameter(skuCodeParam);
		RuleParameter numItemsQuantifierParam = new RuleParameterImpl();
		numItemsQuantifierParam.setKey(RuleParameter.NUM_ITEMS_QUANTIFIER_KEY);
		numItemsQuantifierParam.setValue("AT_LEAST");
		skuInCartCondition.addParameter(numItemsQuantifierParam);
		RuleParameter quantityParam = new RuleParameterImpl();
		quantityParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		quantityParam.setValue("3");
		skuInCartCondition.addParameter(quantityParam);
		promotionRule.addCondition(skuInCartCondition);

		// Create a condition that the currency is CAD
		RuleCondition currencyCondition = new CartCurrencyConditionImpl();
		RuleParameter currencyParam = new RuleParameterImpl();
		currencyParam.setKey(RuleParameter.CURRENCY_KEY);
		currencyParam.setValue(CAD_CURRENCY);
		currencyCondition.addParameter(currencyParam);
		promotionRule.addCondition(currencyCondition);

		// Create an action
		RuleAction discountAction = new CartCategoryPercentDiscountActionImpl();
		RuleParameter discountParameter = new RuleParameterImpl();
		discountParameter.setKey(RuleParameter.DISCOUNT_PERCENT_KEY);
		discountParameter.setValue("50");
		discountAction.addParameter(discountParameter);
		RuleParameter categoryParameter = new RuleParameterImpl();
		categoryParameter.setKey(RuleParameter.CATEGORY_CODE_KEY);
		categoryParameter.setValue("10");
		discountAction.addParameter(categoryParameter);
		RuleParameter numItemsParameter = new RuleParameterImpl();
		numItemsParameter.setKey(RuleParameter.NUM_ITEMS_KEY);
		numItemsParameter.setValue("10");
		discountAction.addParameter(numItemsParameter);
		promotionRule.addAction(discountAction);

		promotionRule.setRuleSet(ruleSet);
		ruleSet.addRule(promotionRule);
		return ruleSet;
	}

	/**
	 * Create a rule set containing the rule: When a cart contains product x, customer gets third item of x free.
	 *
	 * @return a <code>RuleSet</code> specifying this rule.
	 */
	public static RuleSet createBuyTwoGetOneFreeRuleSet() {

		final RuleSet ruleSet = createRuleSet(RULE_SET_NAME, RuleScenarios.CART_SCENARIO);

		final Rule promotionRule = createShoppingCartPromotionRule("Buy Two Get One Free", RuleSetTestUtility.RULE_UID, getStore(), true);

		// Create a condition that the product is in the cart
		RuleCondition productCondition = new ProductInCartConditionImpl();
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.PRODUCT_CODE_KEY, "10"));
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.NUM_ITEMS_QUANTIFIER_KEY, "AT_LEAST"));
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "1"));
		promotionRule.addCondition(productCondition);

		// Create an action to discount the third item of the product
		RuleAction discountAction = new CartNthProductPercentDiscountActionImpl();
		discountAction.addParameter(new RuleParameterImpl(RuleParameter.DISCOUNT_PERCENT_KEY, "100"));
		discountAction.addParameter(new RuleParameterImpl(RuleParameter.PRODUCT_CODE_KEY, "10"));
		discountAction.addParameter(new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "3"));
		promotionRule.addAction(discountAction);
		promotionRule.setRuleSet(ruleSet);

		ruleSet.addRule(promotionRule);
		return ruleSet;

	}

	/**
	 * Create a rule set containing the rule: When a cart contains product x, customer gets N free items of y.
	 *
	 * @return a <code>RuleSet</code> specifying this rule.
	 */
	public static RuleSet createBuyOneYGetZFreeItemsOfX() {

		final RuleSet ruleSet = createRuleSet(RULE_SET_NAME, RuleScenarios.CART_SCENARIO);

		final Rule promotionRule = createShoppingCartPromotionRule("Buy One Get Three Free", RuleSetTestUtility.RULE_UID, getStore(), true);

		// Create a condition that the product is in the cart
		RuleCondition productCondition = new ProductInCartConditionImpl();
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.PRODUCT_CODE_KEY, "10"));
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.NUM_ITEMS_QUANTIFIER_KEY, "AT_LEAST"));
		productCondition.addParameter(new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "1"));
		promotionRule.addCondition(productCondition);

		// Create an action to discount to give 3 free items of another product
		RuleAction discountAction = new CartNFreeSkusActionImpl();
		discountAction.addParameter(new RuleParameterImpl(RuleParameter.SKU_CODE_KEY, "17"));
		discountAction.addParameter(new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "3"));
		promotionRule.addAction(discountAction);
		promotionRule.setRuleSet(ruleSet);

		ruleSet.addRule(promotionRule);
		return ruleSet;

	}

	/**
	 * Create a rule set containing the rule: When a cart contains a shippable product.
	 *
	 * @return a <code>RuleSet</code> specifying this rule.
	 */
	public static RuleSet createShoppingCartRuleSetWithShippingRule() {

		final RuleSet ruleSet = createRuleSet(RULE_SET_NAME, RuleScenarios.CART_SCENARIO);

		final Rule promotionRule = createShoppingCartPromotionRule("Shippable Product", RuleSetTestUtility.RULE_UID, getStore(), true);

		// Create an action
		RuleAction discountAction = new ShippingAmountDiscountActionImpl();
		RuleParameter discountParameter = new RuleParameterImpl();
		discountParameter.setKey(RuleParameter.DISCOUNT_AMOUNT_KEY);
		discountParameter.setValue("1");
		discountAction.addParameter(discountParameter);
		RuleParameter shippingMethodUidParameter = new RuleParameterImpl(RuleParameter.SHIPPING_OPTION_CODE_KEY, "Code001");
		discountAction.addParameter(shippingMethodUidParameter);
		promotionRule.addAction(discountAction);

		promotionRule.setRuleSet(ruleSet);
		ruleSet.addRule(promotionRule);
		return ruleSet;
	}

	/**
	 * Create a rule set containing the rule: When a cart contains a shippable product with promotions.
	 *
	 * @return a <code>RuleSet</code> specifying this rule.
	 */
	public static RuleSet createShoppingCartRuleSetWithShippingRuleAndLimitedPromos() {

		final RuleSet ruleSet = createRuleSet(RULE_SET_NAME, RuleScenarios.CART_SCENARIO);

		final Rule promotionRule = createShoppingCartPromotionRule("Order Sale", RuleSetTestUtility.RULE_UID, getStore(), true);

		// Create promo condition
		RuleCondition promoCondition = new LimitedUseCouponCodeConditionImpl();
		RuleParameter promoParam = new RuleParameterImpl();
		promoParam.setKey(RuleParameter.LIMITED_USAGE_PROMOTION_ID);
		promoParam.setValue(CAD_CURRENCY);
		promoCondition.addParameter(promoParam);
		promotionRule.addCondition(promoCondition);

		// Create an action
		RuleAction discountAction = new ShippingAmountDiscountActionImpl();
		RuleParameter discountParameter = new RuleParameterImpl();
		discountParameter.setKey(RuleParameter.DISCOUNT_AMOUNT_KEY);
		discountParameter.setValue("1");
		discountAction.addParameter(discountParameter);
		RuleParameter shippingMethodUidParameter = new RuleParameterImpl(RuleParameter.SHIPPING_OPTION_CODE_KEY, "Code001");
		discountAction.addParameter(shippingMethodUidParameter);
		promotionRule.addAction(discountAction);

		promotionRule.setRuleSet(ruleSet);
		ruleSet.addRule(promotionRule);
		return ruleSet;
	}

	/**
	 * @return store object
	 */
	private static Store getStore() {
		Store store = new StoreImpl();
		store.setCatalog(new CatalogImpl());
		return store;
	}

	private static RuleSet createRuleSet(final String setName, final int ruleScenario) {
		final RuleSet ruleSet = new RuleSetImpl();

		ruleSet.setName(setName);
		ruleSet.setScenario(ruleScenario);

		return ruleSet;
	}

	private static Rule createPromotionRule() {
		return new PromotionRuleImpl() {
			private static final long serialVersionUID = 5807437957059025393L;
			private final Set<RuleAction> actions = new HashSet<>();

			@Override
			public void addAction(final RuleAction ruleAction) { //NOPMD
				actions.add(ruleAction);
			}

			@Override
			public void removeAction(final RuleAction ruleAction) {
				actions.remove(ruleAction);
			}

			@Override
			public Set<RuleAction> getActions() {
				return actions;
			}
		};

	}

	private static Rule createCatalogPromotionRule(final String name, final long uidPk, final Catalog catalog, final Boolean enable) {
		final Rule promotionRule = createPromotionRule();

		promotionRule.setEnabled(enable);
		promotionRule.setUidPk(uidPk);
		promotionRule.initialize();
		promotionRule.setName(name);
		promotionRule.setCatalog(catalog);

		return promotionRule;
	}

	private static Rule createShoppingCartPromotionRule(final String name, final long uidPk, final Store store, final Boolean enable) {
		final Rule promotionRule = createPromotionRule();

		promotionRule.setEnabled(enable);
		promotionRule.setUidPk(uidPk);
		promotionRule.setName(name);
		promotionRule.setStore(store);

		return promotionRule;
	}

	private RuleSetTestUtility() {
		// Do nothing
	}
}
