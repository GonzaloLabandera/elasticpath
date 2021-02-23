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
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.totals.OrderTotalIdentifier;
import com.elasticpath.rest.definition.totals.TotalEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;

/**
 * Order Total Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class OrderTotalEntityRepositoryImpl<E extends TotalEntity, I extends OrderTotalIdentifier>
		implements Repository<TotalEntity, OrderTotalIdentifier> {

	private CartTotalsCalculator cartTotalsCalculator;
	private ConversionService conversionService;

	@Override
	public Single<TotalEntity> findOne(final OrderTotalIdentifier orderTotalIdentifier) {
		final OrderIdentifier orderIdentifier = orderTotalIdentifier.getOrder();
		String scope = orderIdentifier.getScope().getValue();
		String orderId = orderIdentifier.getOrderId().getValue();

		return cartTotalsCalculator.calculateTotalForCartOrder(scope, orderId)
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
