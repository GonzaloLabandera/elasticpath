/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.caching.core.pricing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.cache.Cache;
import com.elasticpath.domain.pricing.PriceAdjustment;

@RunWith(MockitoJUnitRunner.class)
public class CachingPriceAdjustmentServiceImplTest {

	private static final String PRICE_LIST_GUID = "plGuid";
	private static final Collection<String> BUNDLE_CONSTITUENTS_IDS	= Arrays.asList("1", "2", "3", "4", "5");

	@Mock
	private Cache<PriceListGuidAndBundleConstituentsKey, Map<String, PriceAdjustment>> cache;
	@Mock
	private PriceAdjustment mockPriceAdjustment;


	@InjectMocks
	private CachingPriceAdjustmentServiceImpl cachingPriceAdjustmentService;

	private static final PriceListGuidAndBundleConstituentsKey CACHE_KEY =
		new PriceListGuidAndBundleConstituentsKey(PRICE_LIST_GUID, BUNDLE_CONSTITUENTS_IDS);

	private final Map<String, PriceAdjustment> result = new HashMap<>();

	@Before
	public void setUp() throws Exception {

		result.put("GuidAndAdjustment", mockPriceAdjustment);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFindByPlGUIDAndBCIDS() throws Exception {
		// Given
		when(cache.get(eq(CACHE_KEY), any(Function.class))).thenReturn(result);

		// When
		Map<String, PriceAdjustment> found = cachingPriceAdjustmentService
			.findByPriceListAndBundleConstituentsAsMap(PRICE_LIST_GUID, BUNDLE_CONSTITUENTS_IDS);

		// Then
		assertThat(result).isSameAs(found);
	}

}
