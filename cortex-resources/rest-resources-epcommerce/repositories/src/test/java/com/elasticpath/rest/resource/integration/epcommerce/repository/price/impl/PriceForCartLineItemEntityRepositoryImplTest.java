/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CART_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LINE_ITEM_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.NOT_FOUND;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static org.mockito.Mockito.when;

import java.util.Currency;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.shoppingcart.PriceCalculator;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.base.CostEntity;
import com.elasticpath.rest.definition.prices.CartLineItemPriceEntity;
import com.elasticpath.rest.definition.prices.PriceForCartLineItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.IdentifierTestFactory;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;

/**
 * Test for {@link PriceForCartLineItemEntityRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PriceForCartLineItemEntityRepositoryImplTest {

	private final PriceForCartLineItemIdentifier priceForCartLineItemIdentifier =
			IdentifierTestFactory.buildPriceForCartLineItemIdentifier(SCOPE, CART_ID, LINE_ITEM_ID);

	@Mock
	private ShoppingItemPricingSnapshot shoppingItemPricingSnapshot;

	@Mock
	private CostEntity purchaseCostEntity;

	@Mock
	private CostEntity listCostEntity;

	@Mock
	private PriceCalculator priceCalculator;

	@InjectMocks
	private PriceForCartLineItemEntityRepositoryImpl<CartLineItemPriceEntity, PriceForCartLineItemIdentifier> repository;

	@Mock
	private MoneyWrapperTransformer moneyWrapperTransformer;

	@Mock
	private CartTotalsCalculator cartTotalsCalculator;

	@Test
	public void verifyFindOneReturnsNotFoundWhenPricingSnapshotNotFound() {
		when(cartTotalsCalculator.getShoppingItemPricingSnapshot(SCOPE, CART_ID, LINE_ITEM_ID))
				.thenReturn(Single.error(ResourceOperationFailure.notFound(NOT_FOUND)));

		repository.findOne(priceForCartLineItemIdentifier)
				.test()
				.assertError(createErrorCheckPredicate(NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void verifyFindOneReturnsCartLineItemPriceEntity() {
		Money listPrice = Money.valueOf(1, Currency.getInstance("USD"));
		Money purchasePrice = Money.valueOf(1, Currency.getInstance("USD"));
		CartLineItemPriceEntity cartLineItemPriceEntity = CartLineItemPriceEntity.builder()
				.addingPurchasePrice(purchaseCostEntity)
				.addingListPrice(listCostEntity)
				.build();

		when(cartTotalsCalculator.getShoppingItemPricingSnapshot(SCOPE, CART_ID, LINE_ITEM_ID))
				.thenReturn(Single.just(shoppingItemPricingSnapshot));
		mockListAndPurchasePrice(listPrice, purchasePrice);
		mockMoneyWrapperTransformer(listPrice, purchasePrice, cartLineItemPriceEntity);

		repository.findOne(priceForCartLineItemIdentifier)
				.test()
				.assertNoErrors()
				.assertValue(cartLineItemPriceEntity);
	}

	private void mockListAndPurchasePrice(final Money listPrice, final Money purchasePrice) {
		when(shoppingItemPricingSnapshot.getPriceCalc()).thenReturn(priceCalculator);
		when(priceCalculator.forUnitPrice()).thenReturn(priceCalculator);
		when(shoppingItemPricingSnapshot.getListUnitPrice()).thenReturn(listPrice);
		when(priceCalculator.withCartDiscounts()).thenReturn(priceCalculator);
		when(priceCalculator.getMoney()).thenReturn(purchasePrice);
	}

	private void mockMoneyWrapperTransformer(final Money listPrice, final Money purchasePrice,
											 final CartLineItemPriceEntity cartLineItemPriceEntity) {
		MoneyWrapper moneyWrapper = new MoneyWrapper();
		moneyWrapper.setListPrice(listPrice);
		moneyWrapper.setPurchasePrice(purchasePrice);
		when(moneyWrapperTransformer.transformToEntity(moneyWrapper)).thenReturn(cartLineItemPriceEntity);
	}
}
