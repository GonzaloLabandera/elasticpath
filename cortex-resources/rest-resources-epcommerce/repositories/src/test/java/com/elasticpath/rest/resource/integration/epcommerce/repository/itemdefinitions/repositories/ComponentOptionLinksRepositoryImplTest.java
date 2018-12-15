/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.ImmutableSortedMap;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Test for {@link ComponentOptionLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ComponentOptionLinksRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String COMPONENT_ID = "componentId";
	private static final String OPTION_ID_1 = "optionId1";
	private static final String OPTION_ID_2 = "optionId2";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ProductSkuRepository productSkuRepository;
	@InjectMocks
	private ComponentOptionLinksRepositoryImpl<ItemDefinitionComponentOptionsIdentifier, ItemDefinitionComponentOptionIdentifier> repository;

	@Mock
	private BundleConstituent bundleConstituent;
	@Mock
	private ConstituentItem constituentItem;
	@Mock
	private ProductSku productSku;
	@Mock
	private SkuOption skuOption1;
	@Mock
	private SkuOption skuOption2;

	@Before
	public void setUp() {
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any())).thenReturn(Single.just(bundleConstituent));
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.getProductSku()).thenReturn(productSku);
		when(productSku.getSkuCode()).thenReturn(SKU_CODE);
		when(productSkuRepository.getProductSkuOptionsByCode(SKU_CODE)).thenReturn(Observable.just(skuOption1, skuOption2));
		when(skuOption1.getGuid()).thenReturn(OPTION_ID_1);
		when(skuOption2.getGuid()).thenReturn(OPTION_ID_2);
	}

	@Test
	public void getElementsReturnItemDefinitionComponentOptionIdentifiersWithValidIdentifier() {
		final ItemDefinitionComponentOptionsIdentifier identifier =
				IdentifierTestFactory.buildItemDefinitionComponentOptionsIdentifier(SCOPE, SKU_CODE, COMPONENT_ID);
		final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier1 =
				buildItemDefinitionComponentOptionIdentifier(identifier, OPTION_ID_1);
		final ItemDefinitionComponentOptionIdentifier itemDefinitionComponentOptionIdentifier2 =
				buildItemDefinitionComponentOptionIdentifier(identifier, OPTION_ID_2);
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValues(itemDefinitionComponentOptionIdentifier1, itemDefinitionComponentOptionIdentifier2)
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoBundleConstituentFoundForTheGivenItemId() {
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		final ItemDefinitionComponentOptionsIdentifier identifier =
				IdentifierTestFactory.buildItemDefinitionComponentOptionsIdentifier(SCOPE, SKU_CODE, COMPONENT_ID);
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoSkuOptionFoundForTheGivenSkuCode() {
		final ItemDefinitionComponentOptionsIdentifier identifier =
				IdentifierTestFactory.buildItemDefinitionComponentOptionsIdentifier(SCOPE, SKU_CODE, COMPONENT_ID);
		when(productSkuRepository.getProductSkuOptionsByCode(SKU_CODE))
				.thenReturn(Observable.error(ResourceOperationFailure.notFound()));
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	private ItemDefinitionComponentOptionIdentifier buildItemDefinitionComponentOptionIdentifier(
			final ItemDefinitionComponentOptionsIdentifier identifier, final String optionId) {
		return ItemDefinitionComponentOptionIdentifier.builder()
				.withItemDefinitionComponentOptions(identifier)
				.withOptionId(StringIdentifier.of(optionId))
				.build();
	}
}