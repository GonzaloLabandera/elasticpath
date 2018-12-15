/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionValueIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionValueEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link ComponentOptionValueEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ComponentOptionValueEntityRepositoryImplTest {

	private static final String OPTION_NOT_FOUND_FOR_ITEM = "Option not found for item.";
	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String COMPONENT_ID = "componentId";
	private static final String OPTION_ID_1 = "optionId1";
	private static final String OPTION_ID_2 = "optionId2";
	private static final String OPTION_VALUE_ID_1 = "optionValueId1";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionComponentOptionValueIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ConversionService conversionService;
	@InjectMocks
	private ComponentOptionValueEntityRepositoryImpl<ItemDefinitionOptionValueEntity, ItemDefinitionComponentOptionValueIdentifier> repository;

	@Mock
	private BundleConstituent bundleConstituent;
	@Mock
	private ConstituentItem constituentItem;
	@Mock
	private Product product;
	@Mock
	private ProductType productType;
	@Mock
	private SkuOption skuOption1;
	@Mock
	private SkuOption skuOption2;
	@Mock
	private SkuOptionValue skuOptionValue1;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionComponentOptionValueIdentifier(
				SCOPE, SKU_CODE, COMPONENT_ID, OPTION_ID_1, OPTION_VALUE_ID_1);
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any())).thenReturn(Single.just(bundleConstituent));
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.getProduct()).thenReturn(product);
		when(product.getProductType()).thenReturn(productType);
		when(productType.getSkuOptions()).thenReturn(Sets.newSet(skuOption1, skuOption2));
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
	public void findOneReturnErrorWhenNoBundleConstituentFoundForTheGivenItemId() {
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(OPTION_NOT_FOUND_FOR_ITEM))
				.assertNoValues();
	}

	@Test
	public void findOneReturnErrorWhenBundleConstituentHasNoSkuOptions() {
		when(productType.getSkuOptions()).thenReturn(Collections.emptySet());
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