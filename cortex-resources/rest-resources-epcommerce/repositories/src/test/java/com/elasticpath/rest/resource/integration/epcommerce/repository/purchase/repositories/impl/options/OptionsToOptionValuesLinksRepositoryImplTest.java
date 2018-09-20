/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionValueIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionsIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;

/**
 * Test for the  {@link OptionsToOptionValuesLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OptionsToOptionValuesLinksRepositoryImplTest {

	private static final String OPTION_ID = "optionId";
	private static final String SKU_GUID = "skuGuid";
	private static final String OPTION_VALUE = "optionValue";

	@Mock
	private ProductSkuRepository productSkuRepository;
	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;
	@InjectMocks
	private OptionsToOptionValuesLinksRepositoryImpl repository;
	private PurchaseLineItemOptionIdentifier identifier;

	@Mock
	private ProductSku productSku;
	@Mock
	private SkuOptionValue optionValue;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		setUpPurchaseLineItemOptionIdentifier();
	}

	@Test
	public void testOptionValueForOptionIdentifier() {

		when(productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(SKU_GUID)).thenReturn(Single.just(productSku));
		Map<String, SkuOptionValue> optionValueMap = new HashMap<>();
		optionValueMap.put(OPTION_ID, optionValue);
		when(productSku.getOptionValueMap()).thenReturn(optionValueMap);
		when(optionValue.getOptionValueKey()).thenReturn(OPTION_VALUE);

		repository.getElements(identifier)
				.test()
				.assertValueSequence(ImmutableList.of(PurchaseLineItemOptionValueIdentifier.builder()
						.withOptionValueId(StringIdentifier.of(OPTION_VALUE))
						.withPurchaseLineItemOption(identifier)
						.build()
				));
	}

	@Test
	public void testOptionValueNotFoundInMap() {

		when(productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(SKU_GUID)).thenReturn(Single.just(productSku));
		when(productSku.getOptionValueMap()).thenReturn(new HashMap<>());

		repository.getElements(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OptionsToOptionValuesLinksRepositoryImpl.VALUE_FOR_OPTION_NOT_FOUND));
	}

	private void setUpPurchaseLineItemOptionIdentifier() {
		PurchaseLineItemIdentifier purchaseLineItem = PurchaseLineItemIdentifier.builder()
				.withLineItemId(PathIdentifier.of(SKU_GUID))
				.withPurchaseLineItems(mock(PurchaseLineItemsIdentifier.class))
				.build();
		PurchaseLineItemOptionsIdentifier options = PurchaseLineItemOptionsIdentifier.builder()
				.withPurchaseLineItem(purchaseLineItem)
				.build();
		identifier = PurchaseLineItemOptionIdentifier.builder()
				.withOptionId(StringIdentifier.of(OPTION_ID))
				.withPurchaseLineItemOptions(options)
				.build();
	}
}
