/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.service.rules.impl; //NOPMD

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.impl.OrderImpl;
import com.elasticpath.domain.rules.AppliedCoupon;
import com.elasticpath.domain.rules.AppliedRule;
import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.CouponUsage;
import com.elasticpath.domain.rules.CouponUsageType;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.domain.rules.RuleAction;
import com.elasticpath.domain.rules.RuleCondition;
import com.elasticpath.domain.rules.RuleParameter;
import com.elasticpath.domain.rules.impl.AppliedCouponImpl;
import com.elasticpath.domain.rules.impl.AppliedRuleImpl;
import com.elasticpath.domain.rules.impl.CartAnySkuAmountDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartAnySkuPercentDiscountActionImpl;
import com.elasticpath.domain.rules.impl.CartNFreeSkusActionImpl;
import com.elasticpath.domain.rules.impl.CouponAssignmentActionImpl;
import com.elasticpath.domain.rules.impl.CouponConfigImpl;
import com.elasticpath.domain.rules.impl.CouponImpl;
import com.elasticpath.domain.rules.impl.CouponUsageImpl;
import com.elasticpath.domain.rules.impl.LimitedUseCouponCodeConditionImpl;
import com.elasticpath.domain.rules.impl.PromotionRuleImpl;
import com.elasticpath.domain.rules.impl.RuleParameterImpl;
import com.elasticpath.domain.rules.impl.ShippingAmountDiscountActionImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.shoppingcart.PromotionRecordContainer;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ItemDiscountRecordImpl;
import com.elasticpath.domain.shoppingcart.impl.ShippingDiscountRecordImpl;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.service.rules.CouponCodeGenerator;
import com.elasticpath.service.rules.CouponConfigService;
import com.elasticpath.service.rules.CouponService;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.dao.CouponUsageDao;
import com.elasticpath.test.BeanFactoryExpectationsFactory;
import com.elasticpath.test.factory.TestCustomerSessionFactory;

/**
 * Unit test for {@code CouponUsageServiceImpl}.
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
		"PMD.TooManyMethods", "PMD.TooManyStaticImports",
		"PMD.ExcessiveClassLength",
		"PMD.TooManyFields" })
public class CouponUsageServiceImplTest {

	private static final String USER_TEST_EMAIL = "user@test.com";
	private static final String COUPON_CODE = "COUPON_CODE";
	private static final String XYZ = "xyz";
	private static final String ABC = "abc";
	private static final String PREFIX = "prefix-";
	private static final String PREFIX2 = "prefix2-";
	private static final String GENERATED_CODE = PREFIX + "0000001";
	private static final String GENERATED_CODE2 = PREFIX2 + "0000002";
	private static final int THREE = 3;
	private static final int FORTY_NINE = 49;
	private static final int EIGHT = 8;
	private static final int FOUR = 4;
	private static final int TWENTYTEN = 2010;
	private static final int SEVEN = 7;
	private static final int FIFTY_NINE = 59;
	private static final int TWENTY_THREE = 23;
	private static final long RULE_UID = 123L;
	@org.junit.Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private CouponUsageDao dao;
	private CouponUsageServiceImplDouble service;
	private ShoppingCart shoppingCart;
	private TimeService timeService;
	private BeanFactoryExpectationsFactory expectationsFactory;
	private BeanFactory beanFactory;

	@Mock
	private ShoppingCartPricingSnapshot pricingSnapshot;

	@Mock
	private PromotionRecordContainer promotionRecordContainer;

	@Mock
	private RuleService ruleService;

	/**
	 * Test double CouponCodeGenerator.
	 */
	private class CouponCodeGeneratorImpl implements CouponCodeGenerator {

		private String generatedCouponCode1;
		private String generatedCouponCode2;
		private boolean first = true;

		@Override
		public String generateCouponCode(final Coupon coupon, final String couponCodePrefix) {
			if (first) {
				first = false;
				return generatedCouponCode1;
			}
			return generatedCouponCode2;
		}

		public void setGeneratedCouponCode1(final String generatedCouponCode) {
			this.generatedCouponCode1 = generatedCouponCode;
		}

		public void setGeneratedCouponCode2(final String generatedCouponCode) {
			this.generatedCouponCode2 = generatedCouponCode;
		}
	}

	/**
	 * Test Double for verifying two coupon service calls.
	 */
	private class CouponServiceImplDouble extends CouponServiceImpl {
		@Override
		public Coupon addAndGenerateCode(final Coupon coupon, final String couponCodePrefix) {
			String couponCode = getCouponCodeGenerator().generateCouponCode(coupon, couponCodePrefix);
			coupon.setCouponCode(couponCode);
			return coupon;
		}
	}

	/**
	 * Test double for verifying calls.
	 */
	private class CouponUsageServiceImplDouble extends CouponUsageServiceImpl {
		private CouponUsage updatedCouponUsage;

		@Override
		public CouponUsage update(final CouponUsage newCouponUse) throws EpServiceException {
			this.updatedCouponUsage = newCouponUse;
			return newCouponUse;
		}

		@Override
		public CouponUsage add(final CouponUsage newCouponUse) {
			this.updatedCouponUsage = newCouponUse;
			return newCouponUse;
		}

		@Override
		protected void saveOrUpdateCouponUsage(final CouponUsage usage) {
			this.update(usage);
		}

		public CouponUsage getUpdatedCouponUsage() {
			return this.updatedCouponUsage;
		}

	}

	/**
	 * Test double for verifying calls for multiple updates.
	 */
	private class CouponUsageServiceImplDoubleMultipleUpdates extends CouponUsageServiceImpl {
		private final List<CouponUsage> updatedCouponUsages = new ArrayList<>();

		@Override
		public CouponUsage update(final CouponUsage newCouponUse) throws EpServiceException {
			updatedCouponUsages.add(newCouponUse);
			return newCouponUse;
		}

		@Override
		public CouponUsage add(final CouponUsage newCouponUse) {
			updatedCouponUsages.add(newCouponUse);
			return newCouponUse;
		}

		@Override
		protected void saveOrUpdateCouponUsage(final CouponUsage usage) {
			this.update(usage);
		}

		public List<CouponUsage> getUpdatedCouponUsages() {
			return this.updatedCouponUsages;
		}

	}

	/**
	 * Setup objects and mocks required by tests.
	 *
	 * @throws Exception on error
	 */
	@Before
	public void setUp() throws Exception {
		dao = context.mock(CouponUsageDao.class);

		service = new CouponUsageServiceImplDouble();
		service.setCouponUsageDao(dao);
		service.setTimeService(timeService);
		service.setRuleService(ruleService);

		CouponService couponService = context.mock(CouponService.class, "testCouponService");
		service.setCouponService(couponService);

		shoppingCart = context.mock(ShoppingCart.class);
		timeService = context.mock(TimeService.class);

		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);

		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.COUPON_USAGE, CouponUsage.class, CouponUsageImpl.class);
		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.APPLIED_COUPON, AppliedCoupon.class, AppliedCouponImpl.class);

		service.setBeanFactory(beanFactory);

		context.checking(new Expectations() { {
			allowing(shoppingCart).getShopper(); will(returnValue(createShopper()));
			allowing(pricingSnapshot).getPromotionRecordContainer(); will(returnValue(promotionRecordContainer));
		} });
	}

	@After
	public void tearDown() {
		expectationsFactory.close();
	}

	private Shopper createShopper() {
		CustomerSession session = TestCustomerSessionFactory.getInstance().createNewCustomerSession();

		return session.getShopper();
	}

	private void setCustomerWithEmailOnShopper(final Shopper shopper, final String email) {
		final Customer customer = context.mock(Customer.class);
		context.checking(new Expectations() { {
			allowing(customer).getEmail(); will(returnValue(email));
		} });
		shopper.setCustomer(customer);
	}

	private void givenDiscountRecordContainerContainsRecordForRuleAndAction(final Rule rule,
			final RuleAction action,
			final DiscountRecord discountRecord) {
		context.checking(new Expectations() {
			{
				allowing(promotionRecordContainer).getDiscountRecord(rule, action);
				will(returnValue(discountRecord));
			}
		});
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbers() {

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(2);
		couponUsage.setCoupon(coupon);
		final List<CouponUsage> couponUsages = new ArrayList<>();
		couponUsages.add(couponUsage);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "");

		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			oneOf(dao).findByCode(XYZ); will(returnValue(couponUsages));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("Should be the inital value (2) + 1", 2 + 1, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	private Order populateInputOrder() {
		AppliedRuleImpl appliedRule = new AppliedRuleImpl();
		appliedRule.setRuleUid(RULE_UID);
		appliedRule.setAppliedCoupons(new HashSet<>());

		final Set<AppliedRule> appliedRules = new HashSet<>();
		appliedRules.add(appliedRule);

		final Order inputOrder = new OrderImpl();
		inputOrder.setAppliedRules(appliedRules);
		return inputOrder;
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersNoCouponUsage() {

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);
		couponConfig.setUsageLimit(FOUR);
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);
		context.checking(new Expectations() { {
			oneOf(dao).findByCode(XYZ); will(returnValue(Collections.emptyList()));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("First use of coupon so should be 1.", 1, updatedCouponUsage.getUseCount());
		assertEquals("The coupon that matches should be the one that is updated.", coupon, updatedCouponUsage.getCoupon());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	private Rule getTestPromoRule(final long uidPk) {
		final PromotionRuleImpl promotionRule = new PromotionRuleImpl() {
			private static final long serialVersionUID = 1L;
			private final Set<RuleAction> actions = new HashSet<>();

			@Override
			public void addAction(final RuleAction ruleAction) { //NOPMD
				this.actions.add(ruleAction);
			}

			@Override
			public void removeAction(final RuleAction ruleAction) {
				this.actions.remove(ruleAction);
			}

			@Override
			public Set<RuleAction> getActions() {
				return this.actions;
			}
		};

		promotionRule.setUidPk(uidPk);

		return promotionRule;
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Do this for LimitPerAnyUser coupon.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerAnyUser() {
		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(2);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);
		couponUsage.setCoupon(coupon);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			oneOf(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("Should be the inital value (2) + 1", 2 + 1, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Because it is user specific the customer email address
	 * is updated as well.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersNoCouponUsageLimitPerAnyUser() {
		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(EIGHT);
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);


		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@test.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			allowing(dao).findByCouponCodeAndEmail(XYZ, "test@test.com"); will(returnValue(null));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("First use of coupon so should be 1.", 1, updatedCouponUsage.getUseCount());
		assertEquals("The coupon that matches should be the one that is updated.", coupon, updatedCouponUsage.getCoupon());
		assertEquals("Email address in usage should match parameter", "test@test.com", updatedCouponUsage.getCustomerEmailAddress());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Test that, when the coupon for the rule cannot be found, the updateLimitedUsageCouponCurrentNumbers method
	 * does not update the use count.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersNoMatchingCoupon() {

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);

		rule.setCode(ABC);
		rule.addCondition(condition);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final Set<String> promotionCodes = new HashSet<>();

		context.checking(new Expectations() { {
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(Collections.emptySet()));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();
		assertNull("There should be no update", updatedCouponUsage);

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		assertEquals("Coupon should not be applied", 0, actualAppliedRule.getAppliedCoupons().size());
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Do this for LimitPerAnyUser coupon.
	 * When there are two coupons for the same action then the one with the most usage but below
	 * the limit should be used first.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerAnyUserTwoCouponsOneActions() {
		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(2);

		final CouponUsage couponUsage1 = new CouponUsageImpl();
		couponUsage1.setUseCount(1);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);

		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);
		couponUsage.setCoupon(coupon);

		Coupon coupon2 = new CouponImpl();
		coupon2.setCouponCode("uvw");
		coupon2.setCouponConfig(couponConfig);
		coupons.add(coupon2);
		couponUsage1.setCoupon(coupon2);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			oneOf(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			oneOf(dao).findByCouponCodeAndEmail("uvw", "test@abc.com"); will(returnValue(couponUsage1));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("The usage for the coupon with the most current usage should be updated", couponUsage, updatedCouponUsage);
		assertEquals("Should be the inital value (2) + 1", 2 + 1, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the usecount for the coupon. Do this for LimitPerCoupon coupon. When
	 * there are two coupons for the same action but both of them are out of limit, then the one with the smallest usage will be applied
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerCouponTwoCouponsOneActionsOutOfLimit() {
		final CouponUsage largerCouponUsage = new CouponUsageImpl();
		largerCouponUsage.setUseCount(2);

		final CouponUsage smallerCouponUsage = new CouponUsageImpl();
		smallerCouponUsage.setUseCount(1);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);

		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon1 = new CouponImpl();
		coupon1.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);
		couponConfig.setUsageLimit(2);
		coupon1.setCouponConfig(couponConfig);
		coupons.add(coupon1);
		largerCouponUsage.setCoupon(coupon1);

		Coupon coupon2 = new CouponImpl();
		coupon2.setCouponCode("uvw");
		coupon2.setCouponConfig(couponConfig);
		coupons.add(coupon2);
		smallerCouponUsage.setCoupon(coupon2);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), null);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() {
			{
				oneOf(dao).findByCode(XYZ);
				will(returnValue(Arrays.asList(largerCouponUsage)));
				oneOf(dao).findByCode("uvw");
				will(returnValue(Arrays.asList(smallerCouponUsage)));
				oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
				will(returnValue(Collections.singletonList(rule)));
				oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes);
				will(returnValue(coupons));
				allowing(shoppingCart).getPromotionCodes();
				will(returnValue(promotionCodes));
			}
		});

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("The usage for the coupon with the most current usage should be updated", smallerCouponUsage, updatedCouponUsage);
		assertEquals("Should be the inital value (1) + 1", 1 + 1, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the usecount for the coupon. Do this for LimitPerCoupon coupon. When
	 * there is one coupon in the cart but it's out of limit, we should still allow it to be applied to avoid customer's confusion.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerCouponOneCouponOutOfLimit() {
		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(2);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);

		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);
		couponConfig.setUsageLimit(2);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);
		couponUsage.setCoupon(coupon);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), null);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() {
			{
				oneOf(dao).findByCode(XYZ);
				will(returnValue(Arrays.asList(couponUsage)));
				oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
				will(returnValue(Collections.singletonList(rule)));
				oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes);
				will(returnValue(coupons));
				allowing(shoppingCart).getPromotionCodes();
				will(returnValue(promotionCodes));
			}
		});

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("The usage for the coupon with the most current usage should be updated", couponUsage, updatedCouponUsage);
		assertEquals("Should be the inital value (1) + 1", 2 + 1, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Do this for LimitPerAnyUser coupon.
	 * When there are two coupons for the same action then the one that is not used up
	 * should be used.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerAnyUserTwoCouponsOneActionsOneCouponFinished() {

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(FOUR); // Set to the limit below

		final CouponUsage couponUsage1 = new CouponUsageImpl();
		couponUsage1.setUseCount(1);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);
		couponUsage.setCoupon(coupon);

		Coupon coupon2 = new CouponImpl();
		coupon2.setCouponCode("uvw");
		coupon2.setCouponConfig(couponConfig);
		coupons.add(coupon2);
		couponUsage1.setCoupon(coupon2);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			oneOf(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			oneOf(dao).findByCouponCodeAndEmail("uvw", "test@abc.com"); will(returnValue(couponUsage1));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("The usage for the coupon with the most current usage should be updated", couponUsage1, updatedCouponUsage);
		assertEquals("Should be the inital value (1) + 1", 2, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Do this for LimitPerAnyUser coupon.
	 * When there are two coupons for the same action then the one that is not used up
	 * should be used. If the usage is more than is left on the first coupon then should
	 * be split across to the next coupon.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerAnyUserTwoCouponsOneActionsSplitUseAcrossCoupons() {

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(THREE); // Set to the one less than the limit below

		final CouponUsage couponUsage1 = new CouponUsageImpl();
		couponUsage1.setUseCount(1);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		couponUsage.setCoupon(coupon);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);

		Coupon coupon2 = new CouponImpl();
		coupon2.setCouponCode("uvw");
		coupon2.setCouponConfig(couponConfig);
		couponUsage1.setCoupon(coupon2);
		coupons.add(coupon2);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 2);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			exactly(2).of(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			exactly(2).of(dao).findByCouponCodeAndEmail("uvw", "test@abc.com"); will(returnValue(couponUsage1));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		// This double will not allow us to get the first update
		assertEquals("The usage for the second coupon should be updated", couponUsage1, updatedCouponUsage);
		assertEquals("Should be the inital value (1) + 1", 2, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Do this for LimitPerAnyUser coupon.
	 * When there are two coupons for the same action then the one that is not used up
	 * should be used. If the usage is more than is left on the first coupon then should
	 * be split across to the next coupon. The second coupon in this test has no usage.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerSpecifiedUserTwoCouponsOneActionsSplitUseAcrossCouponsSecondCouponNotUsed() {

		CouponUsageServiceImplDoubleMultipleUpdates serviceMultiple = new CouponUsageServiceImplDoubleMultipleUpdates();
		serviceMultiple.setCouponUsageDao(dao);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(THREE); // Set to the one less than the limit below

		final CouponUsage couponUsage1 = new CouponUsageImpl();
		couponUsage1.setUseCount(0);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		serviceMultiple.setRuleService(ruleService);

		final CouponService couponService = context.mock(CouponService.class);
		serviceMultiple.setCouponService(couponService);
		serviceMultiple.setBeanFactory(beanFactory);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		couponUsage.setCoupon(coupon);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);

		Coupon coupon2 = new CouponImpl();
		coupon2.setCouponCode("uvw");
		coupon2.setCouponConfig(couponConfig);
		couponUsage1.setCoupon(coupon2);
		coupons.add(coupon2);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 2);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			exactly(2).of(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			exactly(2).of(dao).findByCouponCodeAndEmail("uvw", "test@abc.com"); will(returnValue(couponUsage1));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		serviceMultiple.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		List<CouponUsage> updatedCouponUsages = serviceMultiple.getUpdatedCouponUsages();

		assertEquals("The usage for the first coupon should be updated", couponUsage, updatedCouponUsages.get(0));
		assertEquals("Should be the inital value (3) + 1", FOUR, updatedCouponUsages.get(0).getUseCount());
		assertEquals("The usage for the second coupon should be updated", couponUsage1, updatedCouponUsages.get(1));
		assertEquals("Should be the inital value (0) + 1", 1, updatedCouponUsages.get(1).getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		Iterator<AppliedCoupon> appliedCouponsIterator = actualAppliedRule.getAppliedCoupons().iterator();
		AppliedCoupon appliedCoupon1 = appliedCouponsIterator.next();
		assertEquals("Should be usage for this order", 1, appliedCoupon1.getUsageCount());

		AppliedCoupon appliedCoupon2 = appliedCouponsIterator.next();
		assertEquals("Should be usage for the other coupon", 1, appliedCoupon2.getUsageCount());
	}


	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Do this for LimitPerAnyUser coupon.
	 * When there are two coupons for the same action then the one that is not used up
	 * should be used. If the usage is more than is left on the first coupon then should
	 * be split across to the next coupon. Test where there is no usage left on the second (and subsequent) coupons.
	 */
	@Test(expected = EpServiceException.class)
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerAnyUserTwoCouponsOneActionsSplitUseAcrossCouponsNoUsageLeft() {

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(THREE); // Set to the one less than the limit below

		final CouponUsage couponUsage1 = new CouponUsageImpl();
		couponUsage1.setUseCount(THREE); // Set to the one less than the limit below

		// note that there are only two coupons now available.

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);
		couponUsage.setCoupon(coupon);

		Coupon coupon2 = new CouponImpl();
		coupon2.setCouponCode("uvw");
		coupon2.setCouponConfig(couponConfig);
		coupons.add(coupon2);
		couponUsage1.setCoupon(coupon2);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 3);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			atLeast(2).of(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			atLeast(2).of(dao).findByCouponCodeAndEmail("uvw", "test@abc.com"); will(returnValue(couponUsage1));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
			//allowing(customerSession).getEmail(); will(returnValue("test@abc.com"));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		// we expect an exception - see annotation
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon. Do this for LimitPerSpecifiedUser coupon which hasn't been used yet.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerSpecifiedUserUseCount0() {
		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(0);

		final Order inputOrder = populateInputOrder();

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);
		rule.addCondition(condition);

		final CartNFreeSkusActionImpl action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);
		couponConfig.setUsageLimit(FOUR);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			oneOf(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes(ABC, promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
			//allowing(customerSession).getEmail(); will(returnValue("test@abc.com"));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("Should now be 1", 1, updatedCouponUsage.getUseCount());

		AppliedRule actualAppliedRule = inputOrder.getAppliedRules().iterator().next();
		AppliedCoupon appliedCoupon = actualAppliedRule.getAppliedCoupons().iterator().next();
		assertEquals("Should be usage for this order", 1, appliedCoupon.getUsageCount());
	}

	/**
	 * Tests that rules where the coupon code is not user specific are valid promo codes.
	 */
	@Test
	public void testIsValidPromoCodeNotUserSpecific() {
		CouponUsageServiceImpl service = getTestCouponUsageService();

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);
		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(COUPON_CODE);
		coupon.setCouponConfig(couponConfig);
		context.checking(new Expectations() { {
			allowing(dao).findByCode(COUPON_CODE);
			will(returnValue(Collections.EMPTY_LIST));
		} });

		assertEquals(CouponUsageValidationResultEnum.SUCCESS,
				service.validateCouponUsage("", coupon, null));
	}

	/**
	 * Tests that rules where the coupon code is user specific and the coupon usage does not exist
	 *  are valid promo codes.
	 */
	@Test
	public void testIsValidPromoCodeLimitPerAnyUserNoCouponUsage() {
		CouponUsageServiceImpl service = getTestCouponUsageService();


		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(COUPON_CODE);
		coupon.setCouponConfig(couponConfig);
		context.checking(new Expectations() { {
			allowing(dao).findByCouponCodeAndEmail(COUPON_CODE, USER_TEST_EMAIL);
			will(returnValue(null));
		} });

		assertEquals(CouponUsageValidationResultEnum.SUCCESS,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, null));
	}

	/**
	 * Tests that rules where the coupon code is user specific and the coupon usage is under the limit
	 *  are valid promo codes.
	 */
	@Test
	public void testIsValidPromoCodeLimitPerAnyUserCouponUsageUnderLimit() {
		CouponUsageServiceImpl service = getTestCouponUsageService();

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(2);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(1);
		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(COUPON_CODE);
		coupon.setCouponConfig(couponConfig);
		couponUsage.setCoupon(coupon);

		context.checking(new Expectations() { {
			allowing(dao).findByCouponCodeAndEmail(COUPON_CODE, USER_TEST_EMAIL);
			will(returnValue(couponUsage));
		} });

		assertEquals(CouponUsageValidationResultEnum.SUCCESS,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, couponUsage));
	}

	/**
	 * Tests that rules where the coupon code is user specific and the coupon usage
	 * is om the limit are not valid promo codes.
	 */
	@Test
	public void testIsValidPromoCodeLimitPerAnyUserCouponUsageOnLimit() {

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(2);
		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(COUPON_CODE);
		coupon.setCouponConfig(couponConfig);
		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(2);
		couponUsage.setCoupon(coupon);

		context.checking(new Expectations() { {
			allowing(dao).findByCouponCodeAndEmail(COUPON_CODE, USER_TEST_EMAIL);
			will(returnValue(couponUsage));
		} });

		assertEquals(CouponUsageValidationResultEnum.ERROR_USE_COUNT_EXCEEDED,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, couponUsage));
	}

	/**
	 * Tests that rules where the coupon code is not user specific and the coupon usage is under the limit
	 *  are valid promo codes.
	 */
	@Test
	public void testIsValidPromoCodeLimitPerCouponUnderLimit() {
		CouponUsageServiceImpl service = getTestCouponUsageService();

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);
		couponConfig.setUsageLimit(2);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(1);

		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(COUPON_CODE);
		coupon.setCouponConfig(couponConfig);
		couponUsage.setCoupon(coupon);

		context.checking(new Expectations() { {
			allowing(dao).findByCode(COUPON_CODE);
			will(returnValue(Arrays.asList(couponUsage)));
		} });

		assertEquals(CouponUsageValidationResultEnum.SUCCESS,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, null));
	}

	/**
	 * Tests that rules where the coupon code is not user specific and the coupon usage
	 * is om the limit are not valid promo codes.
	 */
	@Test
	public void testIsValidPromoCodeCouponUsageOnLimit() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);
		couponConfig.setUsageLimit(2);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(2);
		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(COUPON_CODE);
		coupon.setCouponConfig(couponConfig);
		couponUsage.setCoupon(coupon);

		context.checking(new Expectations() { {
			allowing(dao).findByCode(COUPON_CODE);
			will(returnValue(Arrays.asList(couponUsage)));
		} });

		assertEquals(CouponUsageValidationResultEnum.ERROR_USE_COUNT_EXCEEDED,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, null));
	}

	/**
	 * Tests that when the coupon is limited duration and there is no coupon usage
	 * that the code is valid.
	 */
	@Test
	public void testIsValidPromoCodeLimitedDurationNoCouponUsage() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(1);
		couponConfig.setLimitedDuration(true);
		couponConfig.setDurationDays(SEVEN);
		final Coupon coupon = new CouponImpl();
		coupon.setCouponConfig(couponConfig);
		coupon.setCouponCode(COUPON_CODE);

		context.checking(new Expectations() { {
			allowing(dao).findByCouponCodeAndEmail(COUPON_CODE, USER_TEST_EMAIL);
			will(returnValue(null));
		} });

		assertEquals(CouponUsageValidationResultEnum.SUCCESS,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, null));
	}

	/**
	 * Tests that when the coupon is limited duration and the current time is within the interval
	 * that the code is valid.
	 */
	@Test
	public void testIsValidPromoCodeLimitedDurationWithinInterval() {
		CouponUsageServiceImpl service = getTestCouponUsageService();

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(1);
		couponConfig.setLimitedDuration(true);
		couponConfig.setDurationDays(SEVEN);

		final Coupon coupon = new CouponImpl();
		coupon.setCouponConfig(couponConfig);
		coupon.setCouponCode(COUPON_CODE);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setCoupon(coupon);

		Calendar calendar = Calendar.getInstance();
		calendar.set(TWENTYTEN, 0, EIGHT, TWENTY_THREE, FIFTY_NINE, FIFTY_NINE);
		Date limitedDurationStartDate = calendar.getTime();
		couponUsage.setLimitedDurationStartDate(limitedDurationStartDate);

		calendar.add(Calendar.DATE, SEVEN);
		Date expectedEndDate = calendar.getTime();

		assertEquals("The expected end date should be 7 days from the start date", expectedEndDate, couponUsage.getLimitedDurationEndDate());

		calendar.set(TWENTYTEN, 0, FOUR, EIGHT, FORTY_NINE);
		final Date withinDate = calendar.getTime();
		final TimeService testTimeService = context.mock(TimeService.class, "testSpecificTimeService");
		service.setTimeService(testTimeService);
		context.checking(new Expectations() { {
			allowing(testTimeService).getCurrentTime(); will(returnValue(withinDate));
		} });

		service.setTimeService(testTimeService);

		context.checking(new Expectations() { {
			allowing(dao).findByCouponCodeAndEmail(COUPON_CODE, USER_TEST_EMAIL);
			will(returnValue(couponUsage));
		} });

		assertEquals(CouponUsageValidationResultEnum.SUCCESS,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, null));
	}

	private CouponUsageServiceImpl getTestCouponUsageService() {
		return service;
	}

	/**
	 * Tests that when the coupon is limited duration and the current time is after the interval
	 * that the code is not valid.
	 */
	@Test
	public void testIsValidPromoCodeLimitedDurationAfterInterval() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);
		couponConfig.setUsageLimit(1);
		couponConfig.setLimitedDuration(true);
		couponConfig.setDurationDays(SEVEN);

		final Coupon coupon = new CouponImpl();
		coupon.setCouponConfig(couponConfig);
		coupon.setCouponCode(COUPON_CODE);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setCoupon(coupon);

		Calendar calendar = Calendar.getInstance();
		calendar.set(TWENTYTEN, 0, EIGHT, TWENTY_THREE, FIFTY_NINE, FIFTY_NINE);
		Date limitedDurationStartDate = calendar.getTime();
		couponUsage.setLimitedDurationStartDate(limitedDurationStartDate);

		calendar.add(Calendar.DATE, SEVEN);
		Date expectedEndDate = calendar.getTime();
		assertEquals("The expected end date should be 7 days from the start date", expectedEndDate, couponUsage.getLimitedDurationEndDate());
		calendar.set(TWENTYTEN, 0, TWENTY_THREE, EIGHT, FORTY_NINE);
		final Date afterDate = calendar.getTime();

		final TimeService testTimeService = context.mock(TimeService.class, "testSpecificTimeService");
		service.setTimeService(testTimeService);
		context.checking(new Expectations() { {
			allowing(testTimeService).getCurrentTime(); will(returnValue(afterDate));
		} });

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();
		final Rule rule = new PromotionRuleImpl();
		rule.setCode(XYZ);
		rule.addCondition(condition);

		context.checking(new Expectations() { {
			allowing(dao).findByCouponCodeAndEmail(COUPON_CODE, USER_TEST_EMAIL);
			will(returnValue(couponUsage));
		} });

		assertEquals(CouponUsageValidationResultEnum.ERROR_EXPIRED,
				service.validateCouponUsage(USER_TEST_EMAIL, coupon, null));
	}

	/**
	 * Use count should be 1 unless we have a rule giving free skus.
	 */
	@Test
	public void testCalculateUseCountForNonFreeRuleActions() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);

		final Coupon appliedCoupon = new CouponImpl();
		appliedCoupon.setCouponCode(XYZ);
		appliedCoupon.setCouponConfig(couponConfig);

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();
		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode("use");
		rule.addCondition(condition);

		final RuleAction action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(null, 0L, 0L, null, 1);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		int useCount = service.calculateUseCount(rule, shoppingCart, pricingSnapshot);
		assertEquals("The use count should be 1 for rules without a NFreeSkus action", 1, useCount);
	}

	/**
	 * Use count should be quantity in cart if use limit is greater.
	 */
	@Test
	public void testCalculateUseCountForRuleWithFreeActionUnderLimit() {
		final int usageLimit = 20;
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(usageLimit);

		final Coupon appliedCoupon = new CouponImpl();
		appliedCoupon.setCouponCode(XYZ);
		appliedCoupon.setCouponConfig(couponConfig);

		RuleParameter skuParam = new RuleParameterImpl();
		skuParam.setKey(RuleParameter.SKU_CODE_KEY);
		skuParam.setValue("FREESKU");

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");

		final RuleAction action = new CartNFreeSkusActionImpl();
		action.addParameter(skuParam);
		action.addParameter(numSkusParam);

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode("use");
		rule.addAction(action);
		rule.addCondition(condition);

		final ShoppingItem freeitem = context.mock(ShoppingItem.class);
		final int freeSkuQuantity = 10;

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(freeitem, 0L, 0L, null, freeSkuQuantity);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		int useCount = service.calculateUseCount(rule, shoppingCart, pricingSnapshot);
		assertEquals("The use count should be the quantity of the free sku in the cart", freeSkuQuantity, useCount);
	}

	/**
	 * Use count should be minimum of quantity in cart and usage limit.
	 */
	@Test
	public void testCalculateUseCountForRuleWithFreeActionOverLimit() {
		final int usageLimit = 20;
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(usageLimit);

		final Coupon appliedCoupon = new CouponImpl();
		appliedCoupon.setCouponCode(XYZ);
		appliedCoupon.setCouponConfig(couponConfig);

		RuleParameter skuParam = new RuleParameterImpl();
		skuParam.setKey(RuleParameter.SKU_CODE_KEY);
		skuParam.setValue("FREESKU");

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");

		final RuleAction action = new CartNFreeSkusActionImpl();
		action.addParameter(skuParam);
		action.addParameter(numSkusParam);

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode("use");
		rule.addAction(action);
		rule.addCondition(condition);

		final ShoppingItem freeitem = context.mock(ShoppingItem.class);
		final int skuQuantity = 30;

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(freeitem, 0L, 0L, null, usageLimit);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		int useCount = service.calculateUseCount(rule, shoppingCart, pricingSnapshot);
		assertEquals("The use count should be the quantity of the free sku in the cart", Math.min(skuQuantity, usageLimit), useCount);
	}

	/**
	 * When there are two actions in the cart with different availableDiscountQuantities
	 * then the use count is the number sufficient to cover the use.
	 */
	@Test
	public void testCalculateUseCountWithTwoActions() {
		final int usageLimit = 20;
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(usageLimit);

		final Coupon appliedCoupon = new CouponImpl();
		appliedCoupon.setCouponCode(XYZ);
		appliedCoupon.setCouponConfig(couponConfig);

		RuleParameter discountAmountParam = new RuleParameterImpl();
		discountAmountParam.setKey(RuleParameter.DISCOUNT_AMOUNT_KEY);
		discountAmountParam.setValue("10");

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("2");

		final RuleAction action = new CartAnySkuPercentDiscountActionImpl();
		action.addParameter(discountAmountParam);
		action.addParameter(numSkusParam);

		RuleParameter discountPercentParam = new RuleParameterImpl();
		discountPercentParam.setKey(RuleParameter.DISCOUNT_PERCENT_KEY);
		discountPercentParam.setValue("10");

		RuleParameter numSkusParam2 = new RuleParameterImpl();
		numSkusParam2.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam2.setValue("2");

		final RuleAction action2 = new CartAnySkuAmountDiscountActionImpl();
		action2.addParameter(discountPercentParam);
		action2.addParameter(numSkusParam2);

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode("use");
		rule.addAction(action);
		rule.addAction(action2);
		rule.addCondition(condition);

		final ShoppingItem freeitem = context.mock(ShoppingItem.class);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(freeitem, 0L, 0L, null, 1);

		final DiscountRecord discountRecord2 = new ItemDiscountRecordImpl(freeitem, 0L, 0L, null, 4);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action2, discountRecord2);

		int useCount = service.calculateUseCount(rule, shoppingCart, pricingSnapshot);
		assertEquals("Two uses of action2 are required to cover the four items in the cart", 2, useCount);
	}

	/**
	 * Use count should be 1 when there is no discount record.
	 */
	@Test
	public void testCalculateUseCountWhenNoDiscountRecord() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);

		final Coupon appliedCoupon = new CouponImpl();
		appliedCoupon.setCouponCode(XYZ);
		appliedCoupon.setCouponConfig(couponConfig);

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();
		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode("use");
		rule.addCondition(condition);

		final RuleAction action = new CartNFreeSkusActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, null);

		int useCount = service.calculateUseCount(rule, shoppingCart, pricingSnapshot);
		assertEquals("The use count should be 1 when there is no discount record", 1, useCount);
	}

	/**
	 * Use count should be 0 if the discount is superceded.
	 */
	@Test
	public void testCalculateUseCountDiscountSuperceded() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);

		final Coupon appliedCoupon = new CouponImpl();
		appliedCoupon.setCouponCode(XYZ);
		appliedCoupon.setCouponConfig(couponConfig);

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();
		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode("use");
		rule.addCondition(condition);

		final RuleAction action = new ShippingAmountDiscountActionImpl();
		rule.addAction(action);

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");
		action.addParameter(numSkusParam);

		final String shippingLevelCode = "SHIPCODE001";
		final ShippingDiscountRecordImpl discountRecord = new ShippingDiscountRecordImpl(shippingLevelCode, 0L, 0L, null);
		discountRecord.setSuperceded(true);
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, discountRecord);

		int useCount = service.calculateUseCount(rule, shoppingCart, pricingSnapshot);
		assertEquals("The use count should be 0 for actions which were superceded", 0, useCount);
	}

	/**
	 * Test that the updateLimitedUsageCouponCurrentNumbers method updates the
	 * usecount for the coupon when we have an NFreeSkus action.
	 */
	@Test
	public void testUpdateLimitedUsageCouponCurrentNumbersLimitPerAnyUserForNFreeSkus() {
		final int alreadyUsed = 10;
		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(alreadyUsed);
		couponUsage.setCustomerEmailAddress("test@abc.com");

		final Order inputOrder = populateInputOrder();

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final int usageLimit = 20;
		CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(usageLimit);

		final List<Coupon> coupons = new ArrayList<>();
		Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);
		coupon.setCouponConfig(couponConfig);
		coupons.add(coupon);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(XYZ);

		RuleParameter skuParam = new RuleParameterImpl();
		skuParam.setKey(RuleParameter.SKU_CODE_KEY);
		skuParam.setValue("FREESKU");

		RuleParameter numSkusParam = new RuleParameterImpl();
		numSkusParam.setKey(RuleParameter.NUM_ITEMS_KEY);
		numSkusParam.setValue("1");

		final RuleAction action = new CartNFreeSkusActionImpl();
		action.addParameter(skuParam);
		action.addParameter(numSkusParam);

		RuleCondition condition = new LimitedUseCouponCodeConditionImpl();

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode("use");
		rule.addAction(action);
		rule.addCondition(condition);

		final ShoppingItem freeitem = context.mock(ShoppingItem.class);

		final DiscountRecord itemDiscountRecord = new ItemDiscountRecordImpl(freeitem, 0L, 0L, null, usageLimit - alreadyUsed);
		setCustomerWithEmailOnShopper(shoppingCart.getShopper(), "test@abc.com");
		givenDiscountRecordContainerContainsRecordForRuleAndAction(rule, action, itemDiscountRecord);

		context.checking(new Expectations() { {
			oneOf(dao).findByCouponCodeAndEmail(XYZ, "test@abc.com"); will(returnValue(couponUsage));
			oneOf(ruleService).findByUids(Collections.singleton(RULE_UID));
			will(returnValue(Collections.singletonList(rule)));
			oneOf(couponService).findCouponsForRuleCodeFromCouponCodes("use", promotionCodes); will(returnValue(coupons));
			allowing(shoppingCart).getPromotionCodes(); will(returnValue(promotionCodes));
		} });

		service.updateLimitedUsageCouponCurrentNumbers(shoppingCart, pricingSnapshot, inputOrder.getAppliedRules());

		CouponUsage updatedCouponUsage = service.getUpdatedCouponUsage();

		assertEquals("Should be the usage limit", usageLimit, updatedCouponUsage.getUseCount());
	}

	/**
	 * Test that, with a rule fired, that the CouponAssignmentAction creates
	 * the coupons.
	 */
	@Test
	public void testProcessCouponCustomerAssignmentsCorrectAction() {

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);

		final CouponConfigService couponConfigService = context.mock(CouponConfigService.class);
		service.setCouponConfigService(couponConfigService);

		CouponCodeGeneratorImpl codeGenerator = new CouponCodeGeneratorImpl();
		codeGenerator.setGeneratedCouponCode1(GENERATED_CODE);
		CouponServiceImplDouble couponService = new CouponServiceImplDouble();
		couponService.setCouponCodeGenerator(codeGenerator);
		service.setCouponService(couponService);

		final Coupon unpopulatedCoupon = new CouponImpl();
		unpopulatedCoupon.setCouponConfig(couponConfig);

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);

		CouponAssignmentActionImpl couponAssignmentActionImpl = new CouponAssignmentActionImpl();
		couponAssignmentActionImpl.addParameter(new RuleParameterImpl(RuleParameter.RULE_CODE_KEY, XYZ));
		couponAssignmentActionImpl.addParameter(new RuleParameterImpl(RuleParameter.COUPON_PREFIX, PREFIX));
		rule.addAction(couponAssignmentActionImpl);

		context.checking(new Expectations() { {
			allowing(ruleService).get(RULE_UID); will(returnValue(rule));
			oneOf(couponConfigService).findByRuleCode(XYZ); will(returnValue(couponConfig));
			oneOf(beanFactory).getPrototypeBean(ContextIdNames.COUPON, Coupon.class);
			will(returnValue(unpopulatedCoupon));
		} });

		Set<Long> appliedRuleUids = new HashSet<>();
		appliedRuleUids.add(RULE_UID);

		service.processCouponCustomerAssignments(appliedRuleUids, "test@user.com");

		CouponUsage couponUsage = service.getUpdatedCouponUsage();
		assertNotNull("A coupon should have been added.", couponUsage);
		assertEquals("test@user.com", couponUsage.getCustomerEmailAddress());
		assertEquals("Use count should be zero as the coupon isn't used yet", 0, couponUsage.getUseCount());
		assertTrue("By default, these coupons should be active in cart", couponUsage.isAutoApplyToCarts());
		assertEquals("Generated code should equal GENERATED_CODE", GENERATED_CODE, couponUsage.getCoupon().getCouponCode());
		assertEquals(unpopulatedCoupon, couponUsage.getCoupon());

	}

	/**
	 * Test that, with a rule fired, that the CouponAssignmentAction creates
	 * the coupons for two CouponAssignmentActions.
	 */
	@Test
	public void testProcessCouponCustomerAssignmentsTwoActions() {

		final CouponConfig couponConfig1 = new CouponConfigImpl();
		couponConfig1.setUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);

		final CouponConfig couponConfig2 = new CouponConfigImpl();
		couponConfig2.setUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);

		final CouponConfigService couponConfigService = context.mock(CouponConfigService.class);
		service.setCouponConfigService(couponConfigService);

		CouponCodeGeneratorImpl codeGenerator = new CouponCodeGeneratorImpl();
		codeGenerator.setGeneratedCouponCode1(GENERATED_CODE);
		codeGenerator.setGeneratedCouponCode2(GENERATED_CODE2);

		CouponServiceImplDouble couponService = new CouponServiceImplDouble();
		couponService.setCouponCodeGenerator(codeGenerator);
		service.setCouponService(couponService);

		final Coupon unpopulatedCoupon1 = new CouponImpl();
		unpopulatedCoupon1.setCouponConfig(couponConfig1);

		final Coupon unpopulatedCoupon2 = new CouponImpl();
		unpopulatedCoupon2.setCouponConfig(couponConfig2);

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);

		CouponAssignmentActionImpl couponAssignmentActionImpl = new CouponAssignmentActionImpl();
		couponAssignmentActionImpl.addParameter(new RuleParameterImpl(RuleParameter.RULE_CODE_KEY, XYZ));
		couponAssignmentActionImpl.addParameter(new RuleParameterImpl(RuleParameter.COUPON_PREFIX, PREFIX));
		rule.addAction(couponAssignmentActionImpl);

		CouponAssignmentActionImpl couponAssignmentActionImpl2 = new CouponAssignmentActionImpl();
		couponAssignmentActionImpl2.addParameter(new RuleParameterImpl(RuleParameter.RULE_CODE_KEY, "uvw"));
		couponAssignmentActionImpl.addParameter(new RuleParameterImpl(RuleParameter.COUPON_PREFIX, PREFIX2));
		rule.addAction(couponAssignmentActionImpl2);

		context.checking(new Expectations() { {
			allowing(ruleService).get(RULE_UID); will(returnValue(rule));
			oneOf(couponConfigService).findByRuleCode(XYZ); will(returnValue(couponConfig1));
			oneOf(beanFactory).getPrototypeBean(ContextIdNames.COUPON, Coupon.class);
			will(returnValue(unpopulatedCoupon1));
			oneOf(couponConfigService).findByRuleCode("uvw"); will(returnValue(couponConfig2));
			oneOf(beanFactory).getPrototypeBean(ContextIdNames.COUPON, Coupon.class);
			will(returnValue(unpopulatedCoupon2));
		} });

		Set<Long> appliedRuleUids = new HashSet<>();
		appliedRuleUids.add(RULE_UID);

		service.processCouponCustomerAssignments(appliedRuleUids, "test@user.com");

		//This will get the second one updated - which is fine as other tests test the first one.
		CouponUsage couponUsage = service.getUpdatedCouponUsage();
		assertNotNull("A coupon should have been added.", couponUsage);
		assertEquals("test@user.com", couponUsage.getCustomerEmailAddress());
		assertEquals("Use count should be zero as the coupon isn't used yet", 0, couponUsage.getUseCount());
		assertTrue("By default, these coupons should be active in cart", couponUsage.isAutoApplyToCarts());
		assertEquals("Generated code should equal GENERATED_CODE2", GENERATED_CODE2, couponUsage.getCoupon().getCouponCode());
		assertEquals(unpopulatedCoupon2, couponUsage.getCoupon());
	}

	/**
	 * Test that, when a CouponAssignmentAction is not present, that assignCouponsToCustomers does nothing.
	 */
	@Test
	public void testProcessCouponCustomerAssignmentsIncorrectAction() {

		final CouponService couponService = context.mock(CouponService.class);
		service.setCouponService(couponService);

		final Rule rule = getTestPromoRule(RULE_UID);
		rule.setCode(ABC);

		CartAnySkuAmountDiscountActionImpl cartAnySkuAmountDiscountActionImpl = new CartAnySkuAmountDiscountActionImpl();
		rule.addAction(cartAnySkuAmountDiscountActionImpl);

		context.checking(new Expectations() { {
			allowing(ruleService).get(RULE_UID); will(returnValue(rule));
		} });

		Set<Long> appliedRuleUids = new HashSet<>();
		appliedRuleUids.add(RULE_UID);

		service.processCouponCustomerAssignments(appliedRuleUids, "test@user.com");

		CouponUsage couponUsage = service.getUpdatedCouponUsage();
		assertNull("A coupon should not have been added.", couponUsage);

	}

	/**
	 * Test {@link com.elasticpath.service.rules.impl.CouponUsageServiceImpl#findAllUsagesByEmailAddress}.
	 */
	@Test
	public void shouldAddCouponUsageWhenSearchingAllUsagesByEmailAddressAndAllowedLimitIsNull() {

		final Long allowedLimit = null;

		final Collection<CouponUsage> expectedCouponUsages = findEligibleUsagesByEmailAddressInStore(allowedLimit);

		assertEquals("There must be one coupon usage in collection", 1, expectedCouponUsages.size());
	}

	/**
	 * Test {@link com.elasticpath.service.rules.impl.CouponUsageServiceImpl#findAllUsagesByEmailAddress}.
	 */
	@Test
	public void shouldAddCouponUsageWhenSearchingAllUsagesByEmailAddressAndAllowedLimitIsZero() {

		final Long allowedLimit = 0L;

		final Collection<CouponUsage> expectedCouponUsages = findEligibleUsagesByEmailAddressInStore(allowedLimit);

		assertEquals("There must be one coupon usage in collection", 1, expectedCouponUsages.size());
	}

	/**
	 * Test {@link com.elasticpath.service.rules.impl.CouponUsageServiceImpl#findAllUsagesByEmailAddress}.
	 */
	@Test
	public void shouldAddCouponUsageWhenSearchingAllUsagesByEmailAddressAndAllowedLimitGreaterThanRuleCurrentLupNumber() {

		final Long allowedLimit = 3L;

		final Collection<CouponUsage> expectedCouponUsages = findEligibleUsagesByEmailAddressInStore(allowedLimit);

		assertEquals("There must be one coupon usage in collection", 1, expectedCouponUsages.size());
	}

	/**
	 * Test {@link com.elasticpath.service.rules.impl.CouponUsageServiceImpl#findAllUsagesByEmailAddress}.
	 */
	@Test
	public void shouldNotAddCouponUsageWhenSearchingAllUsagesByEmailAddressAndAllowedLimitIsLessThanRuleCurrentLupNumber() {

		final Long allowedLimit = 1L;

		final Collection<CouponUsage> expectedCouponUsages = findEligibleUsagesByEmailAddressInStore(allowedLimit);

		assertTrue("The collection must be empty", expectedCouponUsages.isEmpty());
	}

	/**
	 * Test if coupon usage is valid, for a given coupon usage.
	 * Coupon usage should never be fetched from db if already provided.
	 */
	@Test
	public void shouldReturnFalseWhenCouponUsageIsProvidedAndInvalid() {

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(2);

		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(COUPON_CODE);
		coupon.setCouponConfig(couponConfig);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setUseCount(2);
		couponUsage.setCoupon(coupon);

		context.checking(new Expectations() { {
			never(dao).findByCouponCodeAndEmail(COUPON_CODE, USER_TEST_EMAIL);
		} });

		assertFalse(service.validateCouponUsage(USER_TEST_EMAIL, coupon, couponUsage).isSuccess());
	}

	@Test
	public void testThatDisableAutoApplyToCartsUpdatesAutoApplyFlag() {
		context.checking(new Expectations() { {
			oneOf(dao).disableAutoApplyToCarts(COUPON_CODE, USER_TEST_EMAIL);
		} });

		service.disableAutoApplyToCarts(COUPON_CODE, USER_TEST_EMAIL);
	}

	private Collection<CouponUsage> findEligibleUsagesByEmailAddressInStore(final Long allowedLimit) {

		final Long ruleCurrentLupNumber = 2L;

		final Long storeUidPk = 1L;
		final Long ruleUidPk = 1L;

		final Date expirationDate = new Date();
		final Collection<CouponUsage> couponUsages = new ArrayList<>();

		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(XYZ);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setCoupon(coupon);
		couponUsages.add(couponUsage);

		final Rule rule = context.mock(Rule.class);

		context.checking(new Expectations() { {
			oneOf(dao).findEligibleUsagesByEmailAddressInStore(USER_TEST_EMAIL, expirationDate, storeUidPk); will(returnValue(couponUsages));
			oneOf(ruleService).findByPromoCode(XYZ); will(returnValue(rule));
			oneOf(ruleService).getAllowedLimit(ruleUidPk); will(returnValue(allowedLimit));
			oneOf(rule).getUidPk(); will(returnValue(ruleUidPk));
			oneOf(rule).isEnabled(); will(returnValue(true));
			oneOf(rule).isWithinDateRange(); will(returnValue(true));
			allowing(rule).getCurrentLupNumber(); will(returnValue(ruleCurrentLupNumber));
		} });

		return service.findAllUsagesByEmailAddress(USER_TEST_EMAIL, expirationDate, storeUidPk);
	}
}
