/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.importexport.common.caching.impl.populator;

import static org.mockito.Mockito.verify;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.importexport.common.dto.products.ProductSkuDTO;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Tests {@link ProductSkuCachePopulatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductSkuCachePopulatorImplTest {

	private static final String SKU_CODE1 = "skuCode1";
	private static final String SKU_GUID1 = "skuGuid1";
	private static final String SKU_CODE2 = "skuCode2";
	private static final String SKU_GUID2 = "skuGuid2";

	@InjectMocks
	private ProductSkuCachePopulatorImpl populator;

	@Mock
	private ProductSkuLookup cachingProductSkuLookup;

	private ProductSkuDTO productSkuDTO1;
	private ProductSkuDTO productSkuDTO2;

	@Before
	public void setUp() {
		productSkuDTO1 = new ProductSkuDTO();
		productSkuDTO1.setSkuCode(SKU_CODE1);
		productSkuDTO1.setGuid(SKU_GUID1);

		productSkuDTO2 = new ProductSkuDTO();
		productSkuDTO2.setSkuCode(SKU_CODE2);
		productSkuDTO2.setGuid(SKU_GUID2);
	}

	@Test
	public void testPopulateWithProductsAndSkus() {
		populator.populate(Arrays.asList(productSkuDTO1, productSkuDTO2));

		verify(cachingProductSkuLookup).findBySkuCodes(Arrays.asList(SKU_CODE1, SKU_CODE2));
		verify(cachingProductSkuLookup).findByGuids(Arrays.asList(SKU_GUID1, SKU_GUID2));
	}
}
