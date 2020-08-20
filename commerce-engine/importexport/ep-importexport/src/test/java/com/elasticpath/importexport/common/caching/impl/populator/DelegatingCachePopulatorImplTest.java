/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.importexport.common.caching.CachePopulator;
import com.elasticpath.importexport.common.dto.productcategory.ProductCategoriesDTO;
import com.elasticpath.importexport.common.dto.products.ProductDTO;

/**
 * Tests {@link DelegatingCachePopulatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DelegatingCachePopulatorImplTest {

	private static final Class<ProductDTO> DTO_CLASS = ProductDTO.class;

	private DelegatingCachePopulatorImpl delegatingCachePopulator;

	@Mock
	private CachePopulator<ProductDTO> cachePopulator;

	private ProductDTO productDTO;
	private ProductCategoriesDTO productCategoriesDTO;

	@Before
	public void setUp() {
		delegatingCachePopulator = new DelegatingCachePopulatorImpl();
		delegatingCachePopulator.setCachePopulators(ImmutableMap.of(DTO_CLASS, cachePopulator));

		productDTO = new ProductDTO();
		productCategoriesDTO = new ProductCategoriesDTO();
	}

	@Test
	public void testPopulateForEmptyList() {
		delegatingCachePopulator.populate(Collections.emptyList());
		verify(cachePopulator, never()).populate(anyList());
	}

	@Test
	public void testPopulateDelegatesToSpecificPopulator() {
		delegatingCachePopulator.populate(Collections.singletonList(productDTO));
		verify(cachePopulator).populate(anyList());
	}

	@Test
	public void testPopulateMissingPopulatorIgnored() {
		delegatingCachePopulator.populate(Collections.singletonList(productCategoriesDTO));
		verify(cachePopulator, never()).populate(anyList());
	}
}
