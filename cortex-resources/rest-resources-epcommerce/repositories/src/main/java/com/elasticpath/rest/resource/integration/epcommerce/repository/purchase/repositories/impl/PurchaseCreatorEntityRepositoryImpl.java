/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.purchases.PurchaseCreatorEntity;
import com.elasticpath.rest.definition.purchases.PurchaseCreatorIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;

/**
 * Purchase Creator Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class PurchaseCreatorEntityRepositoryImpl<E extends PurchaseCreatorEntity, I extends PurchaseCreatorIdentifier>
		implements Repository<PurchaseCreatorEntity, PurchaseCreatorIdentifier> {

	private OrderRepository orderRepository;

	@Override
	public Single<PurchaseCreatorEntity> findOne(final PurchaseCreatorIdentifier identifier) {
		String purchaseId = identifier.getPurchase().getPurchaseId().getValue();
		Customer customer = orderRepository.getCustomerByOrderNumber(purchaseId);
		return Single.just(PurchaseCreatorEntity.builder()
				.withUserEmail(customer.getEmail())
				.withUserFullName(customer.getFullName()).build());
	}

	@Reference
	public void setOrderRepository(final OrderRepository orderRepository) {
		this.orderRepository = orderRepository;
	}

}
