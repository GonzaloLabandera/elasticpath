/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.caching.core.catalog.CachingProductLookupImpl;
import com.elasticpath.importexport.common.caching.CachePopulator;
import com.elasticpath.importexport.common.dto.products.ProductDTO;
import com.elasticpath.importexport.common.dto.products.ProductSkuDTO;

/**
 * Tests {@link ProductCachePopulatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductCachePopulatorImplTest {

	private static final String PRODUCT_CODE1 = "productCode1";
	private static final String PRODUCT_CODE2 = "productCode2";

	@InjectMocks
	private ProductCachePopulatorImpl populator;

	@Mock
	private CachingProductLookupImpl cachingProductLookup;
	@Mock
	private CachePopulator<ProductSkuDTO> productSkuCachePopulator;

	private ProductDTO productDTO1;
	private ProductDTO productDTO2;
	private ProductSkuDTO productSkuDTO1;
	private ProductSkuDTO productSkuDTO2;

	@Before
	public void setUp() {
		productSkuDTO1 = new ProductSkuDTO();
		productSkuDTO2 = new ProductSkuDTO();

		productDTO1 = new ProductDTO();
		productDTO1.setCode(PRODUCT_CODE1);
		productDTO1.setProductSkus(Collections.singletonList(productSkuDTO1));

		productDTO2 = new ProductDTO();
		productDTO2.setCode(PRODUCT_CODE2);
		productDTO2.setProductSkus(Collections.singletonList(productSkuDTO2));
	}

	@Test
	public void testPopulateWithProductsAndSkus() {
		populator.populate(Arrays.asList(productDTO1, productDTO2));

		verify(cachingProductLookup).findByGuids(Arrays.asList(PRODUCT_CODE1, PRODUCT_CODE2));
		verify(productSkuCachePopulator).populate(Arrays.asList(productSkuDTO1, productSkuDTO2));
	}
}
