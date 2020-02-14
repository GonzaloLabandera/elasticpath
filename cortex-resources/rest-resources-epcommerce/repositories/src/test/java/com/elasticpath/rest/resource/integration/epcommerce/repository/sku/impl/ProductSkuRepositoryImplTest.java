/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.sku.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.catalog.BundleIdentifier;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.catalogview.StoreProductService;
import com.elasticpath.service.store.StoreService;

@RunWith(MockitoJUnitRunner.class)
public class ProductSkuRepositoryImplTest {
	private static final String NOT_FOUND_MESSAGE = "Could not find item for item ID.";
	private static final String PRODUCT_NOT_FOUND_FOR_SKU = "Product not found for product sku.";
	private static final String SKU_CODE = "SKU123";
	private static final String SKU_GUID = "a845e2e3-b6b1-4bbb-b846-44542ea8f3f0";

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private BundleIdentifier mockBundleIdentifier;

	@Mock
	private StoreService storeService;

	@Mock
	private StoreProductService storeProductService;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	private ProductSkuRepositoryImpl repository;

	@Before
	public void initialize() {
		repository = new ProductSkuRepositoryImpl(productSkuLookup, mockBundleIdentifier, reactiveAdapter, storeService, storeProductService);
	}

	@Test
	public void shouldReturnProductSkuWithAttributesByCodeGivenValidSkuCode() {
		final ProductSku mockProductSku = mock(ProductSku.class);
		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(mockProductSku);

		repository.getProductSkuWithAttributesByCode(SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(mockProductSku);
	}

	@Test
	public void shouldReturnNotFoundWhenGetProductSkuWithAttributesByCodeGivenInvalidSkuCode() {
		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(null);

		repository.getProductSkuWithAttributesByCode(SKU_CODE)
				.test()
				.assertError(ResourceOperationFailure.notFound(NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void testGetProductSkuWithAttributesByGuid() {
		final ProductSku mockProductSku = mock(ProductSku.class);
		when(productSkuLookup.findByGuid(SKU_GUID)).thenReturn(mockProductSku);

		repository.getProductSkuWithAttributesByGuid(SKU_GUID)
				.test()
				.assertNoErrors()
				.assertValue(mockProductSku);
	}

	@Test
	public void testGetProductSkuWithAttributesByGuidNotFound() {
		repository.getProductSkuWithAttributesByGuid(SKU_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound(NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void isProductBundleByGuidReturnNotFoundErrorWhenProductSkuNotFound() {
		when(productSkuLookup.findByGuid(SKU_GUID)).thenReturn(null);

		repository.isProductBundleByGuid(SKU_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound(NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void isProductBundleByGuidReturnNotFoundErrorWhenProductNotFound() {
		ProductSku mockProductSku = mock(ProductSku.class);
		when(productSkuLookup.findByGuid(SKU_GUID)).thenReturn(mockProductSku);

		repository.isProductBundleByGuid(SKU_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound(PRODUCT_NOT_FOUND_FOR_SKU))
				.assertNoValues();
	}

	@Test
	public void isProductBundleByGuidReturnTrueWhenProductSkuIsBundle() {
		ProductSku mockProductSku = mock(ProductSku.class);
		Product product = mock(Product.class);
		when(mockProductSku.getProduct()).thenReturn(product);
		when(productSkuLookup.findByGuid(SKU_GUID)).thenReturn(mockProductSku);
		when(mockBundleIdentifier.isBundle(product)).thenReturn(true);

		repository.isProductBundleByGuid(SKU_GUID)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void isProductBundleByGuidReturnFalseWhenProductSkuIsNotBundle() {
		ProductSku mockProductSku = mock(ProductSku.class);
		Product product = mock(Product.class);
		when(mockProductSku.getProduct()).thenReturn(product);
		when(productSkuLookup.findByGuid(SKU_GUID)).thenReturn(mockProductSku);
		when(mockBundleIdentifier.isBundle(product)).thenReturn(false);

		repository.isProductBundleByGuid(SKU_GUID)
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void isProductBundleByCodeReturnNotFoundErrorWhenProductSkuNotFound() {
		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(null);

		repository.isProductBundleByCode(SKU_CODE)
				.test()
				.assertError(ResourceOperationFailure.notFound(NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void isProductBundleByCodeReturnNotFoundErrorWhenProductNotFound() {
		ProductSku mockProductSku = mock(ProductSku.class);
		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(mockProductSku);

		repository.isProductBundleByCode(SKU_CODE)
				.test()
				.assertError(ResourceOperationFailure.notFound(PRODUCT_NOT_FOUND_FOR_SKU))
				.assertNoValues();
	}

	@Test
	public void isProductBundleByCodeReturnTrueWhenProductSkuIsBundle() {
		ProductSku mockProductSku = mock(ProductSku.class);
		Product product = mock(Product.class);
		when(mockProductSku.getProduct()).thenReturn(product);
		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(mockProductSku);
		when(mockBundleIdentifier.isBundle(product)).thenReturn(true);

		repository.isProductBundleByCode(SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(true);
	}

	@Test
	public void isProductBundleByCodeReturnTrueWhenProductSkuIsNotBundle() {
		ProductSku mockProductSku = mock(ProductSku.class);
		Product product = mock(Product.class);
		when(mockProductSku.getProduct()).thenReturn(product);
		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(mockProductSku);
		when(mockBundleIdentifier.isBundle(product)).thenReturn(false);

		repository.isProductBundleByCode(SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValue(false);
	}

	@Test
	public void shouldSucceedOnValidItemIdWhenCheckingIfProductSkuExists() {
		String skuCode = "sony_bt_sku";
		when(productSkuLookup.isProductSkuExist(skuCode))
			.thenReturn(Boolean.TRUE);

		repository.isProductSkuExistByCode(skuCode)
				.test()
				.assertNoErrors()
				.assertValue(true);

		verify(productSkuLookup).isProductSkuExist(skuCode);
	}

	@Test
	public void shouldFailOnLookupCallFailureWhenCheckingIfProductSkuExists() {
		String skuCode = "sony_bt_sku";
		when(productSkuLookup.isProductSkuExist(skuCode))
			.thenThrow(new EpServiceException("Failure"));

		repository.isProductSkuExistByCode(skuCode)
				.test()
				.assertError(EpServiceException.class)
				.assertErrorMessage("Failure");
	}

	@Test
	public void shouldReturnSkuOptionsGivenValidSkuCode() {
		final ProductSku mockProductSku = mock(ProductSku.class);
		final Product mockProduct = mock(Product.class);
		final ProductType mockProductType = mock(ProductType.class);
		final SkuOption mockSkuOption1 = mock(SkuOption.class);
		final SkuOption mockSkuOption2 = mock(SkuOption.class);
		final Set<SkuOption> skuOptions = Sets.newSet(mockSkuOption1, mockSkuOption2);

		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(mockProductSku);
		when(mockProductSku.getProduct()).thenReturn(mockProduct);
		when(mockProduct.getProductType()).thenReturn(mockProductType);
		when(mockProductType.getSkuOptions()).thenReturn(skuOptions);
		repository.getProductSkuOptionsByCode(SKU_CODE)
				.test()
				.assertNoErrors()
				.assertValueCount(skuOptions.size())
				.assertValueSet(skuOptions);
	}

	@Test
	public void shouldReturnNotFoundWhengetProductSkuOptionsByCodeGivenInvalidSkuCode() {
		when(productSkuLookup.findBySkuCode(SKU_CODE)).thenReturn(null);

		repository.getProductSkuOptionsByCode(SKU_CODE)
				.test()
				.assertError(ResourceOperationFailure.notFound(NOT_FOUND_MESSAGE))
				.assertNoValues();
	}
}
