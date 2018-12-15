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
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link ItemDefinitionEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionEntityRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ConversionService conversionService;
	@InjectMocks
	private ItemDefinitionEntityRepositoryImpl<ItemDefinitionEntity, ItemDefinitionIdentifier> repository;

	@Mock
	private ProductSku productSku;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionIdentifier(SCOPE, SKU_CODE);
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.just(productSku));
		when(conversionService.convert(productSku, ItemDefinitionEntity.class)).thenReturn(ItemDefinitionEntity.builder().build());
	}

	@Test
	public void findOneReturnItemDefinitionEntityWithValidIdentifier() {
		repository.findOne(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionEntity.builder()
						.withItemId(CompositeIdUtil.encodeCompositeId(ITEM_ID_MAP))
						.build());
	}

	@Test
	public void findOneReturnErrorWhenNoProductSkuFoundForTheGivenItemId() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound())
				.assertNoValues();
	}
}