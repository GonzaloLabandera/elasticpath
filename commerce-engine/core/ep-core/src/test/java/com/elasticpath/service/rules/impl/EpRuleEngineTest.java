/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.rules.impl;

import static org.hamcrest.Matchers.anyOf;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.drools.core.SessionConfiguration;
import org.jmock.Expectations;
import org.junit.Test;

import com.elasticpath.cache.SimpleTimeoutCache;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.Utility;
import com.elasticpath.commons.util.impl.UtilityImpl;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.catalog.Brand;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.BrandImpl;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.discounts.DiscountItemContainer;
import com.elasticpath.domain.discounts.ShoppingCartDiscountItemContainer;
import com.elasticpath.domain.discounts.TotallingApplier;
import com.elasticpath.domain.discounts.impl.LimitedTotallingApplierImpl;
import com.elasticpath.domain.discounts.impl.ShoppingCartDiscountItemContainerImpl;
import com.elasticpath.domain.rules.EpRuleBase;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleCondition;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.rules.RuleScenarios;
import com.elasticpath.domain.rules.RuleSet;
import com.elasticpath.domain.rules.impl.BrandConditionImpl;
import com.elasticpath.domain.rules.impl.CartCategoryAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartCurrencyConditionImpl;
import com.elasticpath.domain.rules.impl.CartNFreeSkusActionImpl;
import com.elasticpath.domain.rules.impl.CartProductAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSkuAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSkuPercentDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSubtotalAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartSubtotalConditionImpl;
import com.elasticpath.domain.rules.impl.CatalogCurrencyAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.EpRuleBaseImpl;
import com.elasticpath.domain.rules.impl.LimitedUseCouponCodeConditionImpl;
import com.elasticpath.domain.rules.impl.ProductCategoryConditionImpl;
import com.elasticpath.domain.rules.impl.ProductConditionImpl;
import com.elasticpath.domain.rules.impl.PromotionRuleImpl;
import com.elasticpath.domain.rules.impl.RuleParameterImpl;
import com.elasticpath.domain.rules.impl.RuleSetImpl;
import com.elasticpath.domain.rules.impl.ShippingAmountDiscountActionImpl;
import com.elasticpath.domain.sellingcontext.SellingContext;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.rules.EpRuleEngine;
import com.elasticpath.service.rules.PromotionRuleDelegate;
import com.elasticpath.service.rules.PromotionRuleExceptions;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.RuleSetService;
import com.elasticpath.service.shoppingcart.ShoppingItemSubtotalCalculator;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.service.tax.TaxCalculationResult;
import com.elasticpath.service.tax.adapter.TaxAddressAdapter;
import com.elasticpath.service.tax.impl.TaxCalculationResultImpl;
import com.elasticpath.settings.SettingsService;
import com.elasticpath.settings.impl.SettingsServiceImpl;
import com.elasticpath.tags.TagSet;
import com.elasticpath.tags.service.ConditionEvaluatorService;
import com.elasticpath.test.MapBasedSimpleTimeoutCache;
import com.elasticpath.test.factory.RuleSetTestUtility;
import com.elasticpath.test.jmock.AbstractCatalogDataTestCase;

/**
 * Test cases for {@link EpRuleEngineTest}. This test case is used to test
 * integration of the rules subsystem by constructing rules and testing their execution by the
 * rule engine.
 */
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.TooManyMethods" })
public class EpRuleEngineTest extends AbstractCatalogDataTestCase {

	private static final int ORDER_RULESET_ID = 1;

	private static final int PRODUCT_RULESET_ID = 2;

	private DBCompilingRuleEngineImpl ruleEngine;

	private RuleService mockRuleService;

	private StoreService mockStoreService;

	private ConditionEvaluatorService conditionEvaluationService;

	private final SellingContext sellingContext = context.mock(SellingContext.class);
	private final SimpleTimeoutCache<String, SessionConfiguration> mockSimpleTimeoutCache =
		new MapBasedSimpleTimeoutCache<>();

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception -- in case of any errors.
	 */
	@SuppressWarnings("PMD.ExcessiveMethodLength")
	@Override
	public void setUp() throws Exception {
		super.setUp();

		final ShoppingCartDiscountItemContainerImpl discountItemContainer = new ShoppingCartDiscountItemContainerImpl();
		discountItemContainer.setProductSkuLookup(getProductSkuLookup());
		final LimitedTotallingApplierImpl limitedTotallingApplier = new LimitedTotallingApplierImpl();
		limitedTotallingApplier.setProductSkuLookup(getProductSkuLookup());

		stubGetPrototypeBean(ContextIdNames.ATTRIBUTE_USAGE, AttributeUsage.class, AttributeUsageImpl.class);
		stubGetPrototypeBean(ContextIdNames.EP_RULE_BASE, EpRuleBase.class, EpRuleBaseImpl.class);
		stubGetSingletonBean(ContextIdNames.PRODUCT_SERVICE, ProductService.class, getProductService());
		stubGetPrototypeBean(ContextIdNames.SHOPPING_CART_DISCOUNT_ITEM_CONTAINER, ShoppingCartDiscountItemContainer.class, discountItemContainer);
		stubGetPrototypeBean(ContextIdNames.TAX_CALCULATION_RESULT, TaxCalculationResult.class, TaxCalculationResultImpl.class);
		stubGetPrototypeBean(ContextIdNames.TOTALLING_APPLIER, TotallingApplier.class, limitedTotallingApplier);

		TaxAddressAdapter adapter = new TaxAddressAdapter();
		stubGetSingletonBean(ContextIdNames.TAX_ADDRESS_ADAPTER, TaxAddressAdapter.class, adapter);

		final EpRuleEngine mockRuleEngine = context.mock(EpRuleEngine.class);
		stubGetSingletonBean(ContextIdNames.EP_RULE_ENGINE, EpRuleEngine.class, mockRuleEngine);
		stubGetSingletonBean(ContextIdNames.UTILITY, Utility.class, new UtilityImpl());
		stubGetSingletonBean(ContextIdNames.SETTINGS_SERVICE, SettingsService.class, new SettingsServiceImpl());
		context.checking(new Expectations() {
			{
				allowing(mockRuleEngine).fireOrderPromotionRules(with(any(ShoppingCart.class)),
						with(any(CustomerSession.class))
				);
				allowing(mockRuleEngine).fireOrderPromotionSubtotalRules(with(any(ShoppingCart.class)),
						with(any(CustomerSession.class))
				);
				allowing(sellingContext).isSatisfied(
						with(any(ConditionEvaluatorService.class)), with(any(TagSet.class)), with(any(String[].class)));
				will(returnValue(RuleValidationResultEnum.SUCCESS));
			}
		});
		ruleEngine = new DBCompilingRuleEngineImpl() {
			@Override
			protected Date getLastSuccessfulCompilationBeginDate() {
				return new Date();
			}

			@Override
			protected void setLastSuccessfulCompilationBeginDate(final Date compilationBeginDate) {
				// do nothing
			}

			@Override
			public TimeService getTimeService() {
				return createMockedTimeService();
			}

			@Override
			protected List<Object[]> getSellingContextsForRules(final String storeCode) {
				return Collections.singletonList(new Object[]{
						RuleSetTestUtility.RULE_UID, sellingContext
				});
			}
		};
		ruleEngine.setStatefulSessionConfiguration(mockSimpleTimeoutCache);
		ruleEngine.setBeanFactory(getBeanFactory());

		// Mock the rule set service
		RuleSet catalogRuleSet = createCatalogRuleSet();
		RuleSet orderRuleSet = createShoppingCartRuleSet();

		ruleEngine.setRuleSetService(createMockRuleSetService(catalogRuleSet, orderRuleSet));

		PromotionRuleDelegateImpl delegate = new PromotionRuleDelegateImpl();
		ruleEngine.setPromotionRuleDelegate(delegate);

		mockStoreService = context.mock(StoreService.class);
		ruleEngine.setStoreService(mockStoreService);

		mockRuleService = context.mock(RuleService.class);
		context.checking(new Expectations() {
			{
				allowing(mockRuleService).findRuleBaseByScenario(
						with(anyOf(aNull(Store.class), any(Store.class))),
						with(anyOf(aNull(Catalog.class), any(Catalog.class))),
						with(any(int.class)));
				will(returnValue(null));

				allowing(mockRuleService).saveOrUpdateRuleBase(with(any(EpRuleBase.class)));
			}
		});

		ruleEngine.setRuleService(mockRuleService);

		stubGetPrototypeBean(ContextIdNames.PROMOTION_RULE_EXCEPTIONS, PromotionRuleExceptions.class, new PromotionRuleExceptionsImpl());

		conditionEvaluationService = context.mock(ConditionEvaluatorService.class);
		ruleEngine.setConditionEvaluatorService(conditionEvaluationService);
	}

	private TimeService createMockedTimeService() {
		final TimeService timeService = context.mock(TimeService.class, "ruleEngineTimeService");
		context.checking(new Expectations() {
			{
				atLeast(1).of(timeService).getCurrentTime();
				will(returnValue(new Date(System.currentTimeMillis())));
			}
		});
		return timeService;
	}

	/**
	 * This method test fires a number of Catalog rules and essentially serves
	 * as an integration test for those rules by ensuring that their rule engine
	 * syntax is correct. The rules are configured in createMockCatalogRuleSet().
	 */
	@Test
	public void testFireCatalogRules() {
		getShoppingCart();
		Product product = getShippableSku().getProduct();
		Brand brand = new BrandImpl();
		product.setBrand(brand);

		// Mock the promotion rule delegate
		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		ruleEngine.setRuleSetService(createMockRuleSetService(createCatalogRuleSet(), getEmptyRuleSet()));
		context.checking(new Expectations() {
			{
				atLeast(1).of(mockDelegate).catalogProductInCategory(
						with(any(Product.class)), with(any(boolean.class)), with(any(String.class)), with(any(String.class)));
				will(returnValue(true));

				atLeast(1).of(mockDelegate).catalogProductIs(
						with(any(Product.class)), with(any(boolean.class)), with(any(String.class)), with(any(String.class)));
				will(returnValue(true));

				atLeast(1).of(mockDelegate).catalogBrandIs(
						with(any(Product.class)), with(any(boolean.class)), with(any(String.class)), with(any(String.class)));
				will(returnValue(true));

				oneOf(mockDelegate).applyCatalogCurrencyDiscountAmount(
						with(any(long.class)),
						with(any(long.class)),
						with(aNull(Object.class)),
						with(any(String.class)),
						with(any(String.class)),
						with(any(String.class)));
			}
		});

		ruleEngine.setPromotionRuleDelegate(mockDelegate);
		ruleEngine.fireCatalogPromotionRules(Arrays.asList(product),
				Currency.getInstance("USD"), getStore(), new HashMap<>());
	}

	@Override
	protected Product newProductImpl() {
		return new ProductImpl() {
			private static final long serialVersionUID = 5000000001L;
			@Override
			public String getDisplayName(final Locale locale) {
				return "Test Display Name";
			}
		};
	}

	/**
	 * This method tests that required parameters are checked for
	 * appropriately.
	 */
	@SuppressWarnings("PMD.EmptyCatchBlock")
	@Test
	public void testFireCatalogRulesRequiredParameters() {
		List<Product> products = new ArrayList<>();

		try {
			ruleEngine.fireCatalogPromotionRules(products,
					Currency.getInstance("USD"), null, new HashMap<>());
			fail("Shouldn't run rules with a null store");
		} catch (IllegalArgumentException expected) {
			// Shouldn't be able to run rules with a null store
		}
	}

	/**
	 *
	 * @return
	 */
	private Store getStore() {
		Store store = new StoreImpl();
		Catalog catalog = new CatalogImpl();
		store.setCatalog(catalog);
		store.setCode("STORE_CODE");

		return store;
	}

	private PromotionRuleDelegate createMockDelegate() {
		final PromotionRuleDelegate mockDelegate = context.mock(PromotionRuleDelegate.class);
		context.checking(new Expectations() {
			{
				allowing(mockDelegate).checkDateRange(with(any(String.class)), with(any(String.class)));
				will(returnValue(Boolean.TRUE));

				allowing(mockDelegate).checkEnabled(with(any(String.class)));
				will(returnValue(Boolean.TRUE));
			}
		});
		return mockDelegate;
	}

	/**
	 * Test method for 'com.elasticpath.domain.rules.impl.EpRuleEngineImpl.fireOrderPromotionRules(Product, ShoppingCart)'.
	 */
	@Test
	public void testFireShoppingCartRules() {
		ShoppingCart shoppingCart = givenACartWithAShoppingItemSubtotalCalculator();

		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), createShoppingCartRuleSet()));
		// Mock the promotion rule delegate
		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		context.checking(new Expectations() {
			{
				atLeast(1).of(mockDelegate)
						.cartSubtotalAtLeast(with(any(DiscountItemContainer.class)),
								with(any(String.class)),
								with(any(String.class)));
				will(returnValue(true));

				atLeast(1).of(mockDelegate).cartCurrencyMatches(with(any(ShoppingCart.class)), with(any(String.class)));
				will(returnValue(true));

				atLeast(1).of(mockDelegate).calculateAvailableDiscountQuantity(
						with(any(ShoppingCart.class)), with(any(long.class)), with(any(int.class)));
				will(returnValue(1));
			}
		});

		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, shoppingCart.getCustomerSession());
		ruleEngine.fireOrderPromotionRules(shoppingCart, shoppingCart.getCustomerSession());
	}

	private ShoppingCart givenACartWithAShoppingItemSubtotalCalculator() {
		final ShoppingCartImpl shoppingCart = getShoppingCart();
		stubGetSingletonBean(ContextIdNames.SHOPPING_ITEM_SUBTOTAL_CALCULATOR, ShoppingItemSubtotalCalculator.class,
				getShoppingItemSubtotalCalculator());
		final Currency currency = shoppingCart.getCustomerSession().getCurrency();
		context.checking(new Expectations() {
			{
				allowing(getShoppingItemSubtotalCalculator()).calculate(shoppingCart.getApportionedLeafItems(), shoppingCart, currency);
				will(returnValue(Money.valueOf(BigDecimal.ZERO, currency)));
			}
		});
		return shoppingCart;
	}

	/**
	 * Tests execution of the following rule on the rule engine: When a cart contains SKU x, apply a discount to items of category y.
	 */
	@Test
	public void testCategoryCartDiscountRule() {
		RuleSet orderRuleSet = RuleSetTestUtility.createCartCategoryDiscountRuleSet();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCart shoppingCart = getShoppingCart();

		// Mock the promotion rule delegate
		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		context.checking(new Expectations() {
			{
				atLeast(1).of(mockDelegate).cartContainsSku(
						with(any(ShoppingCart.class)), with(any(String.class)), with(any(String.class)), with(any(int.class)));
				will(returnValue(true));

				atLeast(1).of(mockDelegate).cartCurrencyMatches(with(any(ShoppingCart.class)), with(any(String.class)));
				will(returnValue(true));

				atLeast(1).of(mockDelegate).calculateAvailableDiscountQuantity(
						with(any(ShoppingCart.class)), with(any(long.class)), with(any(int.class)));
				will(returnValue(1));
			}
		});

		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionRules(shoppingCart, shoppingCart.getCustomerSession());

	}

	/**
	 * Tests execution of a shipping amount discount rule.
	 */
	@Test
	public void testShippingAmountDiscountRule() {
		RuleSet orderRuleSet = createShoppingCartRuleSetWithShippingRule();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCartImpl shoppingCart = getShoppingCart();

		// Mock the promotion rule delegate
		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		context.checking(new Expectations() {
			{
				oneOf(mockDelegate).applyShippingDiscountAmount(
						with(any(ShoppingCart.class)),
						with(any(DiscountItemContainer.class)),
						with(any(long.class)),
						with(any(long.class)),
						with(any(String.class)),
						with(any(String.class)),
						with(any(Currency.class)));
			}
		});

		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, shoppingCart.getCustomerSession());
	}

	/**
	 * Tests that execution of a shipping rule does not occur when the
	 * promotion code is incorrect.
	 */
	@Test
	public void testPromotionCodeIncorrect() {
		stubGetPrototypeBean(ContextIdNames.LIMITED_USE_COUPON_CODE_COND, RuleCondition.class, LimitedUseCouponCodeConditionImpl.class);

		// Assign a promo code to a rule
		RuleSet orderRuleSet = createShoppingCartRuleSetWithShippingRule();
		Rule rule = orderRuleSet.getRules().iterator().next();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCart shoppingCart = getShoppingCart();

		rule.setCouponEnabled(true);

		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		context.checking(new Expectations() {
			{
				never(mockDelegate).applyShippingDiscountAmount(
						with(any(ShoppingCart.class)),
						with(any(DiscountItemContainer.class)),
						with(any(long.class)),
						with(any(long.class)),
						with(any(String.class)),
						with(any(String.class)),
						with(any(Currency.class)));

				atLeast(1).of(mockDelegate).cartHasValidLimitedUseCouponCode(
						with(any(ShoppingCart.class)),
						with(any(long.class)));
				will(returnValue(false));
			}
		});

		ruleEngine.setPromotionRuleDelegate(mockDelegate);
		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, shoppingCart.getCustomerSession());
	}

	/**
	 * Tests that execution of a shipping rule does occur when the
	 * promotion code is correct.
	 */
	@Test
	public void testPromotionCodeCorrect() {
		stubGetPrototypeBean(ContextIdNames.LIMITED_USE_COUPON_CODE_COND, RuleCondition.class, LimitedUseCouponCodeConditionImpl.class);

		// Assign a promo code to a rule
		RuleSet orderRuleSet = createShoppingCartRuleSetWithShippingRule();
		Rule rule = orderRuleSet.getRules().iterator().next();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCartImpl shoppingCart = getShoppingCart();

		rule.setCouponEnabled(true);

		// Test that when cartHasValidLimitedUseCouponCode() returns true, the apply discount method is called
		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		context.checking(new Expectations() {
			{
				oneOf(mockDelegate).applyShippingDiscountAmount(
						with(any(ShoppingCart.class)),
						with(any(DiscountItemContainer.class)),
						with(any(long.class)),
						with(any(long.class)),
						with(any(String.class)),
						with(any(String.class)),
						with(any(Currency.class)));

				atLeast(1).of(mockDelegate).cartHasValidLimitedUseCouponCode(
						with(any(ShoppingCart.class)),
						with(any(long.class)));
				will(returnValue(true));
			}
		});

		ruleEngine.setPromotionRuleDelegate(mockDelegate);
		ruleEngine.fireOrderPromotionSubtotalRules(shoppingCart, shoppingCart.getCustomerSession());
	}

	private RuleSet getEmptyRuleSet() {
		RuleSet ruleSet = new RuleSetImpl();
		ruleSet.setRules(new HashSet<>());
		ruleSet.setName("NullRuleSet");
		ruleSet.setScenario(RuleScenarios.CATALOG_BROWSE_SCENARIO);
		return ruleSet;
	}

	/**
	 * Tests execution of the following rule on the rule engine: When a cart contains product x, customer gets third item of x free.
	 */
	@Test
	public void testBuy3Get1FreeDiscountRule() {
		// Mock the rule set service
		RuleSet orderRuleSet = RuleSetTestUtility.createBuyTwoGetOneFreeRuleSet();
		ruleEngine.setRuleSetService(createMockRuleSetService(getEmptyRuleSet(), orderRuleSet));

		ShoppingCart shoppingCart = getShoppingCart();

		// Mock the promotion rule delegate
		final PromotionRuleDelegate mockDelegate = createMockDelegate();
		context.checking(new Expectations() {
			{
				atLeast(1).of(mockDelegate).cartContainsProduct(
						with(any(ShoppingCart.class)),
						with(any(String.class)),
						with(any(String.class)),
						with(any(int.class)),
						with(any(String.class)));
				will(returnValue(true));
			}
		});
		ruleEngine.setPromotionRuleDelegate(mockDelegate);

		ruleEngine.fireOrderPromotionRules(shoppingCart, shoppingCart.getCustomerSession());

	}

	private RuleSetService createMockRuleSetService(final RuleSet catalogRuleSet, final RuleSet orderRuleSet) {
		final RuleSetService mockRuleSetService = context.mock(
				RuleSetService.class, String.format("rule set service %d", System.nanoTime()));
		context.checking(new Expectations() {
			{
				allowing(mockRuleSetService).findByScenarioId(PRODUCT_RULESET_ID);
				will(returnValue(catalogRuleSet));

				allowing(mockRuleSetService).findByScenarioId(ORDER_RULESET_ID);
				will(returnValue(orderRuleSet));
			}
		});

		final List<RuleSet> ruleSets = new ArrayList<>();
		ruleSets.add(catalogRuleSet);
		ruleSets.add(orderRuleSet);
		context.checking(new Expectations() {
			{
				allowing(mockRuleSetService).findByModifiedDate(with(any(Date.class)));
				will(returnValue(ruleSets));
			}
		});

		return mockRuleSetService;
	}

	private RuleSet createCatalogRuleSet() {
		RuleSet ruleSet = new RuleSetImpl();
		ruleSet.setName("com.elasticpath.rules");
		ruleSet.setScenario(RuleScenarios.CATALOG_BROWSE_SCENARIO);

		Rule promotionRule = new PromotionRuleImpl() {
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
		promotionRule.setEnabled(true);
		promotionRule.setUidPk(RuleSetTestUtility.RULE_UID);
		promotionRule.initialize();
		promotionRule.setName("Car Sale");
		promotionRule.setCatalog(new CatalogImpl());

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
		currencyParam.setValue("CAD");

		// Create an action
		RuleAction discountAction = new CatalogCurrencyAmountDiscountActionImpl();
		RuleParameter discountParameter = new RuleParameterImpl();
		discountParameter.setKey(RuleParameter.DISCOUNT_AMOUNT_KEY);
		discountParameter.setValue("100");
		discountAction.addParameter(currencyParam);
		discountAction.addParameter(discountParameter);
		promotionRule.addAction(discountAction);

		// promotionRule.addAction(discountAction);
		promotionRule.setRuleSet(ruleSet);
		ruleSet.addRule(promotionRule);
		return ruleSet;
	}

	private RuleSet createShoppingCartRuleSet() { //NOPMD
		RuleSet ruleSet = new RuleSetImpl();
		ruleSet.setName("com.elasticpath.orderrules");
		ruleSet.setScenario(RuleScenarios.CART_SCENARIO);

		Rule promotionRule = new PromotionRuleImpl() {
			private static final long serialVersionUID = 527923595161646027L;
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
		promotionRule.setEnabled(true);
		promotionRule.setUidPk(RuleSetTestUtility.RULE_UID);
		promotionRule.setName("Order Sale");
		promotionRule.setStore(new StoreImpl());

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
		currencyParam.setValue("CAD");
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
		RuleParameter productIdParameter = new RuleParameterImpl(RuleParameter.PRODUCT_CODE_KEY, String.valueOf(PRODUCT_UID_1));
		cartProductAmountDiscountAction.addParameter(productIdParameter);
		RuleParameter numProductItemsParameter = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		cartProductAmountDiscountAction.addParameter(numProductItemsParameter);
		promotionRule.addAction(cartProductAmountDiscountAction);

		//Create a CartSkuAmountDiscountAction
		RuleAction cartSkuAmountDiscountAction = new CartSkuAmountDiscountActionImpl();
		RuleParameter skuDiscountAmountParameter = new RuleParameterImpl(RuleParameter.DISCOUNT_AMOUNT_KEY, "5");
		cartSkuAmountDiscountAction.addParameter(skuDiscountAmountParameter);
		RuleParameter skuGuidParameter = new RuleParameterImpl(RuleParameter.SKU_CODE_KEY, SKU_GUID_1);
		cartSkuAmountDiscountAction.addParameter(skuGuidParameter);
		RuleParameter numSkusParameter = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		cartSkuAmountDiscountAction.addParameter(numSkusParameter);
		promotionRule.addAction(cartSkuAmountDiscountAction);

		//Create a CartSkuPercentDiscountAction
		RuleAction cartSkuPercentDiscountAction = new CartSkuPercentDiscountActionImpl();
		RuleParameter skuDiscountPercentParameter = new RuleParameterImpl(RuleParameter.DISCOUNT_PERCENT_KEY, "5");
		cartSkuPercentDiscountAction.addParameter(skuDiscountPercentParameter);
		RuleParameter skuGuidParameter2 = new RuleParameterImpl(RuleParameter.SKU_CODE_KEY, SKU_GUID_1);
		cartSkuPercentDiscountAction.addParameter(skuGuidParameter2);
		RuleParameter numSkusParameter2 = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		cartSkuPercentDiscountAction.addParameter(numSkusParameter2);
		promotionRule.addAction(cartSkuPercentDiscountAction);

		//Create an action to add free SKUs to the cart
		RuleAction freeSkusAction = new CartNFreeSkusActionImpl();
		RuleParameter freeSkuCodeParameter = new RuleParameterImpl(RuleParameter.SKU_CODE_KEY, SKU_GUID_1);
		freeSkusAction.addParameter(freeSkuCodeParameter);
		RuleParameter numFreeSkusParameter = new RuleParameterImpl(RuleParameter.NUM_ITEMS_KEY, "2");
		freeSkusAction.addParameter(numFreeSkusParameter);
		promotionRule.addAction(freeSkusAction);

		promotionRule.setRuleSet(ruleSet);
		ruleSet.addRule(promotionRule);
		return ruleSet;
	}

	private RuleSet createShoppingCartRuleSetWithShippingRule() {
		RuleSet ruleSet = new RuleSetImpl();
		ruleSet.setName("com.elasticpath.orderrules");
		ruleSet.setScenario(RuleScenarios.CART_SCENARIO);

		Rule promotionRule = new PromotionRuleImpl() {
			private static final long serialVersionUID = 8570985009402971328L;
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
		promotionRule.setEnabled(true);
		promotionRule.setUidPk(RuleSetTestUtility.RULE_UID);
		promotionRule.setName("Order Sale");
		promotionRule.setStore(new StoreImpl());

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

}
