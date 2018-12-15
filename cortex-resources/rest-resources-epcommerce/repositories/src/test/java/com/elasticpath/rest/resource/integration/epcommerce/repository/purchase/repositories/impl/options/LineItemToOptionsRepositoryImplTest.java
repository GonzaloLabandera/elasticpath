/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Test for the  {@link LineItemToOptionsRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LineItemToOptionsRepositoryImplTest {

	private static final String ITEM_ID = "itemId";
	private static final String PURCHASE_ID = "purchaseId";
	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";

	@Mock
	private ProductSkuRepository productSkuRepository;

	@Mock
	private OrderRepository orderRepository;
	@InjectMocks
	private LineItemToOptionsRepositoryImpl<PurchaseLineItemIdentifier, PurchaseLineItemOptionsIdentifier> repository;

	@Mock
	private ProductSku productSku;
	private PurchaseLineItemIdentifier purchaseLineItemIdentifier;

	@Test
	public void testNoOptionsForTheLineItem() {

		createPurchaseLineItemIdentifier();
		setUpSkuRepoAndOptionLookupWithOptions(Observable.empty());

		repository.getElements(purchaseLineItemIdentifier)
				.test()
				.assertNoValues();
	}

	@Test
	public void testSeveralOptionsForTheLineItem() {

		Observable<SkuOption> options = Observable.just(mock(SkuOption.class), mock(SkuOption.class));
		createPurchaseLineItemIdentifier();
		setUpSkuRepoAndOptionLookupWithOptions(options);

		repository.getElements(purchaseLineItemIdentifier)
				.test()
				.assertValue(PurchaseLineItemOptionsIdentifier.builder()
						.withPurchaseLineItem(purchaseLineItemIdentifier)
						.build());

	}

	private void setUpSkuRepoAndOptionLookupWithOptions(final Observable<SkuOption> options) {
		when(orderRepository.findProductSku(any(), any(), any())).thenReturn(Single.just(productSku));
		when(productSku.getSkuCode()).thenReturn(SKU_CODE);
		when(productSkuRepository.getProductSkuOptionsByCode(SKU_CODE)).thenReturn(options);
	}

	private void createPurchaseLineItemIdentifier() {
		PurchasesIdentifier purchases = PurchasesIdentifier.builder()
				.withScope(StringIdentifier.of(SCOPE))
				.build();
		PurchaseIdentifier purchase = PurchaseIdentifier.builder()
				.withPurchases(purchases)
				.withPurchaseId(StringIdentifier.of(PURCHASE_ID))
				.build();
		PurchaseLineItemsIdentifier purchaseLineItems = PurchaseLineItemsIdentifier.builder()
				.withPurchase(purchase)
				.build();
		purchaseLineItemIdentifier = PurchaseLineItemIdentifier.builder()
				.withPurchaseLineItems(purchaseLineItems)
				.withLineItemId(PathIdentifier.of(ITEM_ID))
				.build();
	}
}
