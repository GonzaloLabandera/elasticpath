/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.components;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemComponentsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Test for the  {@link LineItemToComponentsRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LineItemToComponentsRepositoryImplTest {

	private static final String LINE_ITEM_ID = "line item id";
	@Mock
	private ProductSkuRepository productSkuRepository;

	@InjectMocks
	private LineItemToComponentsRepositoryImpl<PurchaseLineItemIdentifier, PurchaseLineItemComponentsIdentifier> repository;

	@Before
	public void setUp() {
		repository.setProductSkuRepository(productSkuRepository);
	}

	@Test
	public void componentsInsideOfBundleTest() {

		Product bundle = mock(ProductBundle.class);

		performSetupWithProduct(bundle);

		PurchaseLineItemIdentifier identifier = getPurchaseLineItemIdentifier();

		repository.getElements(identifier)
				.test()
				.assertValue(PurchaseLineItemComponentsIdentifier.builder()
						.withPurchaseLineItem(identifier)
						.build());
	}

	@Test
	public void noComponentsForLineItemTest() {

		Product storeProduct = mock(StoreProduct.class);

		performSetupWithProduct(storeProduct);

		PurchaseLineItemIdentifier identifier = getPurchaseLineItemIdentifier();

		repository.getElements(identifier)
				.test()
				.assertValueCount(0);
	}

	private PurchaseLineItemIdentifier getPurchaseLineItemIdentifier() {
		PurchaseLineItemsIdentifier lineItems = mock(PurchaseLineItemsIdentifier.class);
		return PurchaseLineItemIdentifier.builder()
				.withLineItemId(PathIdentifier.of(LINE_ITEM_ID))
				.withPurchaseLineItems(lineItems)
				.build();
	}

	private void performSetupWithProduct(final Product product) {
		ProductSku productSku = mock(ProductSku.class);

		when(productSku.getProduct()).thenReturn(product);

		when(productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(LINE_ITEM_ID))
				.thenReturn(Single.just(productSku));
	}
}
