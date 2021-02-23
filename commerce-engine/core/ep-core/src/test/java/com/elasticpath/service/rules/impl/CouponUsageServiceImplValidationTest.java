/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.rules.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.apache.commons.lang.StringUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.CouponUsage;
import com.elasticpath.domain.rules.CouponUsageType;
import com.elasticpath.domain.rules.Rule;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.service.rules.dao.CouponUsageDao;

/**
 * Tests for CouponUsageServiceImpl#validateCouponRuleAndUsage.
 */
public class CouponUsageServiceImplValidationTest {

	private static final String COUPON_CODE = "COUPON_CODE";

	@org.junit.Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private final RuleService ruleService = context.mock(RuleService.class);

	private final CouponUsageDao couponUsageDao = context.mock(CouponUsageDao.class);

	private static final String STORE_CODE = "STORE_CODE";

	private static final String EMAIL = "EMAIL";

	private final Coupon coupon = context.mock(Coupon.class);

	private final CouponUsageServiceImpl couponUsageService = new CouponUsageServiceImpl();

	@Before
	public void setUp() {
		couponUsageService.setRuleService(ruleService);
		couponUsageService.setCouponUsageDao(couponUsageDao);
	}

	@Test
	public void testWhenCouponIsNullSpecIsNotSatisfied() {
		boolean result = couponUsageService.validateCouponRuleAndUsage(null, STORE_CODE, EMAIL, null).isSuccess();

		assertFalse("Specification should not be satisfied by null coupon.", result);
	}

	@Test
	public void testWhenCouponCodeIsEmptySpecIsNotSatisfied() {
		setUpCouponCodeForCoupon(StringUtils.EMPTY);

		boolean result = couponUsageService.validateCouponRuleAndUsage(coupon, STORE_CODE, EMAIL, null).isSuccess();

		assertFalse("Specification should not be satisfied by null coupon code.", result);
	}

	@Test
	public void testWhenRuleIsNotValidSpecIsNotSatisfied() {
		setUpCouponCodeForCoupon(COUPON_CODE);
		setUpRuleToBeValid(RuleValidationResultEnum.ERROR_UNSPECIFIED);

		boolean result = couponUsageService.validateCouponRuleAndUsage(coupon, STORE_CODE, EMAIL, null).isSuccess();

		assertFalse("Specification should not be satisfied by invalid rule.", result);
	}

	@Test
	public void testWhenCouponUsageIsInvalidThenSpecIfNotSatisfied() {
		setUpCouponCodeForCoupon(COUPON_CODE);
		setUpRuleToBeValid(RuleValidationResultEnum.SUCCESS);
		setUpCouponUsageToBeValidForRule(false);

		boolean result = couponUsageService.validateCouponRuleAndUsage(coupon, STORE_CODE, EMAIL, null).isSuccess();

		assertFalse("Specification should not be satisfied by invalid coupon usage.", result);
	}

	@Test
	public void testWhenCouponExistsHasValidRuleAndValidCouponUsageSpecIsSatisfied() {
		setUpCouponCodeForCoupon(COUPON_CODE);
		setUpRuleToBeValid(RuleValidationResultEnum.SUCCESS);
		setUpCouponUsageToBeValidForRule(true);

		boolean result = couponUsageService.validateCouponRuleAndUsage(coupon, STORE_CODE, EMAIL, null).isSuccess();

		assertTrue("Specification should be satisfied by valid potential coupon usage.", result);
	}

	private Rule setUpRuleToBeValid(final RuleValidationResultEnum isValid) {
		final Rule rule = context.mock(Rule.class);
		context.checking(new Expectations() {
			{
				allowing(ruleService).findByPromoCode(COUPON_CODE);
				will(returnValue(rule));

				allowing(ruleService).isRuleValid(rule, STORE_CODE);
				will(returnValue(isValid));

				allowing(rule).hasLimitedUseCondition();
				will(returnValue(true));
			}
		});
		return rule;
	}

	private void setUpCouponUsageToBeValidForRule(final boolean isValid) {
		final CouponConfig couponConfig = context.mock(CouponConfig.class);
		final CouponUsage couponUsage = context.mock(CouponUsage.class);

		context.checking(new Expectations() {
			{
				allowing(coupon).getCouponConfig();
				will(returnValue(couponConfig));
				allowing(couponConfig).getUsageType();
				will(returnValue(CouponUsageType.LIMIT_PER_ANY_USER));
				allowing(couponUsageDao).findByCouponCodeAndEmail(COUPON_CODE, EMAIL);
				will(returnValue(couponUsage));
				allowing(couponUsage).getUseCount();
				will(returnValue(0));
				allowing(couponConfig).getUsageLimit();
				will(returnValue(1));
				allowing(coupon).isSuspended();
				will(returnValue(!isValid));
			}
		});
	}

	private void setUpCouponCodeForCoupon(final String couponCode) {
		context.checking(new Expectations() {
			{
				allowing(coupon).getCouponCode();
				will(returnValue(couponCode));
			}
		});
	}
}
