/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl.options;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseLineItemOptionsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Test for the  {@link OptionsEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OptionsEntityRepositoryImplTest {

	private static final String LINE_ITEM_ID = "line item id";
	private static final String CODE_1 = "code 1";
	private static final String CODE_2 = "code 2";

	@Mock
	private ProductSkuRepository productSkuRepository;
	@InjectMocks
	private OptionsEntityRepositoryImpl repository;

	@Mock
	private PurchaseLineItemOptionsIdentifier identifier;
	@Mock
	private ProductSku productSku;

	@Before
	public void setUp() {
		PurchaseLineItemIdentifier purchaseLineItem = mock(PurchaseLineItemIdentifier.class);
		when(purchaseLineItem.getLineItemId()).thenReturn(PathIdentifier.of(LINE_ITEM_ID));
		when(identifier.getPurchaseLineItem()).thenReturn(purchaseLineItem);
		when(productSkuRepository.getProductSkuWithAttributesByGuidAsSingle(LINE_ITEM_ID)).thenReturn(Single.just(productSku));
	}

	@Test
	public void testGetOneElement() {
		Set<String> valueCodes = new HashSet<>();
		valueCodes.add(CODE_1);
		when(productSku.getOptionValueCodes()).thenReturn(valueCodes);

		PurchaseLineItemOptionIdentifier firstOption = PurchaseLineItemOptionIdentifier.builder()
				.withOptionId(StringIdentifier.of(CODE_1))
				.withPurchaseLineItemOptions(identifier)
				.build();

		repository.getElements(identifier)
				.test()
				.assertValueSequence(ImmutableList.of(firstOption));
	}

	@Test
	public void testGetSeveralElements() {
		Set<String> valueCodes = new HashSet<>();
		valueCodes.add(CODE_1);
		valueCodes.add(CODE_2);
		when(productSku.getOptionValueCodes()).thenReturn(valueCodes);

		PurchaseLineItemOptionIdentifier firstOption = PurchaseLineItemOptionIdentifier.builder()
				.withOptionId(StringIdentifier.of(CODE_1))
				.withPurchaseLineItemOptions(identifier)
				.build();

		PurchaseLineItemOptionIdentifier secondOption = PurchaseLineItemOptionIdentifier.builder()
				.withOptionId(StringIdentifier.of(CODE_2))
				.withPurchaseLineItemOptions(identifier)
				.build();

		repository.getElements(identifier)
				.test()
				.assertValueSequence(ImmutableList.of(firstOption, secondOption));
	}

	@Test
	public void testOptionsNotFound() {
		when(productSku.getOptionValueCodes()).thenReturn(new HashSet<>());

		repository.getElements(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OptionsEntityRepositoryImpl.OPTIONS_NOT_FOUND));
	}


}
