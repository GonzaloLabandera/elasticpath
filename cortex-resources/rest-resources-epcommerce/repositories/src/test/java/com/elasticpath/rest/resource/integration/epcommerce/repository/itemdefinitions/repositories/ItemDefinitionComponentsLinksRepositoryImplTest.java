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

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link ItemDefinitionComponentsLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionComponentsLinksRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private ItemDefinitionComponentsLinksRepositoryImpl<ItemDefinitionIdentifier, ItemDefinitionComponentsIdentifier> repository;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionIdentifier(SCOPE, SKU_CODE);
		when(itemRepository.isItemBundle(ITEM_ID_MAP)).thenReturn(Single.just(true));
	}

	@Test
	public void getElementsReturnItemDefinitionComponentsIdentifierWithValidIdentifier() {
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionComponentsIdentifier.builder()
						.withItemDefinition(identifier)
						.build())
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValueWhenItemIsNotBundle() {
		when(itemRepository.isItemBundle(ITEM_ID_MAP)).thenReturn(Single.just(false));
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}
}