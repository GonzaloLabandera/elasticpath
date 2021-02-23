/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.function.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.catalog.ProductType;

/**
 * Tests {@link CachingProductTypeDaoImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingProductTypeDaoImplTest {

	private static final String NAME = "name";

	@InjectMocks
	private CachingProductTypeDaoImpl cachingDao;

	@Mock
	private Cache<String, ProductType> productTypeByNameCache;
	@Mock
	private ProductType productType;

	@SuppressWarnings("unchecked")
	@Test
	public void testFindProductTypeWithAttributesUsesCache() {
		given(productTypeByNameCache.get(eq(NAME), any(Function.class))).willReturn(productType);
		final ProductType result = cachingDao.findProductTypeWithAttributes(NAME);
		assertThat(result).isEqualTo(productType);
	}
}
