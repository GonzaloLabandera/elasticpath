/*
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.caching.core.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Currency;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.pricing.BaseAmount;
import com.elasticpath.domain.pricing.PriceListStack;
import com.elasticpath.domain.pricing.impl.PriceListStackImpl;
import com.elasticpath.service.pricing.datasource.BaseAmountDataSource;

/**
 * Test for the caching Base Amount Finder class.
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingBaseAmountFinderImplTest {

	private static final String SKUCODE = "SKUCODE";
	private static final String PLGUID1 = "PLGUID1";
	private static final String PLGUID2 = "PLGUID2";
	private static final Currency CURRENCY = Currency.getInstance("CAD");

	@Mock
	private Cache<PricingCacheKey, Collection<BaseAmount>> cache;

	@Mock
	private BaseAmountDataSource baseAmountDataSource;

	@Mock
	private ProductSku sku;

	@Mock
	private BaseAmount baseAmount;

	private PriceListStack priceListStack;

	@InjectMocks
	private CachingBaseAmountFinderImpl cachingBaseAmountFinder;

	@Before
	public void setUp() {
		when(sku.getSkuCode()).thenReturn(SKUCODE);
		priceListStack = new PriceListStackImpl();
		priceListStack.setCurrency(CURRENCY);
		priceListStack.addPriceList(PLGUID1);
		priceListStack.addPriceList(PLGUID2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetBaseAmountsLoadsFromCache() {
		// Given
		Collection<BaseAmount> expectedBaseAmounts = ImmutableList.of(baseAmount);
		PricingCacheKey pricingCacheKey = new PricingCacheKey(priceListStack, SKUCODE);
		when(cache.get(eq(pricingCacheKey), any(Function.class))).thenReturn(expectedBaseAmounts);

		// When
		final Collection<BaseAmount> result = cachingBaseAmountFinder.getBaseAmounts(sku, priceListStack, baseAmountDataSource);

		// Then
		assertThat(result).isSameAs(expectedBaseAmounts);
	}

}
