/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.product.impl;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.ProductImpl;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.domain.catalogview.impl.StoreProductImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;
import com.elasticpath.service.catalog.ProductLookup;
import com.elasticpath.service.catalogview.StoreProductService;

/**
 * Tests for {@link StoreProductRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StoreProductRepositoryImplTest {
	private static final String SCOPE = "MOBEE";
	private static final long PRODUCT_UID = 1L;
	private static final String PRODUCT_GUID = "product_code_1";
	private static final String ERROR_STORE_PRODUCT_NOT_FOUND = "Offer with GUID " + PRODUCT_GUID + " was not found in store " + SCOPE + ".";
	private static final String SKU_GUID = "sku guid";

	private Product product;
	private Store store;
	private StoreProduct storeProduct;

	@Mock
	private ProductLookup mockProductLookup;
	@Mock
	private StoreProductService mockStoreProductService;
	@Mock
	private StoreRepository mockStoreRepository;
	@Mock
	private ProductSkuRepository mockProductSkuRepository;

	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;

	@InjectMocks
	private StoreProductRepositoryImpl storeProductRepository;

	@Mock
	private ProductSku mockProductSku;

	@Before
	public void initialize() {
		product = new ProductImpl();
		product.setUidPk(PRODUCT_UID);
		product.setGuid(PRODUCT_GUID);
		when(mockProductLookup.findByGuid(product.getGuid())).thenReturn(product);
		when(mockProductLookup.findByUids(singletonList(PRODUCT_UID))).thenReturn(singletonList(product));

		when(mockProductSku.getProduct()).thenReturn(product);
		when(mockProductSkuRepository.getProductSkuWithAttributesByGuid(SKU_GUID)).thenReturn(Single.just(mockProductSku));

		store = new StoreImpl();
		when(mockStoreRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.just(store));

		storeProduct = new StoreProductImpl(product);
		((StoreProductImpl) storeProduct).setProductDisplayable(true);
		when(mockStoreProductService.getProductForStore(product, store)).thenReturn(storeProduct);

		storeProductRepository = new StoreProductRepositoryImpl(mockStoreRepository, mockProductLookup, mockStoreProductService,
				mockProductSkuRepository, reactiveAdapter);
	}

	@Test
	public void testFindStoreProduct() {
		storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, product.getGuid())
				.test()
				.assertNoErrors()
				.assertValue(storeProduct1 -> storeProduct1.getWrappedProduct().equals(product));
	}

	@Test
	public void testFindNotDisplayableStoreProduct() {
		((StoreProductImpl) storeProduct).setProductDisplayable(false);

		storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, product.getGuid())
				.test()
				.assertErrorMessage(ERROR_STORE_PRODUCT_NOT_FOUND);
	}

	@Test
	public void testFindMissingStoreProduct() {
		when(mockStoreProductService.getProductForStore(product, store)).thenReturn(null);

		storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, product.getGuid())
				.test()
				.assertErrorMessage(ERROR_STORE_PRODUCT_NOT_FOUND);
	}

	@Test
	public void testFindMissingProduct() {
		when(mockProductLookup.findByGuid(product.getGuid())).thenReturn(null);

		storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, product.getGuid())
				.test()
				.assertErrorMessage(ERROR_STORE_PRODUCT_NOT_FOUND);

		verifyZeroInteractions(mockStoreRepository);
		verifyZeroInteractions(mockStoreProductService);
	}

	@Test
	public void testFindProductInMissingStore() {
		when(mockStoreRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.error(ResourceOperationFailure.notFound()));

		storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(SCOPE, product.getGuid())
				.test()
				.assertError(ResourceOperationFailure.class);

		verifyZeroInteractions(mockStoreProductService);
	}

	@Test
	public void testFindByUids() {
		List<StoreProduct> products = storeProductRepository.findByUids(SCOPE, singletonList(PRODUCT_UID));

		assertEquals(product, products.get(0).getWrappedProduct());
		assertEquals(1, products.size());
	}

	@Test
	public void testFindByUidsWhereProductIsNotDisplayable() {
		((StoreProductImpl) storeProduct).setProductDisplayable(false);

		List<StoreProduct> products = storeProductRepository.findByUids(SCOPE, singletonList(PRODUCT_UID));

		assertEquals(0, products.size());
	}

	@Test
	public void testFindBySkuGuidWhenProductSkuIsFound() {
		storeProductRepository.findDisplayableStoreProductWithAttributesBySkuGuid(SCOPE, SKU_GUID)
				.test()
				.assertNoErrors();
	}

	@Test
	public void testFindBySkuGuidWhenProductSkuIsNotFound() {
		when(mockProductSkuRepository.getProductSkuWithAttributesByGuid(SKU_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));

		storeProductRepository.findDisplayableStoreProductWithAttributesBySkuGuid(SCOPE, SKU_GUID)
				.test()
				.assertError(ResourceOperationFailure.notFound());

		verifyZeroInteractions(mockStoreRepository);
		verifyZeroInteractions(mockStoreProductService);
	}
}
