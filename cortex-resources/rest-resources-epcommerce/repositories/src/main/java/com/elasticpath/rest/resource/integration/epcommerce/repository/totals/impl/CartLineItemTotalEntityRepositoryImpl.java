/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.totals.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.money.Money;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.carts.CartIdentifier;
import com.elasticpath.rest.definition.carts.LineItemIdentifier;
import com.elasticpath.rest.definition.totals.CartLineItemTotalIdentifier;
import com.elasticpath.rest.definition.totals.TotalEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;

/**
 * Cart Line Item Total Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CartLineItemTotalEntityRepositoryImpl<E extends TotalEntity, I extends CartLineItemTotalIdentifier>
		implements Repository<TotalEntity, CartLineItemTotalIdentifier> {

	private CartTotalsCalculator cartTotalsCalculator;
	private ConversionService conversionService;

	@Override
	public Single<TotalEntity> findOne(final CartLineItemTotalIdentifier cartLineItemTotalIdentifier) {
		final LineItemIdentifier lineItemIdentifier = cartLineItemTotalIdentifier.getLineItem();
		final CartIdentifier cartIdentifier = lineItemIdentifier.getLineItems().getCart();

		String lineItemId = lineItemIdentifier.getLineItemId().getValue();
		String cartId = cartIdentifier.getCartId().getValue();
		String scope = cartIdentifier.getCarts().getScope().getValue();

		return cartTotalsCalculator.calculateTotalForShoppingItem(scope, cartId, lineItemId)
				.map(this::convertMoneyToTotalEntity);
	}

	/**
	 * Converts given Money to TotalEntity.
	 *
	 * @param money money to convert
	 * @return the converted total entity
	 */
	protected TotalEntity convertMoneyToTotalEntity(final Money money) {
		return conversionService.convert(money, TotalEntity.class);
	}

	@Reference
	public void setCartTotalsCalculator(final CartTotalsCalculator cartTotalsCalculator) {
		this.cartTotalsCalculator = cartTotalsCalculator;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
