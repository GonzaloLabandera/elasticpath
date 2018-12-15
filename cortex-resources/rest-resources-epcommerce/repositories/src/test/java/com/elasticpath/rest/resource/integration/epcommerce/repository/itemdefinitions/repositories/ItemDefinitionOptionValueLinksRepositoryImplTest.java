/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;

/**
 * Test for {@link ItemDefinitionOptionValueLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionOptionValueLinksRepositoryImplTest {

	private static final String NOT_FOUND_MESSAGE = "Could not find item for item ID.";
	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String OPTION_ID = "optionId";
	private static final String OPTION_VALUE_ID = "optionValueId";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionOptionIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;
	@InjectMocks
	private ItemDefinitionOptionValueLinksRepositoryImpl<ItemDefinitionOptionIdentifier, ItemDefinitionOptionValueIdentifier> repository;

	@Mock
	private ProductSku productSku;
	@Mock
	private Map<String, SkuOptionValue> optionValueMap;
	@Mock
	private SkuOptionValue skuOptionValue;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		identifier = IdentifierTestFactory.buildItemDefinitionOptionIdentifier(SCOPE, SKU_CODE, OPTION_ID);
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.just(productSku));
		when(productSku.getOptionValueMap()).thenReturn(optionValueMap);
		when(optionValueMap.get(OPTION_ID)).thenReturn(skuOptionValue);
		when(skuOptionValue.getOptionValueKey()).thenReturn(OPTION_VALUE_ID);
	}

	@Test
	public void getElementsReturnItemDefinitionOptionValueIdentifierWithValidIdentifier() {
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionOptionValueIdentifier.builder()
						.withItemDefinitionOption(identifier)
						.withOptionValueId(StringIdentifier.of(OPTION_VALUE_ID))
						.build());
	}

	@Test
	public void getElementsReturnErrorWhenNoProductSkuFound() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.error(ResourceOperationFailure.notFound(NOT_FOUND_MESSAGE)));
		repository.getElements(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(NOT_FOUND_MESSAGE))
				.assertNoValues();
	}

	@Test
	public void getElementsReturnErrorWhenNoSkuOptionValueFoundForOptionId() {
		when(optionValueMap.get(OPTION_ID)).thenReturn(null);
		repository.getElements(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(VALUE_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void getSkuOptionValueReturnSkuOptionValueForGivenOptionId() {
		repository.getSkuOptionValue(productSku, OPTION_ID)
				.test()
				.assertNoErrors()
				.assertValue(skuOptionValue);
	}

	@Test
	public void getSkuOptionValueReturnErrorWhenNoSkuOptionValueFoundForGivenOptionId() {
		when(optionValueMap.get(OPTION_ID)).thenReturn(null);
		repository.getSkuOptionValue(productSku, OPTION_ID)
				.test()
				.assertError(ResourceOperationFailure.notFound(VALUE_NOT_FOUND))
				.assertNoValues();
	}
}