/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link ItemDefinitionOptionValueEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionOptionValueEntityRepositoryImplTest {

	private static final String OPTION_NOT_FOUND_FOR_ITEM = "Option not found for item.";
	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String OPTION_ID_1 = "optionId1";
	private static final String OPTION_ID_2 = "optionId2";
	private static final String OPTION_VALUE_ID_1 = "optionValueId1";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionOptionValueIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ConversionService conversionService;
	@InjectMocks
	private ItemDefinitionOptionValueEntityRepositoryImpl<ItemDefinitionOptionValueEntity, ItemDefinitionOptionValueIdentifier> repository;

	@Mock
	private SkuOption skuOption1;
	@Mock
	private SkuOption skuOption2;
	@Mock
	private SkuOptionValue skuOptionValue1;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionOptionValueIdentifier(SCOPE, SKU_CODE, OPTION_ID_1, OPTION_VALUE_ID_1);
		when(itemRepository.getSkuOptionsForItemId(ITEM_ID_MAP)).thenReturn(Observable.just(skuOption1, skuOption2));
		when(skuOption1.getOptionKey()).thenReturn(OPTION_ID_1);
		when(skuOption2.getOptionKey()).thenReturn(OPTION_ID_2);
		when(skuOption1.contains(OPTION_VALUE_ID_1)).thenReturn(true);
		when(skuOption1.getOptionValue(OPTION_VALUE_ID_1)).thenReturn(skuOptionValue1);
		when(conversionService.convert(skuOptionValue1, ItemDefinitionOptionValueEntity.class))
				.thenReturn(ItemDefinitionOptionValueEntity.builder()
						.withName(OPTION_ID_1)
						.build());
	}

	@Test
	public void findOneReturnItemDefinitionOptionValueEntityWithValidIdentifier() {
		repository.findOne(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionOptionValueEntity.builder()
						.withName(OPTION_ID_1)
						.build());
	}

	@Test
	public void findOneReturnErrorWhenErrorIsReturnedWhenLookingForSkuOptionGivenItemId() {
		when(itemRepository.getSkuOptionsForItemId(ITEM_ID_MAP))
				.thenReturn(Observable.error(ResourceOperationFailure.notFound()));
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OPTION_NOT_FOUND_FOR_ITEM))
				.assertNoValues();
	}

	@Test
	public void findOneReturnErrorWhenNoSkuOptionsFoundForGivenItemId() {
		when(itemRepository.getSkuOptionsForItemId(ITEM_ID_MAP)).thenReturn(Observable.empty());
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OPTION_NOT_FOUND_FOR_ITEM))
				.assertNoValues();
	}

	@Test
	public void findOneReturnErrorWhenNoSkuOptionFoundForGivenOptionId() {
		when(skuOption1.getOptionKey()).thenReturn("wrongId");
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OPTION_NOT_FOUND_FOR_ITEM))
				.assertNoValues();
	}

	@Test
	public void findOneReturnErrorWhenNoSkuOptionContainsGivenOptionValueId() {
		when(skuOption1.contains(OPTION_VALUE_ID_1)).thenReturn(false);
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OPTION_NOT_FOUND_FOR_ITEM))
				.assertNoValues();
	}
}