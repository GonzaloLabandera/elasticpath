/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.repositories;

import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Locale;
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

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.catalog.Availability;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.definition.availabilities.AvailabilityEntity;
import com.elasticpath.rest.definition.availabilities.AvailabilityForItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.AvailabilityTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.item.ItemRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.util.date.DateUtil;

/**
 * Test for {@link ItemAvailabilityEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ItemAvailabilityEntityRepositoryImplTest {

	private static final String ITEM_ID = "itemid";
	private static final Map<String, String> ITEM_ID_MAP = ImmutableSortedMap.of(ItemRepository.SKU_CODE_KEY, ITEM_ID);
	private static final String PRODUCT_GUID = "product guid";
	private static final Date DATE = new Date();

	@Mock
	private ItemRepository itemRepository;
	@Mock
	private StoreProductRepository storeProductRepository;
	@Mock
	private ConversionService conversionService;
	@Mock
	private ProductSku productSku;
	@Mock
	private Product product;
	@Mock
	private StoreProduct storeProduct;

	@InjectMocks
	private ItemAvailabilityEntityRepositoryImpl<AvailabilityEntity, AvailabilityForItemIdentifier> itemAvailabilityEntityRepository;

	@Before
	public void setUp() {
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.just(productSku));
		when(productSku.getProduct()).thenReturn(product);
		when(product.getGuid()).thenReturn(PRODUCT_GUID);
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(ResourceTestConstants.SCOPE, PRODUCT_GUID))
				.thenReturn(Single.just(storeProduct));
		when(conversionService.convert(new Pair<>(storeProduct, productSku), AvailabilityEntity.class))
				.thenReturn(AvailabilityTestFactory.createAvailabilityEntity(
						Availability.AVAILABLE.getName(),
						DATE.getTime(),
						DateUtil.formatDateTime(DATE, Locale.CANADA)));
	}

	@Test
	public void findOneReturnsItemAvailabilityEntityWithReleaseDateAndState() {
		itemAvailabilityEntityRepository.findOne(
				AvailabilityTestFactory.createAvailabilityForItemIdentifier(ITEM_ID, ResourceTestConstants.SCOPE))
				.test()
				.assertValue(itemAvailabilityEntity -> itemAvailabilityEntity.getReleaseDate().getValue().equals(DATE.getTime()))
				.assertValue(itemAvailabilityEntity -> itemAvailabilityEntity.getReleaseDate().getDisplayValue()
						.equals(DateUtil.formatDateTime(DATE, Locale.CANADA)))
				.assertValue(itemAvailabilityEntity -> itemAvailabilityEntity.getState().equals(Availability.AVAILABLE.getName()));
	}

	@Test
	public void findOneReturnsErrorWhenNoSkuFoundForTheGivenItemId() {
		String itemNotFound = "Item not found";
		when(itemRepository.getSkuForItemId(ITEM_ID_MAP)).thenReturn(Single.error(ResourceOperationFailure.notFound(itemNotFound)));
		itemAvailabilityEntityRepository.findOne(
				AvailabilityTestFactory.createAvailabilityForItemIdentifier(ITEM_ID, ResourceTestConstants.SCOPE))
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertErrorMessage(itemNotFound);
	}

	@Test
	public void findOneReturnsErrorWhenProductNotFoundInTheStore() {
		String productNotFound = "Product not found";
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuid(ResourceTestConstants.SCOPE, PRODUCT_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(productNotFound)));
		itemAvailabilityEntityRepository.findOne(
				AvailabilityTestFactory.createAvailabilityForItemIdentifier(ITEM_ID, ResourceTestConstants.SCOPE))
				.test()
				.assertError(ResourceOperationFailure.class)
				.assertErrorMessage(productNotFound);
	}
}