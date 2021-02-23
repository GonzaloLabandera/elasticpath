/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.taxes.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.taxes.OrderTaxIdentifier;
import com.elasticpath.rest.definition.taxes.TaxesEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.calc.CartTotalsCalculator;
import com.elasticpath.service.tax.TaxCalculationResult;

/**
 * Repository for Order Tax Entity.
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class OrderTaxEntityRepositoryImpl<E extends TaxesEntity, I extends OrderTaxIdentifier>
		implements Repository<TaxesEntity, OrderTaxIdentifier> {

	private ConversionService conversionService;
	private CartTotalsCalculator cartTotalsCalculator;

	@Override
	public Single<TaxesEntity> findOne(final OrderTaxIdentifier identifier) {

		String scope = identifier.getOrder().getScope().getValue();
		String orderId = identifier.getOrder().getOrderId().getValue();

		return cartTotalsCalculator.calculateTax(scope, orderId)
				.map(this::convertTaxCalculationToTaxesEntity);
	}

	@Reference
	public void setCartTotalsCalculator(final CartTotalsCalculator cartTotalsCalculator) {
		this.cartTotalsCalculator = cartTotalsCalculator;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	/**
	 * Converts given tax calculation to taxes entity.
	 *
	 * @param taxCalculationResult tax calculation
	 * @return taxes entity
	 */
	protected TaxesEntity convertTaxCalculationToTaxesEntity(final TaxCalculationResult taxCalculationResult) {
		return conversionService.convert(taxCalculationResult, TaxesEntity.class);
	}
}
