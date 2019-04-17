/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.itemdefinitions.repositories;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentEntity;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentIdentifier;
import com.elasticpath.rest.id.util.Base32Util;
import com.elasticpath.rest.id.util.CompositeIdUtil;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;

/**
 * Test for {@link ItemDefinitionComponentEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemDefinitionComponentEntityRepositoryImplTest {

	private static final String COMPONENT_NOT_FOUND = "Component not found.";
	private static final String SCOPE = "scope";
	private static final String SKU_CODE = "skuCode";
	private static final String COMPONENT_ID = "componentId";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, SKU_CODE);
	private static final Map<String, String> STANDALONE_ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, "standalone");
	private ItemDefinitionComponentIdentifier identifier;

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private ConversionService conversionService;
	@InjectMocks
	private ItemDefinitionComponentEntityRepositoryImpl<ItemDefinitionComponentEntity, ItemDefinitionComponentIdentifier> repository;

	@Mock
	private BundleConstituent bundleConstituent;
	@Mock
	private ConstituentItem constituentItem;
	@Mock
	private ProductSku productSku;

	@Before
	public void setUp() {
		identifier = IdentifierTestFactory.buildItemDefinitionComponentIdentifier(SCOPE, SKU_CODE, COMPONENT_ID);
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any())).thenReturn(Single.just(bundleConstituent));
		when(bundleConstituent.getConstituent()).thenReturn(constituentItem);
		when(constituentItem.getProductSku()).thenReturn(productSku);
		when(itemRepository.getItemIdForSku(productSku)).thenReturn(CompositeIdUtil.encodeCompositeId(STANDALONE_ITEM_ID_MAP));
		when(conversionService.convert(bundleConstituent, ItemDefinitionComponentEntity.class))
				.thenReturn(ItemDefinitionComponentEntity.builder().build());
		when(bundleConstituent.getGuid()).thenReturn(COMPONENT_ID);
	}

	@Test
	public void findOneReturnItemDefinitionComponentEntityWithValidIdentifier() {
		repository.findOne(identifier)
				.test()
				.assertNoErrors()
				.assertValue(ItemDefinitionComponentEntity.builder()
						.withStandaloneItemId(CompositeIdUtil.encodeCompositeId(STANDALONE_ITEM_ID_MAP))
						.withItemId(CompositeIdUtil.encodeCompositeId(ITEM_ID_MAP))
						.withComponentId(Base32Util.encode(COMPONENT_ID))
						.build());
	}

	@Test
	public void findOneReturnErrorWhenNoBundleConstituentFoundForGivenItemId() {
		when(itemRepository.findBundleConstituentAtPathEnd(eq(ITEM_ID_MAP), any()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(COMPONENT_NOT_FOUND)));
		repository.findOne(identifier)
				.test()
				.assertError(ResourceOperationFailure.notFound(COMPONENT_NOT_FOUND))
				.assertNoValues();
	}
}