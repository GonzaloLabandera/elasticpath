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
import com.elasticpath.rest.definition.totals.CartTotalIdentifier;
import com.elasticpath.rest.definition.totals.TotalEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;

/**
 * Cart Total Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CartTotalEntityRepositoryImpl<E extends TotalEntity, I extends CartTotalIdentifier>
		implements Repository<TotalEntity, CartTotalIdentifier> {

	private CartTotalsCalculator cartTotalsCalculator;
	private ConversionService conversionService;

	@Override
	public Single<TotalEntity> findOne(final CartTotalIdentifier cartTotalIdentifier) {
		final CartIdentifier cartIdentifier = cartTotalIdentifier.getCart();

		String scope = cartIdentifier.getCarts().getScope().getValue();
		String cartID = cartIdentifier.getCartId().getValue();

		return cartTotalsCalculator.calculateTotalForShoppingCart(scope, cartID)
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
