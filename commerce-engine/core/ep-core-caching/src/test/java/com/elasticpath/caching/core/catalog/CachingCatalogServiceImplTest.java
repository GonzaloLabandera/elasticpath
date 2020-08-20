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
import com.elasticpath.domain.catalog.Catalog;

/**
 * Tests {@link CachingCatalogServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingCatalogServiceImplTest {

	private static final String CODE = "code";

	@InjectMocks
	private CachingCatalogServiceImpl cachingService;

	@Mock
	private Cache<String, Catalog> catalogByCodeCache;
	@Mock
	private Catalog catalog;

	@Test
	public void testFindByCodeUsesCache() {
		given(catalogByCodeCache.get(CODE, cachingService.getCatalogByCodeCacheLoader())).willReturn(catalog);
		final Catalog result = cachingService.findByCode(CODE);
		assertThat(result).isEqualTo(catalog);
	}
}
