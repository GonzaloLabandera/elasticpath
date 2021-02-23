/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.price.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.shoppingcart.PriceCalculator;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.prices.CartLineItemPriceEntity;
import com.elasticpath.rest.definition.prices.PriceForCartLineItemIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;

/**
 * Repository that implements reading price for a cart lineitem.
 *
 * @param <E> extends CartLineItemPriceEntity
 * @param <I> extends PriceForCartLineItemIdentifier
 */
@Component
public class PriceForCartLineItemEntityRepositoryImpl<E extends CartLineItemPriceEntity, I extends PriceForCartLineItemIdentifier>
		implements Repository<CartLineItemPriceEntity, PriceForCartLineItemIdentifier> {

	private MoneyWrapperTransformer moneyWrapperTransformer;
	private CartTotalsCalculator cartTotalsCalculator;

	@Override
	public Single<CartLineItemPriceEntity> findOne(final PriceForCartLineItemIdentifier priceForCartLineItemIdentifier) {
		CartIdentifier cartIdentifier = priceForCartLineItemIdentifier.getLineItem().getLineItems().getCart();
		String cartScope = cartIdentifier.getCarts().getScope().getValue();
		String cartGuid = cartIdentifier.getCartId().getValue();
		String cartItemGuid = priceForCartLineItemIdentifier.getLineItem().getLineItemId().getValue();
		return cartTotalsCalculator.getShoppingItemPricingSnapshot(cartScope, cartGuid, cartItemGuid)
				.flatMap(this::buildCartLineItemPriceEntitySingle);
	}

	private Single<CartLineItemPriceEntity> buildCartLineItemPriceEntitySingle(final ShoppingItemPricingSnapshot shoppingItemPricingSnapshot) {
		PriceCalculator priceCalculator = shoppingItemPricingSnapshot.getPriceCalc().forUnitPrice();
		Money listPrice = shoppingItemPricingSnapshot.getListUnitPrice();
		Money purchasePrice = priceCalculator.withCartDiscounts().getMoney();
		return getCartLineItemPriceEntitySingle(listPrice, purchasePrice);
	}

	private Single<CartLineItemPriceEntity> getCartLineItemPriceEntitySingle(final Money listPrice, final Money purchasePrice) {
		MoneyWrapper lineItemPrice = new MoneyWrapper();
		lineItemPrice.setListPrice(listPrice);
		lineItemPrice.setPurchasePrice(purchasePrice);
		return Single.just(moneyWrapperTransformer.transformToEntity(lineItemPrice));
	}

	@Reference
	public void setMoneyWrapperTransformer(final MoneyWrapperTransformer moneyWrapperTransformer) {
		this.moneyWrapperTransformer = moneyWrapperTransformer;
	}

	@Reference
	public void setCartTotalsCalculator(final CartTotalsCalculator cartTotalsCalculator) {
		this.cartTotalsCalculator = cartTotalsCalculator;
	}
}
