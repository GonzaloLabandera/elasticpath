/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.catalog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.catalog.Brand;

/**
 * Tests {@link CachingBrandServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingBrandServiceImplTest {

	private static final String CODE = "code";

	@InjectMocks
	private CachingBrandServiceImpl cachingService;

	@Mock
	private Cache<String, Brand> brandByCodeCache;
	@Mock
	private Brand brand;

	@Test
	public void testFindByCodeUsesCache() {
		given(brandByCodeCache.get(CODE, cachingService.getBrandByCodeCacheLoader())).willReturn(brand);
		final Brand result = cachingService.findByCode(CODE);
		assertThat(result).isEqualTo(brand);
	}
}
