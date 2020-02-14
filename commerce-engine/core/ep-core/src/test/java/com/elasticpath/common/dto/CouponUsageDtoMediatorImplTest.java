/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.common.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.rules.Coupon;
import com.elasticpath.domain.rules.CouponConfig;
import com.elasticpath.domain.rules.CouponUsage;
import com.elasticpath.domain.rules.impl.CouponConfigImpl;
import com.elasticpath.domain.rules.impl.CouponImpl;
import com.elasticpath.domain.rules.impl.CouponUsageImpl;
import com.elasticpath.service.rules.CouponConfigService;
import com.elasticpath.service.rules.dao.CouponDao;
import com.elasticpath.service.rules.dao.CouponUsageDao;
import com.elasticpath.service.rules.impl.CouponServiceImpl;
import com.elasticpath.service.rules.impl.CouponUsageServiceImpl;

/**
 * Test that the coupon usage dto mediator behaves as expected.
 */
@RunWith(MockitoJUnitRunner.class)
public class CouponUsageDtoMediatorImplTest {
	private static final String COUPON_CODE1 = "CouponCode1";
	private static final String EMAIL_ADDRESS1 = "email.address@blog.com";

	private CouponServiceImplDouble couponService;

	private CouponUsageServiceImplDouble couponUsageService;

	@Mock
	private CouponConfigService couponConfigService;

	private CouponUsageDtoMediatorImpl mediator;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private CouponDao couponDao;

	@Mock
	private CouponUsageDao couponUsageDao;


	/**
	 * Test double for verifying calls.
	 */
	private class CouponUsageServiceImplDouble extends CouponUsageServiceImpl {
		private CouponUsage updatedCouponUsage;

		@Override
		public CouponUsage add(final CouponUsage newCouponUsage) throws EpServiceException {
			this.updatedCouponUsage = newCouponUsage;
			return updatedCouponUsage;
		}

		public CouponUsage getUpdatedCouponUsage() {
			return updatedCouponUsage;
		}
	}

	/**
	 * Test double for verifying calls.
	 */
	private class CouponServiceImplDouble extends CouponServiceImpl {
		private Coupon updatedCoupon;

		@Override
		public Coupon update(final Coupon newCoupon) throws EpServiceException {
			this.updatedCoupon = newCoupon;
			return updatedCoupon;
		}

		@Override
		public Coupon add(final Coupon newCoupon) {
			this.updatedCoupon = newCoupon;
			return updatedCoupon;
		}

		@Override
		public Map<String, Coupon> findCouponsForCodes(final Collection<String> codes) {
			return new HashMap<>();
		}

		public Coupon getUpdatedCoupon() {
			return this.updatedCoupon;
		}

		@Override
		public Coupon findByCouponCode(final String couponCode) {
			return null;
		}

		@Override
		public boolean doesCouponCodeOnlyExistForThisRuleCode(final String couponCode, final String ruleCode) {
			return true;
		}
	}


	/**
	 * Set up required for each test.
	 *
	 * @throws java.lang.Exception in case of error during setup
	 */
	@Before
	public void setUp() throws Exception {
		couponService = new CouponServiceImplDouble();
		couponService.setCouponDao(couponDao);
		couponUsageService = new CouponUsageServiceImplDouble();
		couponUsageService.setCouponUsageDao(couponUsageDao);
		mediator = new CouponUsageDtoMediatorImpl();
		mediator.setCouponConfigService(couponConfigService);
		mediator.setCouponService(couponService);
		mediator.setCouponUsageService(couponUsageService);
		mediator.setBeanFactory(beanFactory);
	}

	/**
	 * Test Adding Coupon given a list of Dtos and an associated CouponPk.
	 */
	@Test
	public void testAddCouponsForDtosAndCouponPk() {
		final String ruleCode = "RULE";
		final CouponConfig persistentCouponConfig = new CouponConfigImpl();
		persistentCouponConfig.setRuleCode(ruleCode);

		final CouponUsage persistentCouponUsage = new CouponUsageImpl();

		final Coupon persistentCoupon = new CouponImpl();
		persistentCoupon.setCouponCode(COUPON_CODE1);
		persistentCoupon.setSuspended(false);
		persistentCoupon.setCouponConfig(persistentCouponConfig);

		Collection<CouponUsageModelDto> addedCouponUsages = new ArrayList<>();
		CouponUsageModelDto dto1 = new CouponUsageModelDto();
		dto1.setCouponCode(COUPON_CODE1);
		dto1.setSuspended(true);
		dto1.setEmailAddress(EMAIL_ADDRESS1);
		addedCouponUsages.add(dto1);

		when(couponConfigService.findByRuleCode(ruleCode)).thenReturn(persistentCouponConfig);
		when(beanFactory.getPrototypeBean(ContextIdNames.COUPON, Coupon.class)).thenReturn(persistentCoupon);
		when(beanFactory.getPrototypeBean(ContextIdNames.COUPON_USAGE, CouponUsage.class)).thenReturn(persistentCouponUsage);

		mediator.add(addedCouponUsages, ruleCode);
		Coupon coupon = couponService.getUpdatedCoupon();
		CouponUsage couponUsage = couponUsageService.getUpdatedCouponUsage();

		verify(couponConfigService).findByRuleCode(ruleCode);
		verify(beanFactory).getPrototypeBean(ContextIdNames.COUPON, Coupon.class);
		verify(beanFactory).getPrototypeBean(ContextIdNames.COUPON_USAGE, CouponUsage.class);

		assertThat(couponUsage.isSuspended()).isTrue();
		assertThat(coupon.isSuspended()).isFalse();

		assertThat(couponUsage.getCoupon().getCouponCode()).isEqualTo(COUPON_CODE1);
		assertThat(couponUsage.getCustomerEmailAddress()).isEqualTo(EMAIL_ADDRESS1);
	}
}
