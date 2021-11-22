/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.rules.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.CouponUsage;
import com.elasticpath.domain.rules.CouponUsageType;
import com.elasticpath.domain.rules.impl.CouponConfigImpl;
import com.elasticpath.domain.rules.impl.CouponImpl;
import com.elasticpath.domain.rules.impl.CouponUsageImpl;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.service.rules.CouponConfigService;
import com.elasticpath.service.rules.CouponUsageService;
import com.elasticpath.service.rules.RuleService;
import com.elasticpath.test.factory.TestShopperFactory;

/**
 * Tests {@code PromotionRuleDelegateImpl} without extending elastic path test case.
 */
@RunWith(MockitoJUnitRunner.class)
public class PromotionRuleDelegateImplNewTest {

	private static final int THREE = 3;
	private static final int FIVE = 5;
	private static final String RULE_CODE = "promo5";
	private static final int DISCOUNT_QUANTITY_PER_COUPON = 7;
	private static final int RULE_ID = FIVE;

	@Mock
	private RuleService ruleService;
	@Mock
	private CouponConfigService couponConfigService;
	@Mock
	private CouponUsageService couponUsageService;
	@Mock
	private ShoppingCart shoppingCart;
	@Mock
	private Customer customer;

	@InjectMocks
	private PromotionRuleDelegateImpl delegate;


	/**
	 * Tests that when calculateAvailableDiscountQuantity is called but there
	 * is no coupon config that the NUM_ITEMS parameter is returned.
	 */
	@Test
	public void testCalculateAvailableDiscountQuantityNoCouponConfig() {
		when(ruleService.findRuleCodeById(RULE_ID)).thenReturn(RULE_CODE);
		when(shoppingCart.getShopper()).thenReturn(createShopper());

		final int actualResult = delegate.calculateAvailableDiscountQuantity(shoppingCart, RULE_ID, DISCOUNT_QUANTITY_PER_COUPON);

		assertEquals("The discountQuantityPerCoupon should be returned", DISCOUNT_QUANTITY_PER_COUPON, actualResult);

		verify(ruleService).findRuleCodeById(RULE_ID);
		verify(shoppingCart, times(2)).getShopper();

	}

	private Shopper createShopper() {
		final Shopper shopper = TestShopperFactory.getInstance().createNewShopperWithMemento();
		shopper.setCustomer(new CustomerImpl());
		return shopper;
	}

	/**
	 * Tests that when calculateAvailableDiscountQuantity is called but there
	 * is no coupon usage that the NUM_ITEMS parameter is returned.
	 */
	@Test
	public void testCalculateAvailableDiscountQuantityNoCouponUsage() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);

		when(ruleService.findRuleCodeById(RULE_ID)).thenReturn(RULE_CODE);
		when(shoppingCart.getShopper()).thenReturn(createShopper());

		final int actualResult = delegate.calculateAvailableDiscountQuantity(shoppingCart, RULE_ID, DISCOUNT_QUANTITY_PER_COUPON);

		assertEquals("The discountQuantityPerCoupon should be returned", DISCOUNT_QUANTITY_PER_COUPON, actualResult);

		verify(ruleService).findRuleCodeById(RULE_ID);
		verify(shoppingCart, times(2)).getShopper();
	}

	/**
	 * Tests that when calculateAvailableDiscountQuantity is called with
	 * a usage type of limit per coupon then the parameter is returned.
	 */
	@Test
	public void testCalculateAvailableDiscountQuantityLimitPerCoupon() {
		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_COUPON);

		when(ruleService.findRuleCodeById(RULE_ID)).thenReturn(RULE_CODE);
		when(shoppingCart.getShopper()).thenReturn(createShopper());

		final int actualResult = delegate.calculateAvailableDiscountQuantity(shoppingCart, RULE_ID, DISCOUNT_QUANTITY_PER_COUPON);
		assertEquals("The discountQuantityPerCoupon should be returned", DISCOUNT_QUANTITY_PER_COUPON, actualResult);

		verify(ruleService).findRuleCodeById(RULE_ID);
		verify(shoppingCart, times(2)).getShopper();
	}

	/**
	 * Tests that when calculateAvailableDiscountQuantity is called with
	 * multi use per order set then the remaining coupon uses
	 * are returned.
	 */
	@Test
	public void testCalculateAvailableDiscountQuantityMultiUsePerOrder() {
		final Shopper shopper = createShopper();

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(FIVE);
		couponConfig.setMultiUsePerOrder(true);

		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add("ABC");

		final List<CouponUsage> couponUsageList = new ArrayList<>();

		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode("ABC");
		coupon.setCouponConfig(couponConfig);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setCoupon(coupon);
		couponUsage.setUseCount(2);
		couponUsageList.add(couponUsage);

		shopper.setCustomer(customer);

		when(ruleService.findRuleCodeById(RULE_ID)).thenReturn(RULE_CODE);
		when(couponConfigService.findByRuleCode(RULE_CODE)).thenReturn(couponConfig);
		when(shoppingCart.getShopper()).thenReturn(shopper);
		when(shoppingCart.getPromotionCodes()).thenReturn(promotionCodes);
		when(couponUsageService.findByRuleCodeAndEmail(RULE_CODE, "")).thenReturn(couponUsageList);
		when(customer.getEmail()).thenReturn("");

		final int actualResult = delegate.calculateAvailableDiscountQuantity(shoppingCart, RULE_ID, 2);

		assertEquals("2 * (Coupon Config max - Coupon usage)", 2 * THREE, actualResult);

		verify(ruleService).findRuleCodeById(RULE_ID);
		verify(couponConfigService).findByRuleCode(RULE_CODE);
		verify(shoppingCart, times(2)).getShopper();
		verify(shoppingCart).getPromotionCodes();
		verify(couponUsageService).findByRuleCodeAndEmail(RULE_CODE, "");
		verify(customer).getEmail();
	}

	/**
	 * Tests that when calculateAvailableDiscountQuantity is called with
	 * multi use per order set then the remaining coupon uses
	 * are returned.
	 */
	@Test
	public void testCalculateAvailableDiscountQuantitySingleReservePerPIPerOrder() {

		final CouponConfig couponConfig = new CouponConfigImpl();
		couponConfig.setUsageType(CouponUsageType.LIMIT_PER_ANY_USER);
		couponConfig.setUsageLimit(FIVE);
		couponConfig.setMultiUsePerOrder(false);

		final String promotionCode = "ABC";
		final Set<String> promotionCodes = new HashSet<>();
		promotionCodes.add(promotionCode);

		final List<CouponUsage> couponUsageList = new ArrayList<>();

		final Coupon coupon = new CouponImpl();
		coupon.setCouponCode(promotionCode);
		coupon.setCouponConfig(couponConfig);

		final CouponUsage couponUsage = new CouponUsageImpl();
		couponUsage.setCoupon(coupon);
		couponUsage.setUseCount(2);
		couponUsageList.add(couponUsage);

		when(ruleService.findRuleCodeById(RULE_ID)).thenReturn(RULE_CODE);
		when(shoppingCart.getShopper()).thenReturn(createShopper());

		final int actualResult = delegate.calculateAvailableDiscountQuantity(shoppingCart, RULE_ID, 2);

		assertEquals("1 Coupon usage * parameter", 2, actualResult);

		verify(ruleService).findRuleCodeById(RULE_ID);
		verify(shoppingCart, times(2)).getShopper();
	}
}
