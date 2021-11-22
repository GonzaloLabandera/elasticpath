/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.pricing.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl;
import com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl;
import com.elasticpath.domain.subscriptions.PaymentSchedule;
import com.elasticpath.domain.subscriptions.impl.PaymentScheduleImpl;
import com.elasticpath.service.catalog.SkuOptionService;

/**
 * Tests {@link PaymentScheduleHelperImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentScheduleHelperImplTest {

	@Mock
	private SkuOptionService skuOptionService;
	@Mock
	private BeanFactory beanFactory;

	@Mock
	private ProductSku sku;
	@Mock
	private Product product;

	private SkuOption skuOption;
	@Mock
	private SkuOptionValue sov;

	@InjectMocks
	private PaymentScheduleHelperImpl fixture;

	@Before
	public void init() {
		skuOption = mock(SkuOption.class);
		when(skuOptionService.findByKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY)).thenReturn(skuOption);
		fixture.init();
	}
	/**
	 * Test createPaymentSchedule method for empty option value map.
	 */
	@Test
	public void testGetPaymentSchedule1() {
		when(sku.getOptionValueMap()).thenReturn(null);

		assertNull(fixture.getPaymentSchedule(sku));
	}
	
	/**
	 * Test createPaymentSchedule method for null frequency option.
	 */
	@Test
	public void testGetPaymentSchedule2() {
		when(sku.getOptionValueMap()).thenReturn(Collections.emptyMap());
		when(sku.getProduct()).thenReturn(product);
		when(skuOptionService.findByKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY)).thenReturn(null);

		PaymentScheduleHelperImpl fixture = new PaymentScheduleHelperImpl();
		fixture.setSkuOptionService(skuOptionService);
		fixture.init();

		assertNull(fixture.getPaymentSchedule(sku));
	}
	
	/**
	 * Test createPaymentSchedule method for no sku option value.
	 */
	@Test
	public void testGetPaymentSchedule3() {
		when(sku.getOptionValueMap()).thenReturn(Collections.emptyMap());
		when(sku.getProduct()).thenReturn(product);
		when(sku.getSkuOptionValue(skuOption)).thenReturn(null);

		assertNull(fixture.getPaymentSchedule(sku));
		verify(skuOptionService).findByKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY);
	}

	/**
	 * Test createPaymentSchedule method for sku option value equal to NA.
	 */
	@Test
	public void testGetPaymentSchedule4() {
		final Map<String, SkuOptionValue> optionMap = new HashMap<>();
		optionMap.put(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY, sov);

		when(sku.getOptionValueMap()).thenReturn(optionMap);
		when(sku.getProduct()).thenReturn(product);
		when(sku.getSkuOptionValue(skuOption)).thenReturn(sov);
		when(sov.getOptionValueKey()).thenReturn(PaymentScheduleHelperImpl.PAY_NOW_OPTION_VALUE_KEY);

		assertNull(fixture.getPaymentSchedule(sku));
		verify(skuOptionService).findByKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY);
	}
	
	/**
	 * Test createPaymentSchedule method for a "Monthly" option value.
	 */
	@Test
	public void testGetPaymentSchedule5() {
		final Map<String, SkuOptionValue> optionMap = new HashMap<>();
		optionMap.put(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY, sov);
		final String optionValueKey = "Monthly";

		when(sku.getOptionValueMap()).thenReturn(optionMap);
		when(sku.getProduct()).thenReturn(product);
		when(sku.getSkuOptionValue(skuOption)).thenReturn(sov);
		when(sov.getOptionValueKey()).thenReturn(optionValueKey);
		when(sov.getOrdering()).thenReturn(0);
		when(beanFactory.getPrototypeBean(ContextIdNames.PAYMENT_SCHEDULE, PaymentSchedule.class)).thenReturn(new PaymentScheduleImpl());

		PaymentSchedule paymentSchedule = fixture.getPaymentSchedule(sku);
		assertNotNull(paymentSchedule);
		assertSame(paymentSchedule.getName(), optionValueKey);
		assertNotNull(paymentSchedule.getPaymentFrequency());
		assertNull(paymentSchedule.getScheduleDuration());
		assertNotNull(paymentSchedule.getPaymentFrequency().getUnit());
		assertSame(paymentSchedule.getPaymentFrequency().getUnit(), optionValueKey);

		assertEquals(paymentSchedule, fixture.getPaymentSchedule(sku));
		verify(skuOptionService).findByKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY);
		verify(beanFactory, times(2)).getPrototypeBean(ContextIdNames.PAYMENT_SCHEDULE, PaymentSchedule.class);
	}
	
	/**
	 * Test createPaymentSchedule method for non-recurring items.
	 */
	@Test
	public void testCreatePaymentSchedule1() {
		PaymentScheduleHelperImpl fixture = new PaymentScheduleHelperImpl() {
			@Override
			protected boolean isPurchaseTime(final SkuOptionValue skuOptionValue) {
				return true;
			}
		};
		assertNull(fixture.createPaymentSchedule(sov));
	}
	
	/**
	 * Test createPaymentSchedule method for recurring items.
	 */
	@Test
	public void testCreatePaymentSchedule2() {
		PaymentScheduleHelperImpl fixture = new PaymentScheduleHelperImpl() {
			@Override
			protected boolean isPurchaseTime(final SkuOptionValue skuOptionValue) {
				return false;
			}
		};
		when(beanFactory.getPrototypeBean(ContextIdNames.PAYMENT_SCHEDULE, PaymentSchedule.class)).thenReturn(new PaymentScheduleImpl());
		fixture.setBeanFactory(beanFactory);
		fixture.setSkuOptionService(skuOptionService);
	
		SkuOptionValue sov = new SkuOptionValueImpl();
		sov.setOptionValueKey("sov1");
		PaymentSchedule paymentSchedule = fixture.createPaymentSchedule(sov);

		assertNotNull(paymentSchedule);
		assertEquals(sov.getOptionValueKey(), paymentSchedule.getName());
		assertNotNull(paymentSchedule.getPaymentFrequency());
		assertEquals(paymentSchedule.getPaymentFrequency().getAmount(), 1);
		assertEquals(paymentSchedule.getPaymentFrequency().getUnit(), sov.getOptionValueKey());
	
		assertEquals(paymentSchedule, fixture.createPaymentSchedule(sov));
	}

	/**
	 * Test isPurchaseTime method for recurring and non-recurring items.
	 */
	@Test
	public void testIsPurchaseTime() {
		assertTrue(fixture.isPurchaseTime(null));
		
		SkuOptionValue sov = new SkuOptionValueImpl();
		sov.setOptionValueKey("sov1");
		assertFalse(fixture.isPurchaseTime(sov));
		
		sov.setOptionValueKey(PaymentScheduleHelperImpl.PAY_NOW_OPTION_VALUE_KEY);
		assertTrue(fixture.isPurchaseTime(sov));
	}

	/**
	 * Test getFrequencyOption method.
	 */
	@Test
	public void testGetFrequencyOption() {
		SkuOption frequencyOption = fixture.getFrequencyOption(null);
		assertSame(frequencyOption, skuOption);
		assertSame(fixture.getFrequencyOption(product), frequencyOption);
		
	}

	/**
	 * Test isPaymentScheduleCapable for a recurring item.
	 */
	@Test
	public void testIsPaymentScheduleCapable1() {
		final ProductType productType = mock(ProductType.class);
		final SkuOption skuOption = new SkuOptionImpl();
		skuOption.setOptionKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY);

		final Set<SkuOption> soSet = new HashSet<>();
		soSet.add(skuOption);

		when(skuOptionService.findByKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY)).thenReturn(skuOption);
		when(product.getProductType()).thenReturn(productType);
		when(productType.getSkuOptions()).thenReturn(soSet);

		PaymentScheduleHelperImpl fixture = new PaymentScheduleHelperImpl();
		fixture.setSkuOptionService(skuOptionService);
		fixture.init();

		assertTrue(fixture.isPaymentScheduleCapable(product));
	}
	
	/**
	 * Test isPaymentScheduleCapable for a non-recurring item.
	 */
	@Test
	public void testIsPaymentScheduleCapable2() {
		final ProductType productType = mock(ProductType.class);
		final SkuOption skuOption = new SkuOptionImpl();
		skuOption.setOptionKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY);

		final SkuOption skuOption2 = new SkuOptionImpl();
		skuOption2.setOptionKey("Something else");
		final Set<SkuOption> soSet = new HashSet<>();
		soSet.add(skuOption2);

		when(skuOptionService.findByKey(PaymentScheduleHelperImpl.FREQUENCY_OPTION_KEY)).thenReturn(skuOption);
		when(product.getProductType()).thenReturn(productType);
		when(productType.getSkuOptions()).thenReturn(soSet);

		PaymentScheduleHelperImpl fixture = new PaymentScheduleHelperImpl();
		fixture.setSkuOptionService(skuOptionService);
		fixture.init();

		assertFalse(fixture.isPaymentScheduleCapable(product));
	}

}
