/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.rules.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.SimpleCache;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.discounts.DiscountItemContainer;
import com.elasticpath.domain.discounts.ShoppingCartDiscountItemContainer;
import com.elasticpath.domain.discounts.impl.ShoppingCartDiscountItemContainerImpl;
import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.rules.RuleSet;
import com.elasticpath.domain.rules.impl.EpRuleBaseImpl;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.service.rules.SellingContextRuleSummary;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shopper.ShopperMemento;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.rules.PromotionRuleDelegate;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.RuleSetService;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.service.ConditionEvaluatorService;
import com.elasticpath.test.factory.RuleSetTestUtility;

/**
 * Test cases for {@link EpRuleEngineTest}. This test case is used to test
 * integration of the rules subsystem by constructing rules and testing their execution by the
 * rule engine.
 */
@RunWith(MockitoJUnitRunner.class)
public class EpRuleEngineTest {

	private static final int ORDER_RULESET_ID = 1;

	private static final int PRODUCT_RULESET_ID = 2;

	private static final String CART_RULE_IDS = "CART_RULE_IDS";

	private static final String STORE_CODE = "MOBEE";

	private static final String CATALOG_CODE = "CATALOG_CODE";

	private static final String USD_CURRENCY = "USD";

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private RuleService ruleService;

	@Mock
	private Shopper shopper;

	@Mock
	private Store store;

	@Mock
	private TagSet tagSet;

	@Mock
	private CustomerSession customerSession;

	@Mock
	private ConditionEvaluatorService mockConditionEvaluatorService;

	@Mock
	private SellingContext sellingContext;

	@Mock
	private RuleEngineSessionFactory ruleEngineSessionFactory;

	@Spy
	private RuleEngineRuleStrategy ruleEngineRuleStrategy;

	@Spy
	@InjectMocks
	private DBCompilingRuleEngineImpl ruleEngine;

	private final ShoppingCartDiscountItemContainer shoppingCartDiscountItemContainer = new ShoppingCartDiscountItemContainerImpl();

	private final EpRuleBase epRuleBase = new EpRuleBaseImpl();

	@Before
	public void setup() {
		mockStore();

		mockShopper();

		mockSellingContext();

		mockRuleEngineSessionFactory();

		mockRuleEngineRuleFactory();

		mockBeanFactory();

		mockRuleService();

		setupRuleEngine();
	}

	/**
	 * This method test fires a number of Catalog rules and essentially serves
	 * as an integration test for those rules by ensuring that their rule engine
	 * syntax is correct.
	 */
	@Test
	public void testFireCatalogRules() {

		// Given
		final Product product = mock(Product.class);

		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		final RuleSet catalogRuleSet = RuleSetTestUtility.createCatalogRuleSet();

		when(mockDelegate.catalogProductInCategory(eq(product), anyBoolean(), anyString(), anyString())).thenReturn(true);
		when(mockDelegate.catalogProductIs(eq(product), anyBoolean(), anyString(), anyString())).thenReturn(true);
		when(mockDelegate.catalogBrandIs(eq(product), anyBoolean(), anyString(), anyString())).thenReturn(true);

		ruleEngine.setRuleSetService(createMockRuleSetService(catalogRuleSet, getEmptyRuleSet()));

		// When
		ruleEngine.fireCatalogPromotionRules(Arrays.asList(product), Currency.getInstance(USD_CURRENCY), store, new HashMap<>(), new TagSet());

		// Verify
		verify(mockDelegate, times(1)).checkDateRange(anyString(), anyString());
		verify(mockDelegate, times(1)).checkEnabled(anyString());

		verify(mockDelegate, atLeastOnce()).catalogBrandIs(eq(product), anyBoolean(), anyString(), anyString());
		verify(mockDelegate, atLeastOnce()).catalogProductIs(eq(product), anyBoolean(), anyString(), anyString());
		verify(mockDelegate, atLeastOnce()).catalogProductInCategory(eq(product), anyBoolean(), anyString(), anyString());
		verify(mockDelegate, times(1)).applyCatalogCurrencyDiscountAmount(anyLong(), anyLong(), isNull(), anyString(), anyString(), anyString());

	}

	/**
	 * This method tests that the required products parameter is checked for appropriately.
	 */
	@Test
	public void testFireCatalogRulesRequiredProductsParameter() {

		ruleEngine.fireCatalogPromotionRules(new ArrayList<>(), Currency.getInstance(USD_CURRENCY), store, new HashMap<>(), new TagSet());

		verify(ruleEngineRuleStrategy, never()).evaluateApplicableRules(any(Catalog.class), any(TagSet.class));

	}

	/**
	 * This method tests that the required store parameter is checked for appropriately.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFireCatalogRulesRequiredStoreParameter() {

		ruleEngine.fireCatalogPromotionRules(new ArrayList<>(), Currency.getInstance(USD_CURRENCY), null, new HashMap<>(), new TagSet());

	}

	/**
	 * This method tests that the required shopping carts parameter in promotion rules is checked for appropriately.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFirePromotionRulesRequiredShoppingCartParameter() {

		ruleEngine.fireOrderPromotionRules(null, customerSession);

	}


	/**
	 * This method tests that the required shopping carts parameter in promotion subtotal rules is checked for appropriately.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFirePromotionSubtotalRulesRequiredShoppingCartParameter() {

		ruleEngine.fireOrderPromotionSubtotalRules(null, customerSession);

	}

	/**
	 * This method tests that shopping cart's store field is not null and the uidpks doesn't return empty.
	 */
	@Test(expected =  NullPointerException.class)
	public void testFirePromotionRulesRequiredStore() {
		ShoppingCart shoppingCart = createMockShoppingCart(USD_CURRENCY);
		when(shoppingCart.getStore()).thenReturn(null);

		ruleEngine.fireOrderPromotionRules(shoppingCart, customerSession);

		verify(ruleEngineSessionFactory, never()).getSessionConfiguration();
	}

	/**
	 * Test method for 'com.elasticpath.domain.rules.impl.EpRuleEngineImpl.fireOrderPromotionRules(Product, ShoppingCart)'.
	 */
	@Test
	public void testFireShoppingCartRules() {

		// Given
		final ShoppingCart shoppingCart = createMockShoppingCart(USD_CURRENCY);
		final RuleSet orderRuleSet = RuleSetTestUtility.createShoppingCartRuleSet();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		when(mockDelegate.cartSubtotalAtLeast(any(DiscountItemContainer.class), anyString(), anyString())).thenReturn(true);
		when(mockDelegate.cartCurrencyMatches(eq(shoppingCart), anyString())).thenReturn(true);
		when(mockDelegate.calculateAvailableDiscountQuantity(eq(shoppingCart), anyLong(), anyInt())).thenReturn(1);

		// When
		ruleEngine.fireOrderPromotionRules(shoppingCart, customerSession);
		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, customerSession);

		// Verify
		verify(mockDelegate, atLeast(2)).checkEnabled(anyString());

		verify(mockDelegate, atLeast(2)).cartSubtotalAtLeast(any(ShoppingCartDiscountItemContainer.class), anyString(), anyString());
		verify(mockDelegate, atLeast(2)).cartCurrencyMatches(eq(shoppingCart), anyString());
		verify(mockDelegate, atLeastOnce()).calculateAvailableDiscountQuantity(eq(shoppingCart), anyLong(), anyInt());

	}

	/**
	 * Tests execution of the following rule on the rule engine: When a cart contains SKU x, apply a discount to items of category y.
	 */
	@Test
	public void testCategoryCartDiscountRule() {

		// Given
		final RuleSet orderRuleSet = RuleSetTestUtility.createCartCategoryDiscountRuleSet();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		final ShoppingCart shoppingCart = createMockShoppingCart(USD_CURRENCY);

		final PromotionRuleDelegate mockDelegate = createMockDelegate();

		when(mockDelegate.cartContainsSku(eq(shoppingCart), anyString(), anyString(), anyInt())).thenReturn(true);
		when(mockDelegate.cartCurrencyMatches(eq(shoppingCart), anyString())).thenReturn(true);
		when(mockDelegate.calculateAvailableDiscountQuantity(eq(shoppingCart), anyLong(), anyInt())).thenReturn(1);

		// When
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionRules(shoppingCart, customerSession);

		// Verify
		verify(mockDelegate, atLeastOnce()).cartContainsSku(eq(shoppingCart), anyString(), anyString(), anyInt());
		verify(mockDelegate, atLeastOnce()).cartCurrencyMatches(eq(shoppingCart), anyString());
		verify(mockDelegate, atLeastOnce()).calculateAvailableDiscountQuantity(eq(shoppingCart), anyLong(), anyInt());

	}

	/**
	 * Tests execution of a shipping amount discount rule.
	 */
	@Test
	public void testShippingAmountDiscountRule() {

		// Given
		final RuleSet orderRuleSet = RuleSetTestUtility.createShoppingCartRuleSetWithShippingRule();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		final ShoppingCart shoppingCart = createMockShoppingCart(USD_CURRENCY);
		Currency currency = customerSession.getCurrency();

		final PromotionRuleDelegate mockDelegate = createMockDelegate();

		// When
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, customerSession);

		// Verify
		verify(mockDelegate, times(1)).applyShippingDiscountAmount(
				eq(shoppingCart),
				any(DiscountItemContainer.class),
				anyLong(),
				anyLong(),
				anyString(),
				anyString(),
				eq(currency)
		);

	}

	/**
	 * Tests that execution of a shipping rule does not occur when the
	 * promotion code is incorrect.
	 */
	@Test
	public void testPromotionCodeIncorrect() {

		// Given
		final RuleSet orderRuleSet = RuleSetTestUtility.createShoppingCartRuleSetWithShippingRuleAndLimitedPromos();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCart shoppingCart = createMockShoppingCart(USD_CURRENCY);

		final PromotionRuleDelegate mockDelegate = createMockDelegate();

		when(mockDelegate.cartHasValidLimitedUseCouponCode(eq(shoppingCart), anyLong())).thenReturn(false);

		// When
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, customerSession);

		// Verify
		verify(mockDelegate, never()).applyShippingDiscountAmount(
				any(ShoppingCart.class),
				any(DiscountItemContainer.class),
				anyLong(),
				anyLong(),
				anyString(),
				anyString(),
				any(Currency.class));

	}

	/**
	 * Tests that execution of a shipping rule does occur when the
	 * promotion code is correct.
	 */
	@Test
	public void testPromotionCodeCorrect() {

		// Given
		final RuleSet orderRuleSet = RuleSetTestUtility.createShoppingCartRuleSetWithShippingRuleAndLimitedPromos();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCart shoppingCart = createMockShoppingCart(USD_CURRENCY);

		final PromotionRuleDelegate mockDelegate = createMockDelegate();

		when(mockDelegate.cartHasValidLimitedUseCouponCode(eq(shoppingCart), anyLong())).thenReturn(true);

		// When
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, customerSession);

		// Verify
		verify(mockDelegate, atLeastOnce()).applyShippingDiscountAmount(
				any(ShoppingCart.class),
				any(DiscountItemContainer.class),
				anyLong(),
				anyLong(),
				anyString(),
				anyString(),
				any(Currency.class));
	}

	/**
	 * Tests execution of the following rule on the rule engine: When a cart contains product x, customer gets third item of x free.
	 */
	@Test
	public void testBuy3Get1FreeDiscountRule() {

		// Given
		final RuleSet orderRuleSet = RuleSetTestUtility.createBuyTwoGetOneFreeRuleSet();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCart shoppingCart = createMockShoppingCart(USD_CURRENCY);

		final PromotionRuleDelegate mockDelegate = createMockDelegate();

		when(mockDelegate.cartContainsProduct(eq(shoppingCart), anyString(), anyString(), anyInt(), anyString())).thenReturn(true);

		// When
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionRules(shoppingCart, shoppingCart.getCustomerSession());

		// Then
		verify(mockDelegate, atLeastOnce()).cartContainsProduct(eq(shoppingCart), anyString(), anyString(), anyInt(), anyString());

	}

	private RuleSetService createMockRuleSetService(final RuleSet catalogRuleSet, final RuleSet orderRuleSet) {
		final RuleSetService mockRuleSetService = mock(RuleSetService.class, String.format("rule set service %d", System.nanoTime()));

		when(mockRuleSetService.findByScenarioId(PRODUCT_RULESET_ID)).thenReturn(catalogRuleSet);
		when(mockRuleSetService.findByScenarioId(ORDER_RULESET_ID)).thenReturn(orderRuleSet);

		return mockRuleSetService;
	}

	private RuleSet getEmptyRuleSet() {
		return mock(RuleSet.class);
	}

	private ShoppingCart createMockShoppingCart(final String currencyCode) {
		final ShoppingCart shoppingCart = mock(ShoppingCart.class);
		when(shoppingCart.getCustomerSession()).thenReturn(customerSession);

		final Currency currency = Currency.getInstance(currencyCode);
		when(customerSession.getCurrency()).thenReturn(currency);

		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shoppingCart.getStore()).thenReturn(store);

		return shoppingCart;
	}

	private PromotionRuleDelegate createMockDelegate() {
		PromotionRuleDelegate mockDelegate = mock(PromotionRuleDelegate.class);

		when(mockDelegate.checkDateRange(anyString(), anyString())).thenReturn(true);
		when(mockDelegate.checkEnabled(anyString())).thenReturn(true);

		return mockDelegate;
	}

	private void mockStore() {
		Catalog catalog = mock(Catalog.class);

		when(store.getCatalog()).thenReturn(catalog);
		when(store.getCode()).thenReturn(STORE_CODE);
		when(catalog.getCode()).thenReturn(CATALOG_CODE);
	}

	private void mockShopper() {
		final SimpleCache simpleCache = mock(SimpleCache.class);
		final ShopperMemento shopperMemento = mock(ShopperMemento.class);

		when(shopper.getTagSet()).thenReturn(tagSet);
		when(shopper.getCache()).thenReturn(simpleCache);
		when(shopper.getShopperMemento()).thenReturn(shopperMemento);

		when(shopperMemento.getStoreCode()).thenReturn(STORE_CODE);

		when(simpleCache.isInvalidated(CART_RULE_IDS)).thenReturn(true);
	}

	private void mockSellingContext() {
		when(sellingContext.isSatisfied(any(ConditionEvaluatorService.class), any(TagSet.class), anyString(), anyString()))
				.thenReturn(RuleValidationResultEnum.SUCCESS);
	}

	private void mockRuleEngineRuleFactory() {

		ruleEngine.setRuleEngineRuleStrategy(ruleEngineRuleStrategy);

		ruleEngineRuleStrategy.setConditionEvaluatorService(mockConditionEvaluatorService);

		doReturn(Collections.singletonList(new SellingContextRuleSummary(null, RuleSetTestUtility.RULE_UID, sellingContext, null, null)))
				.when(ruleEngineRuleStrategy).getSellingContextWithRuleUidpk(any(String.class), anyInt());

	}

	private void mockRuleEngineSessionFactory() {
		ruleEngine.setRuleEngineSessionFactory(ruleEngineSessionFactory);
	}

	private void setupRuleEngine() {
		final RuleSet catalogRuleSet = RuleSetTestUtility.createCatalogRuleSet();
		final RuleSet orderRuleSet = RuleSetTestUtility.createShoppingCartRuleSet();
		final PromotionRuleDelegate delegate = new PromotionRuleDelegateImpl();

		ruleEngine.setRuleSetService(createMockRuleSetService(catalogRuleSet, orderRuleSet));
		ruleEngine.setPromotionRuleDelegate(delegate);
	}

	private void mockBeanFactory() {
		when(beanFactory.getPrototypeBean(ContextIdNames.SHOPPING_CART_DISCOUNT_ITEM_CONTAINER, ShoppingCartDiscountItemContainer.class))
				.thenReturn(shoppingCartDiscountItemContainer);
		when(beanFactory.getPrototypeBean(ContextIdNames.EP_RULE_BASE, EpRuleBase.class)).thenReturn(epRuleBase);

	}

	private void mockRuleService() {
		when(ruleService.findRuleBaseByScenario(any(), any(), anyInt())).thenReturn(null);
		when(ruleService.saveOrUpdateRuleBase(epRuleBase)).thenReturn(epRuleBase);
	}
}
