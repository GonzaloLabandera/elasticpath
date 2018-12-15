/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionNestedComponentsIdentifier;
import com.elasticpath.rest.id.type.PathIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link NestedComponentLinksRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NestedComponentLinksRepositoryImplTest {

	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String COMPONENT_ID = "componentId";
	private static final String NESTED_BUNDLE_CONS_GUID_1 = "nestedComponentId1";
	private static final String NESTED_BUNDLE_CONS_GUID_2 = "nestedComponentId2";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private ItemDefinitionNestedComponentsIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@InjectMocks
	private NestedComponentLinksRepositoryImpl<ItemDefinitionNestedComponentsIdentifier, ItemDefinitionComponentIdentifier> repository;

	@Mock
	private BundleConstituent bundleConstituent;
	@Mock
	private ProductBundle productBundle;
	@Mock
	private BundleConstituent nestedBundleConstituent1;
	@Mock
	private BundleConstituent nestedBundleConstituent2;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionNestedComponentsIdentifier(SCOPE, SKU_CODE, COMPONENT_ID);
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any())).thenReturn(Single.just(bundleConstituent));
		when(itemRepository.getProductBundleFromConstituent(bundleConstituent)).thenReturn(Single.just(productBundle));
		when(productBundle.getConstituents()).thenReturn(Arrays.asList(nestedBundleConstituent1, nestedBundleConstituent2));
		when(nestedBundleConstituent1.getGuid()).thenReturn(NESTED_BUNDLE_CONS_GUID_1);
		when(nestedBundleConstituent2.getGuid()).thenReturn(NESTED_BUNDLE_CONS_GUID_2);
	}

	@Test
	public void getElementsReturnItemDefinitionComponentIdentifierWithValidIdentifier() {
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValues(buildItemDefinitionComponentIdentifier(NESTED_BUNDLE_CONS_GUID_1),
						buildItemDefinitionComponentIdentifier(NESTED_BUNDLE_CONS_GUID_2))
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoBundleConstituentFoundForGivenItemId() {
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoProductBundleFoundForGivenItemId() {
		when(itemRepository.getProductBundleFromConstituent(bundleConstituent)).thenReturn(Single.error(ResourceOperationFailure.notFound()));
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	@Test
	public void getElementsReturnNoValuesWhenNoConstituentsFoundForProductBundle() {
		when(productBundle.getConstituents()).thenReturn(Collections.emptyList());
		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues()
				.assertComplete();
	}

	private ItemDefinitionComponentIdentifier buildItemDefinitionComponentIdentifier(final String bundleConsGuid) {
		return ItemDefinitionComponentIdentifier.builder()
				.withItemDefinitionComponents(identifier.getItemDefinitionComponent().getItemDefinitionComponents())
				.withComponentId(PathIdentifier.of(identifier.getItemDefinitionComponent().getComponentId(), bundleConsGuid))
				.build();
	}
}