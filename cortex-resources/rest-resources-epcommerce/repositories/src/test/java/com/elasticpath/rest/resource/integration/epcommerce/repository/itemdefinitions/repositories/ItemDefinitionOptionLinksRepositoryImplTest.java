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

import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link ItemDefinitionOptionLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionOptionLinksRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String SKU_GUID = "skuGuid";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionOptionsIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private ItemDefinitionOptionLinksRepositoryImpl<ItemDefinitionOptionsIdentifier, ItemDefinitionOptionIdentifier> repository;

	@Mock
	private SkuOption skuOption;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionOptionsIdentifier(SCOPE, SKU_CODE);
		when(itemRepository.getSkuOptionsForItemId(ITEM_ID_MAP)).thenReturn(Observable.just(skuOption));
		when(skuOption.getGuid()).thenReturn(SKU_GUID);
	}

	@Test
	public void getElementsReturnItemDefinitionOptionIdentifierWithValidIdentifier() {
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionOptionIdentifier.builder()
						.withItemDefinitionOptions(identifier)
						.withOptionId(StringIdentifier.of(SKU_GUID))
						.build())
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoSkuOptionsFoundForGivenItemId() {
		when(itemRepository.getSkuOptionsForItemId(ITEM_ID_MAP)).thenReturn(Observable.empty());
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}
}