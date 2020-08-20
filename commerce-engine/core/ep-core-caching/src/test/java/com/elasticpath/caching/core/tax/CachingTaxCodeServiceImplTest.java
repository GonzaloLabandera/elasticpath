/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.caching.core.tax;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.tax.TaxCode;

/**
 * Tests {@link CachingTaxCodeServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CachingTaxCodeServiceImplTest {

	private static final String CODE = "code";

	@InjectMocks
	private CachingTaxCodeServiceImpl cachingService;

	@Mock
	private Cache<String, TaxCode> taxCodeByCodeCache;
	@Mock
	private TaxCode taxCode;

	@Test
	public void testFindByCodeUsesCache() {
		given(taxCodeByCodeCache.get(CODE, cachingService.getTaxCodeByCodeCacheLoader())).willReturn(taxCode);
		final TaxCode result = cachingService.findByCode(CODE);
		assertThat(result).isEqualTo(taxCode);
	}
}
