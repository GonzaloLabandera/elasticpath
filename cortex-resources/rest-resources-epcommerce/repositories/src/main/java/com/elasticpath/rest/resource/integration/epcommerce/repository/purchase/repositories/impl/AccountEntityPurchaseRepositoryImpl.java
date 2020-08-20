/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.purchase.repositories.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.UserAccountAssociation;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.definition.purchases.PurchasesIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.order.OrderRepository;
import com.elasticpath.service.customer.UserAccountAssociationService;

/**
 * Purchases Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class AccountEntityPurchaseRepositoryImpl<E extends AccountEntity, I extends PurchaseIdentifier>
		implements Repository<AccountEntity, PurchaseIdentifier> {

	private OrderRepository orderRepository;

	private ResourceOperationContext resourceOperationContext;

	private UserAccountAssociationService userAccountAssociationService;

	@Override
	public Observable<PurchaseIdentifier> findAll(final IdentifierPart<String> scope) {
		List<String> orderIdsArray = new ArrayList<>();
		orderRepository.findOrderIdsByCustomerGuid(scope.getValue(),
				resourceOperationContext.getUserIdentifier()).blockingIterable()
				.forEach(orderIdsArray::add);

		Collection<UserAccountAssociation> associationsForUser =
				userAccountAssociationService.findAssociationsForUser(resourceOperationContext.getUserIdentifier());
		associationsForUser.forEach(userAccountAssociation ->
				orderRepository.findOrderIdsByAccountGuid(scope.getValue(),
						userAccountAssociation.getAccountGuid(),
						0,
						Integer.MAX_VALUE)
						.blockingIterable().forEach(orderIdsArray::add));

		return Observable.fromIterable(getPurchaseIdentifiers(scope, orderIdsArray));
	}

	private List<PurchaseIdentifier> getPurchaseIdentifiers(final IdentifierPart<String> scope, final List<String> orderIdsArray) {
		List<PurchaseIdentifier> orderPurchasesArray;

		orderPurchasesArray = orderIdsArray.stream().map(orderId -> PurchaseIdentifier.builder()
				.withPurchaseId(StringIdentifier.of(orderId))
				.withPurchases(PurchasesIdentifier.builder().withScope(scope).build())
				.build()).collect(Collectors.toList());
		return orderPurchasesArray;
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
	public void setUserAccountAssociationService(final UserAccountAssociationService userAccountAssociationService) {
		this.userAccountAssociationService = userAccountAssociationService;
	}
}
