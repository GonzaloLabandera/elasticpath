/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.caching.core.catalog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.service.catalog.ProductLookup;

public class CachingProductLookupImplTest {
	@Mock private Cache<Long, Product> productByUidCache;
	@Mock private Cache<String, Long> productGuidToUidPkCache;
	@Mock private ProductLookup fallback;
	private CachingProductLookupImpl cachingProductLookup;
	private ProductImpl product;
	private ProductImpl product2;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		product = new ProductImpl();
		product.initialize();
		product.setUidPk(1L);
		product.setCode("1");

		product2 = new ProductImpl();
		product2.initialize();
		product2.setUidPk(2L);
		product2.setCode("2");

		cachingProductLookup = new CachingProductLookupImpl();
		cachingProductLookup.setProductByUidCache(productByUidCache);
		cachingProductLookup.setProductUidByGuidCache(productGuidToUidPkCache);
		cachingProductLookup.setFallbackLookup(fallback);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByUidDelegatesToCacheWithFallbackLoader() {
		// Given
		when(productByUidCache.get(eq(product.getUidPk()), any(Function.class))).thenReturn(product);

		// When
		Product found = cachingProductLookup.findByUid(product.getUidPk());

		assertSame("Cached product should have been returned", product, found);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByUidsDelegatesToMultiKeyCacheWithFallbackLoader() {
		// Given
		final List<Long> productUids = Arrays.asList(product.getUidPk(), product2.getUidPk());
		when(productByUidCache.getAll(eq(productUids), any(Function.class))).thenReturn(
				ImmutableMap.of(
						product.getUidPk(), product,
						product2.getUidPk(), product2
				));

		// When
		List<Product> found = cachingProductLookup.findByUids(productUids);

		assertEquals("Cached products should have been returned",
				Arrays.asList(product, product2), found);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByGuidOnCacheHitReturnsCachedProduct() {
		// Given
		when(productGuidToUidPkCache.get(eq(product.getGuid()), any(Function.class))).thenReturn(product.getUidPk());
		when(productByUidCache.get(eq(product.getUidPk()), any(Function.class))).thenReturn(product);

		// When
		Product found = cachingProductLookup.findByGuid(product.getGuid());

		assertSame("Cached product should have been returned", product, found);

		verify(productGuidToUidPkCache).get(eq(product.getGuid()), any(Function.class));
		verify(productByUidCache).get(eq(product.getUidPk()), any(Function.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByGuidOnCacheMissReturnsNull() {
		// when syncing deleted products, product GUID exists but not the product
		when(productGuidToUidPkCache.get(eq(product.getGuid()), any(Function.class))).thenReturn(product.getUidPk());
		when(productByUidCache.get(eq(product.getUidPk()), any(Function.class))).thenReturn(null);

		// When
		Product found = cachingProductLookup.findByGuid(product.getGuid());

		assertNull("Cached product should have been null", found);

		verify(productGuidToUidPkCache).get(eq(product.getGuid()), any(Function.class));
		verify(productByUidCache).get(eq(product.getUidPk()), any(Function.class));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testFindByGuidsDelegatesToMultiKeyCacheWithFallbackLoader() {
		final List<String> productGuids = Arrays.asList(product.getGuid(), product2.getGuid());
		final List<Long> productUids = Arrays.asList(product.getUidPk(), product2.getUidPk());
		when(productGuidToUidPkCache.getAll(eq(productGuids), any(Function.class)))
				.thenReturn(ImmutableMap.of(product.getGuid(), product.getUidPk(), product2.getGuid(), product2.getUidPk()));
		when(productByUidCache.getAll(eq(productUids), any(Function.class)))
				.thenReturn(ImmutableMap.of(product.getUidPk(), product, product2.getUidPk(), product2));

		List<Product> found = cachingProductLookup.findByGuids(productGuids);

		assertEquals("Cached products should have been returned", Arrays.asList(product, product2), found);
	}
}
