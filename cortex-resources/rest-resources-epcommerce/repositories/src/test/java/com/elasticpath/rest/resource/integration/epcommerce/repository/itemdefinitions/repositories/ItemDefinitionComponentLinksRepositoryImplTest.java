/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

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
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link ItemDefinitionComponentLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionComponentLinksRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String COMPONENT_ID = "componentId";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionComponentsIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private ItemDefinitionComponentLinksRepositoryImpl<ItemDefinitionComponentsIdentifier, ItemDefinitionComponentIdentifier> repository;

	@Mock
	private BundleConstituent bundleConstituent;
	@Mock
	private ProductSku productSku;
	@Mock
	private Product product;
	@Mock
	private ProductBundle productBundle;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionComponentsIdentifier(SCOPE, SKU_CODE);
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.just(productSku));
		when(productSku.getProduct()).thenReturn(product);
		when(itemRepository.asProductBundle(product)).thenReturn(Single.just(productBundle));
		when(productBundle.getConstituents()).thenReturn(Collections.singletonList(bundleConstituent));
		when(bundleConstituent.getGuid()).thenReturn(COMPONENT_ID);
	}

	@Test
	public void getElementsReturnItemDefinitionComponentIdentifierWithValidIdentifier() {
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionComponentIdentifier.builder()
						.withItemDefinitionComponents(identifier)
						.withComponentId(PathIdentifier.of(COMPONENT_ID))
						.build())
				.assertComplete();
	}

	@Test
	public void getElementsReturnErrorWhenNoProductSkuFoundForTheGivenItemId() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.getElements(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound())
				.assertNoValues();
	}

	@Test
	public void getElementsReturnErrorWhenNoProductBundleFoundForTheGivenProduct() {
		when(itemRepository.asProductBundle(product))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.getElements(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound())
				.assertNoValues();
	}

	@Test
	public void getElementsReturnNoValuesWhenProductBundleHasNoConstituents() {
		when(productBundle.getConstituents()).thenReturn(Collections.emptyList());
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}
}