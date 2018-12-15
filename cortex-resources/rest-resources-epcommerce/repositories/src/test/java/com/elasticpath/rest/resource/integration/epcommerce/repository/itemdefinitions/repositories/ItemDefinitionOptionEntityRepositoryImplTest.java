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
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionIdentifier;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.impl.ReactiveAdapterImpl;

/**
 * Test for {@link ItemDefinitionOptionEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionOptionEntityRepositoryImplTest {

	private static final String COMPONENT_NOT_FOUND = "Component not found.";
	private static final String VALUE_NOT_FOUND = "Option value not found for optionId.";
	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String OPTION_ID = "optionId";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionOptionIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ConversionService conversionService;
	@InjectMocks
	private ReactiveAdapterImpl reactiveAdapter;
	@InjectMocks
	private ItemDefinitionOptionEntityRepositoryImpl<ItemDefinitionOptionEntity, ItemDefinitionOptionIdentifier> repository;

	@Mock
	private ProductSku productSku;
	@Mock
	private SkuOptionValue skuOptionValue;
	@Mock
	private Map<String, SkuOptionValue> optionValueMap;

	@Before
	public void setUp() {
		repository.setReactiveAdapter(reactiveAdapter);
		identifier = IdentifierTestFactory.buildItemDefinitionOptionIdentifier(SCOPE, SKU_CODE, OPTION_ID);
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.just(productSku));
		when(productSku.getOptionValueMap()).thenReturn(optionValueMap);
		when(optionValueMap.get(OPTION_ID)).thenReturn(skuOptionValue);
		when(conversionService.convert(skuOptionValue, ItemDefinitionOptionEntity.class)).thenReturn(ItemDefinitionOptionEntity.builder().build());
	}

	@Test
	public void findOneReturnItemDefinitionEntityWithValidIdentifier() {
		repository.findOne(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionOptionEntity.builder()
						.withItemId(CompositeIdUtil.encodeCompositeId(ITEM_ID_MAP))
						.build());
	}

	@Test
	public void findOneReturnErrorWhenNoProductSkuFoundForTheGivenItemId() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.error(ResourceOperationFailure.notFound(COMPONENT_NOT_FOUND)));
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(COMPONENT_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void findOneReturnErrorWhenNoSkuOptionValueForTheGivenOptionId() {
		when(optionValueMap.get(OPTION_ID)).thenReturn(null);
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(VALUE_NOT_FOUND))
				.assertNoValues();
	}

	@Test
	public void getSkuOptionValueReturnSkuOptionValueForTheGivenValidOptionId() {
		repository.getSkuOptionValue(productSku, OPTION_ID)
				.test()
				.assertNoErrors()
				.assertValue(skuOptionValue);
	}

	@Test
	public void getSkuOptionValueErrorWhenNoSkuOptionValueForTheGivenOptionId() {
		when(optionValueMap.get(OPTION_ID)).thenReturn(null);
		repository.getSkuOptionValue(productSku, OPTION_ID)
				.test()
				.assertError(ResourceOperationFailure.notFound(VALUE_NOT_FOUND))
				.assertNoValues();
	}
}