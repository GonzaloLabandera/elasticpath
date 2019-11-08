/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import com.google.common.annotations.VisibleForTesting;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.springframework.core.convert.ConversionService;

import com.elasticpath.domain.order.Order;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.purchases.PurchaseEntity;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;

/**
 * Purchases Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class PurchaseEntityRepositoryImpl<E extends PurchaseEntity, I extends PurchaseIdentifier>
		implements Repository<PurchaseEntity, PurchaseIdentifier> {

	/**
	 * Error message for non purchasable order.
	 */
	@VisibleForTesting
	static final String NOT_PURCHASABLE = "The products selected are not purchasable";

	private OrderRepository orderRepository;

	private ResourceOperationContext resourceOperationContext;

	private ConversionService conversionService;

	@Override
	public Single<PurchaseEntity> findOne(final PurchaseIdentifier identifier) {
		String scope = identifier.getPurchases().getScope().getValue();
		String purchaseId = identifier.getPurchaseId().getValue();
		return orderRepository.findByGuidAsSingle(scope, purchaseId)
				.map(this::convertOrderToPurchaseEntity);
	}

	@Override
	public Observable<PurchaseIdentifier> findAll(final IdentifierPart<String> scope) {
		String userId = resourceOperationContext.getUserIdentifier();
		return orderRepository.findOrderIdsByCustomerGuid(scope.getValue(), userId)
				.map(purchaseId -> PurchaseIdentifier.builder()
						.withPurchases(PurchasesIdentifier.builder()
								.withScope(scope)
								.build())
						.withPurchaseId(StringIdentifier.of(purchaseId))
						.build());
	}

	/**
	 * Convert order to purchase entity.
	 *
	 * @param order order
	 * @return purchase entity
	 */
	protected PurchaseEntity convertOrderToPurchaseEntity(final Order order) {
		return conversionService.convert(order, PurchaseEntity.class);
	}

	@Reference
	public void setOrderRepository(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

	@Reference
	public void setResourceOperationContext(final ResourceOperationContext resourceOperationContext) {
		this.resourceOperationContext = resourceOperationContext;
	}

	@Reference
	public void setConversionService(final ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}
