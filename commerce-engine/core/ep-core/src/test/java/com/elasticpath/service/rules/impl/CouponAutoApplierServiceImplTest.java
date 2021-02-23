/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;

import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.CouponUsage;
import com.elasticpath.domain.rules.CouponUsageType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.rules.CouponService;
import com.elasticpath.service.rules.CouponUsageService;

public class CouponAutoApplierServiceImplTest {

	private static final String EMAIL = "EMAIL";

	private static final String COUPON_CODE = "COUPON_CODE";

	private static final String STORECODE = "STORECODE";

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private final CouponService mockCouponService = context.mock(CouponService.class);

	private final CouponUsageService mockCouponUsageService = context.mock(CouponUsageService.class);

	private final Coupon mockCoupon = context.mock(Coupon.class);

	private final CouponConfig mockCouponConfig = context.mock(CouponConfig.class);

	private final CouponUsage mockCouponUsage = context.mock(CouponUsage.class);

	private final CouponAutoApplierServiceImpl applierService = new CouponAutoApplierServiceImpl();

	private final Store mockStore = context.mock(Store.class);

	@Before
	public void setUp() {
		context.checking(new Expectations() {
			{
				allowing(mockCouponService).findByCouponCode(COUPON_CODE);
				will(returnValue(mockCoupon));
				
				allowing(mockCoupon).getCouponCode();
				will(returnValue(COUPON_CODE));

				allowing(mockCoupon).getCouponConfig();
				will(returnValue(mockCouponConfig));

				allowing(mockStore).getCode();
				will(returnValue(STORECODE));

				ignoring(mockStore).getUidPk();
			}
		});

		applierService.setCouponService(mockCouponService);
		applierService.setCouponUsageService(mockCouponUsageService);
	}

	@Test
	public void testFilterKeepsUserSpecificCoupons() {
		allowingCouponToBeUserSpecific(true);
		allowingCouponToValidateAs(CouponUsageValidationResultEnum.SUCCESS, null);

		Set<String> result = applierService.filterValidCouponsForCustomer(new HashSet<>(Arrays.asList(COUPON_CODE)),
				mockStore, EMAIL);

		assertEquals("User specific one should be kept.", 1, result.size());
		assertTrue("User specific one should be kept.", result.contains(COUPON_CODE));
	}

	@Test
	public void testFilterKeepsInvalidCoupons() {
		allowingCouponToBeUserSpecific(false);
		allowingCouponToValidateAs(CouponUsageValidationResultEnum.ERROR_UNSPECIFIED, null);

		Set<String> result = applierService.filterValidCouponsForCustomer(new HashSet<>(Arrays.asList(COUPON_CODE)),
				mockStore, EMAIL);

		assertEquals("Invalid one should be kept.", 1, result.size());
		assertTrue("Invalid one should be kept.", result.contains(COUPON_CODE));
	}

	@Test
	public void testRetrieveCouponsForAutoApplyDoesNotReturnInactiveCoupons() {
		setUpCouponUsageServiceToReturnCouponUsages();
		setUpCouponConfigToHaveUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);
		allowingCouponUsageToBeActiveInCart(false);

		Set<String> result = applierService.retrieveCouponsApplicableToAutoApply(mockStore, EMAIL);

		assertTrue("No inactive coupons should be retrieved.", result.isEmpty());
	}

	@Test
	public void testRetrieveCouponsForAutoApplyDoesNotReturnInvalidCoupons() {
		setUpCouponUsageServiceToReturnCouponUsages();
		setUpCouponConfigToHaveUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);
		allowingCouponUsageToBeActiveInCart(true);
		allowingCouponToValidateAs(CouponUsageValidationResultEnum.ERROR_UNSPECIFIED, mockCouponUsage);

		Set<String> result = applierService.retrieveCouponsApplicableToAutoApply(mockStore, EMAIL);

		assertTrue("No invalid coupons should be retrieved.", result.isEmpty());
	}

	@Test
	public void testRetrieveCouponsForAutoApplyDoesNotReturnCouponsWithUsageTypeOtherThanLimitPerSpecifiedUser() {
		setUpCouponUsageServiceToReturnCouponUsages();
		setUpCouponConfigToHaveUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		allowingCouponUsageToBeActiveInCart(true);
		allowingCouponToValidateAs(CouponUsageValidationResultEnum.SUCCESS, null);

		Set<String> result = applierService.retrieveCouponsApplicableToAutoApply(mockStore, EMAIL);

		assertTrue("No coupons should be retrieved.", result.isEmpty());
	}

	@Test
	public void testRetrieveCouponsForAutoApplyDoesReturnsActiveAndValidCouponsEligibleForCustomer() {
		setUpCouponUsageServiceToReturnCouponUsages();
		setUpCouponConfigToHaveUsageType(CouponUsageType.LIMIT_PER_SPECIFIED_USER);
		allowingCouponUsageToBeActiveInCart(true);
		allowingCouponToValidateAs(CouponUsageValidationResultEnum.SUCCESS, mockCouponUsage);

		Set<String> result = applierService.retrieveCouponsApplicableToAutoApply(mockStore, EMAIL);

		assertEquals("Active and Valid coupons should be retrieved.", 1, result.size());
		assertTrue("Active and Valid coupons should be retrieved.", result.contains(COUPON_CODE));
	}

	private void allowingCouponToBeUserSpecific(final boolean isUserSpecific) {
		context.checking(new Expectations() {
			{
				allowing(mockCouponConfig).getUsageType();
				if (isUserSpecific) {
					will(returnValue(CouponUsageType.LIMIT_PER_SPECIFIED_USER));
				}
			}
		});
	}

	private void allowingCouponUsageToBeActiveInCart(final boolean activeInCart) {
		context.checking(new Expectations() {
			{
				allowing(mockCouponUsage).isAutoApplyToCarts();
				will(returnValue(activeInCart));
			}
		});
	}

	private void allowingCouponToValidateAs(final CouponUsageValidationResultEnum result, final CouponUsage couponUsage) {
		context.checking(new Expectations() {
			{
				allowing(mockCouponUsageService).validateCouponRuleAndUsage(mockCoupon, STORECODE, EMAIL, couponUsage);
				will(returnValue(result));
			}
		});
	}

	private void setUpCouponUsageServiceToReturnCouponUsages() {
		context.checking(new Expectations() {
			{
				Collection<CouponUsage> couponUsages = new ArrayList<>();
				couponUsages.add(mockCouponUsage);

				allowing(mockCouponUsageService).findEligibleUsagesByEmailAddress(with(EMAIL), with(any(Long.class)));
				will(returnValue(couponUsages));

				allowing(mockCouponUsage).getCoupon();
				will(returnValue(mockCoupon));
			}
		});
	}

	private void setUpCouponConfigToHaveUsageType(final CouponUsageType couponUsageType) {
		context.checking(new Expectations() {
			{
				allowing(mockCouponConfig).getUsageType();
				will(returnValue(couponUsageType));
			}
		});
	}
}
