/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.impl.promotions.applied;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.PromotionTestFactory.buildAppliedPromotionsForCartLineItemIdentifier;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.promotions.AppliedPromotionsForCartLineItemIdentifier;
import com.elasticpath.rest.definition.promotions.PromotionIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.PricingSnapshotRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.promotion.PromotionRepository;

/**
 * Test for {@link AppliedPromotionsForCartLineItemRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppliedPromotionsForCartLineItemRepositoryImplTest {

	private final AppliedPromotionsForCartLineItemIdentifier identifier = buildAppliedPromotionsForCartLineItemIdentifier();

	private static final int NUM_OF_ID = 2;

	@Mock
	private ShoppingCartPricingSnapshot shoppingCartPricingSnapshot;

	@Mock
	private ShoppingCart shoppingCart;

	@InjectMocks
	private AppliedPromotionsForCartLineItemRepositoryImpl<AppliedPromotionsForCartLineItemIdentifier, PromotionIdentifier> repository;

	@Mock
	private PromotionRepository promotionRepository;

	@Mock
	private CartOrderRepository cartOrderRepository;

	@Mock
	private PricingSnapshotRepository pricingSnapshotRepository;

	@Test
	public void verifyGetElementsReturnNotFoundWhenCartOrderNotFound() {
		when(cartOrderRepository.getEnrichedShoppingCart(
				ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID, CartOrderRepository.FindCartOrder.BY_CART_GUID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(ResourceTestConstants.NOT_FOUND)));

		repository.getElements(identifier)
				.test()
				.assertError(createErrorCheckPredicate(ResourceTestConstants.NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetElementsReturnNotFoundWhenPricingSnapshotNotFound() {
		when(cartOrderRepository.getEnrichedShoppingCart(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID,
				CartOrderRepository.FindCartOrder.BY_CART_GUID)).thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(ResourceTestConstants.NOT_FOUND)));

		repository.getElements(identifier)
				.test()
				.assertError(createErrorCheckPredicate(ResourceTestConstants.NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyGetElementsReturnEmptyWhenAppliedPromotionsAreEmpty() {
		when(cartOrderRepository.getEnrichedShoppingCart(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID,
				CartOrderRepository.FindCartOrder.BY_CART_GUID)).thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart)).thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(promotionRepository.getAppliedCartLineitemPromotions(shoppingCart, shoppingCartPricingSnapshot, ResourceTestConstants.LINE_ITEM_ID))
				.thenReturn(Collections.emptyList());

		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void verifyGetElementsReturnPromotionIdentifiers() {
		List<String> appliedPromotions = new ArrayList<>();
		for (int i = 0; i < NUM_OF_ID; i++) {
			appliedPromotions.add(String.valueOf(i));
		}

		when(cartOrderRepository.getEnrichedShoppingCart(ResourceTestConstants.SCOPE, ResourceTestConstants.CART_ID,
				CartOrderRepository.FindCartOrder.BY_CART_GUID)).thenReturn(Single.just(shoppingCart));
		when(pricingSnapshotRepository.getShoppingCartPricingSnapshot(shoppingCart)).thenReturn(Single.just(shoppingCartPricingSnapshot));
		when(promotionRepository.getAppliedCartLineitemPromotions(shoppingCart, shoppingCartPricingSnapshot, ResourceTestConstants.LINE_ITEM_ID))
				.thenReturn(appliedPromotions);

		repository.getElements(identifier)
				.test()
				.assertNoErrors()
				.assertValueCount(NUM_OF_ID)
				.assertValueAt(0, promotionIdentifier -> promotionIdentifier.getPromotionId().getValue().equals("0"))
				.assertValueAt(1, promotionIdentifier -> promotionIdentifier.getPromotionId().getValue().equals("1"));
	}
}
