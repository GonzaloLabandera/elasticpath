/*
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.domain.catalogview.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductSkuImpl;

/** Test for StoreProduct. */
@RunWith(MockitoJUnitRunner.class)
public class StoreProductTest {

	private StoreProductImpl storeProduct;
	private static final String DEFAULT_SKU_CODE = "defaultSkuCode";
	private static final String OTHER_SKU_CODE = "otherSkuCode";

	@Mock
	private Product product;

	private final ProductSku defaultProductSku = new ProductSkuImpl();
	private final ProductSku otherProductSku = new ProductSkuImpl();
	private final Map<String, ProductSku> skuMap = new LinkedHashMap<>();

	/** .*/
	@Before
	public void setUp() {
		defaultProductSku.setSkuCode(DEFAULT_SKU_CODE);
		otherProductSku.setSkuCode(OTHER_SKU_CODE);

		when(product.getProductSkus()).thenReturn(skuMap);
		storeProduct = new StoreProductImpl(product);
		storeProduct.setDefaultSku(null);
	}

	/** The default SKU of the product has inventory and is within date range. */
	@Test
	public void testDefaultSkuIsAValidSku() {
		storeProduct.setSkuAvailable(DEFAULT_SKU_CODE, true);
		skuMap.put(DEFAULT_SKU_CODE, defaultProductSku);

		assertThat(storeProduct.getDefaultSku().getSkuCode())
			.as("Returns the code of the default sku")
			.isEqualTo(DEFAULT_SKU_CODE);
	}

	/** The default SKU of the product doesn't have inventory or isn't within date range. but the product is single-sku.*/
	@Test
	public void testDefaultSkuIsNotValidSkuButProductIsSingleSku() {
		storeProduct.setSkuAvailable(DEFAULT_SKU_CODE, false);
		skuMap.put(DEFAULT_SKU_CODE, defaultProductSku);

		when(product.getDefaultSku()).thenReturn(defaultProductSku);

		assertThat(storeProduct.getDefaultSku().getSkuCode())
			.as("Returns the code of the default sku")
			.isEqualTo(DEFAULT_SKU_CODE);

		verify(product).getDefaultSku();
	}

	/** The default SKU of the product doesn't have inventory or isn't within date range, but the product is multi-sku.    */
	@Test
	public void testDefaultSkuIsNotValidSkuButProductIsMultiSku() {
		storeProduct.setSkuAvailable(DEFAULT_SKU_CODE, false);
		storeProduct.setSkuAvailable(OTHER_SKU_CODE, true);
		skuMap.put(DEFAULT_SKU_CODE, defaultProductSku);
		skuMap.put(OTHER_SKU_CODE, otherProductSku);

		assertThat(storeProduct.getDefaultSku().getSkuCode())
			.as("Returns the code of the another sku")
			.isEqualTo(OTHER_SKU_CODE);
	}


	/** Product is multi-sku, but none of it's sku's are valid.*/
	@Test
	public void testNoneOfProductSkusAreValid() {
		storeProduct.setSkuAvailable(DEFAULT_SKU_CODE, false);
		storeProduct.setSkuAvailable(OTHER_SKU_CODE, false);
		skuMap.put(DEFAULT_SKU_CODE, defaultProductSku);
		skuMap.put(OTHER_SKU_CODE, otherProductSku);

		when(product.getDefaultSku()).thenReturn(defaultProductSku);

		assertThat(storeProduct.getDefaultSku().getSkuCode())
			.as("Returns the code of the default sku")
			.isEqualTo(DEFAULT_SKU_CODE);

		verify(product).getDefaultSku();
	}
}
