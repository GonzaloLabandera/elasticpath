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
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentOptionsIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.sku.ProductSkuRepository;

/**
 * Test for {@link ComponentOptionsLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ComponentOptionsLinksRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String COMPONENT_ID = "componentId";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionComponentIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ProductSkuRepository productSkuRepository;
	@InjectMocks
	private ComponentOptionsLinksRepositoryImpl<ItemDefinitionComponentIdentifier, ItemDefinitionComponentOptionsIdentifier> repository;

	@Mock
	private BundleConstituent bundleConstituent;
	@Mock
	private ConstituentItem constituentItem;
	@Mock
	private ProductSku productSku;
	@Mock
	private SkuOption skuOption;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionComponentIdentifier(SCOPE, SKU_CODE, COMPONENT_ID);
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any())).thenReturn(Single.just(bundleConstituent));
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.getProductSku()).thenReturn(productSku);
		when(productSku.getSkuCode()).thenReturn(SKU_CODE);
		when(productSkuRepository.getProductSkuOptionsByCode(SKU_CODE)).thenReturn(Observable.just(skuOption));
	}

	@Test
	public void getElementsReturnItemDefinitionComponentOptionsIdentifierWithValidIdentifier() {
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionComponentOptionsIdentifier.builder()
						.withItemDefinitionComponent(identifier)
						.build())
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoBundleConstituentFoundForTheGivenItemId() {
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenErrorIsReturnWhenLookingForSkuOption() {
		when(productSkuRepository.getProductSkuOptionsByCode(SKU_CODE))
				.thenReturn(Observable.error(ResourceOperationFailure.notFound()));
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoValuesIsReturnedWhenLookingForSkuOption() {
		when(productSkuRepository.getProductSkuOptionsByCode(SKU_CODE))
				.thenReturn(Observable.empty());
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}
}