/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.repositories;

import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.Locale;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.commons.util.Pair;
import com.elasticpath.domain.catalog.AvailabilityCriteria;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalogview.StoreProduct;
import com.elasticpath.rest.definition.availabilities.AvailabilityEntity;
import com.elasticpath.rest.definition.availabilities.AvailabilityForCartLineItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.availabilities.AvailabilityTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.product.StoreProductRepository;
import com.elasticpath.rest.util.date.DateUtil;

/**
 * Test for {@link CartLineItemAvailabilityEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CartLineItemAvailabilityEntityRepositoryImplTest {

	private static final String PRODUCT_GUID = "product guid";
	private static final Date DATE = new Date();

	@Mock
	private ShoppingCartRepository shoppingCartRepository;

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
	private CartLineItemAvailabilityEntityRepositoryImpl<AvailabilityEntity, AvailabilityForCartLineItemIdentifier> availabilityRepository;

	@Before
	public void setup() {
		when(shoppingCartRepository.getProductSku(ResourceTestConstants.LINE_ITEM_ID)).thenReturn(Single.just(productSku));
		when(productSku.getProduct()).thenReturn(product);
		when(product.getGuid()).thenReturn(PRODUCT_GUID);
		when(storeProductRepository.findDisplayableStoreProductWithAttributesByProductGuidAsSingle(ResourceTestConstants.SCOPE, PRODUCT_GUID))
				.thenReturn(Single.just(storeProduct));
		when(conversionService.convert(new Pair<>(storeProduct, productSku), AvailabilityEntity.class))
				.thenReturn(AvailabilityTestFactory.createAvailabilityEntity(
						AvailabilityCriteria.ALWAYS_AVAILABLE.name(),
						DATE.getTime(),
						DateUtil.formatDateTime(DATE, Locale.CANADA)));
	}

	@Test
	public void findOneReturnsAvailabilityEntityWithReleaseDate() {
		AvailabilityForCartLineItemIdentifier availabilityForCartLineItemIdentifier =
				AvailabilityTestFactory.createAvailabilityForCartLineItemIdentifier(ResourceTestConstants.CART_ID, ResourceTestConstants.SCOPE,
						ResourceTestConstants.LINE_ITEM_ID);

		availabilityRepository.findOne(availabilityForCartLineItemIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(AvailabilityTestFactory.createAvailabilityEntity(
						AvailabilityCriteria.ALWAYS_AVAILABLE.name(),
						DATE.getTime(),
						DateUtil.formatDateTime(DATE, Locale.CANADA)));
	}
}