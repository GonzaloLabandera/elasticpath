/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.impl.promotions.applied;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.PromotionTestFactory
		.buildAppliedPromotionsForShippingOptionIdentifier;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.shipping.ShippingServiceLevel;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.promotions.AppliedPromotionsForShippingOptionIdentifier;
import com.elasticpath.rest.definition.promotions.PromotionIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.PromotionRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.shipping.ShippingServiceLevelRepository;

/**
 * Test for {@link AppliedPromotionsForShippingOptionRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppliedPromotionsForShippingOptionRepositoryImplTest {

	private final AppliedPromotionsForShippingOptionIdentifier identifier = buildAppliedPromotionsForShippingOptionIdentifier();

	private final List<ShippingServiceLevel> shippingServiceLevels = ImmutableList.of();

	@Mock
	private ShippingServiceLevel shippingServiceLevel;

	@Mock
	private ShoppingCart shoppingCart;

	@Mock
	private ShoppingCartPricingSnapshot shoppingCartPricingSnapshot;

	@InjectMocks
	private AppliedPromotionsForShippingOptionRepositoryImpl<AppliedPromotionsForShippingOptionIdentifier, PromotionIdentifier> repository;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private ShippingServiceLevelRepository shippingServiceLevelRepository;

	@Mock
	private PricingSnapshotRepository pricingSnapshotRepository;

	@Mock
	private PromotionRepository promotionRepository;

	@Test
	public void verifyGetElementsReturnNotFoundWhenShoppingCartNotFound() {
		when(cartOrderRepository.getEnrichedShoppingCartForShipments(ResourceTestConstants.SCOPE, ResourceTestConstants.SHIPMENT_DETAILS_ID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(ResourceTestConstants.NOT_FOUND)));

		repository.getElements(identifier)
				.test()
				.assertError(createErrorCheckPredicate(ResourceTestConstants.NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetElementsReturnNotFoundWhenPricingSnapshotIsNotFound() {
		when(cartOrderRepository.getEnrichedShoppingCartForShipments(ResourceTestConstants.SCOPE, ResourceTestConstants.SHIPMENT_DETAILS_ID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshotSingle(shoppingCart))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(ResourceTestConstants.NOT_FOUND)));

		repository.getElements(identifier)
				.test()
				.assertError(createErrorCheckPredicate(ResourceTestConstants.NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetElementsReturnEmptyWhenShippingServiceLevelNotFound() {
		when(cartOrderRepository.getEnrichedShoppingCartForShipments(ResourceTestConstants.SCOPE, ResourceTestConstants.SHIPMENT_DETAILS_ID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshotSingle(shoppingCart)).thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(shoppingCart.getShippingServiceLevelList()).thenReturn(shippingServiceLevels);
		when(shippingServiceLevelRepository.getShippingServiceLevel(any(), any()))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(ResourceTestConstants.NOT_FOUND)));

		repository.getElements(identifier)
				.test()
				.assertError(createErrorCheckPredicate(ResourceTestConstants.NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetElementsReturnEmptyWhenPromotionsAreNotFound() {
		when(cartOrderRepository.getEnrichedShoppingCartForShipments(ResourceTestConstants.SCOPE, ResourceTestConstants.SHIPMENT_DETAILS_ID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshotSingle(shoppingCart)).thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(shoppingCart.getShippingServiceLevelList()).thenReturn(shippingServiceLevels);
		when(shippingServiceLevelRepository.getShippingServiceLevel(any(), any()))
				.thenReturn(Single.just(shippingServiceLevel));
		when(promotionRepository.getAppliedShippingPromotions(shoppingCartPricingSnapshot, shippingServiceLevel))
				.thenReturn(Collections.emptyList());

		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void verifyGetElementsReturnPromotionIdentifiers() {
		when(cartOrderRepository.getEnrichedShoppingCartForShipments(ResourceTestConstants.SCOPE, ResourceTestConstants.SHIPMENT_DETAILS_ID))
				.thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshotSingle(shoppingCart)).thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(shoppingCart.getShippingServiceLevelList()).thenReturn(shippingServiceLevels);
		when(shippingServiceLevelRepository.getShippingServiceLevel(any(), any()))
				.thenReturn(Single.just(shippingServiceLevel));
		when(promotionRepository.getAppliedShippingPromotions(shoppingCartPricingSnapshot, shippingServiceLevel))
				.thenReturn(ImmutableList.of("0", "1"));

		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValueAt(0, promotionIdentifier -> promotionIdentifier.getPromotionId().getValue().equals("0"))
				.assertValueAt(1, promotionIdentifier -> promotionIdentifier.getPromotionId().getValue().equals("1"));
	}
}
