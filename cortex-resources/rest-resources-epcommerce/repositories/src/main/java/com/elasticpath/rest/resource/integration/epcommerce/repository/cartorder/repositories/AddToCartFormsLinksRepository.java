/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import java.util.stream.Collectors;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.AddToCartFormsIdentifier;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.items.ItemIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;

/**
 * Links repository for itemIdentifier to add-to-cart list identifier.
 *
 * @param <E> the item identifier.
 * @param <I> the add-to cart identifier
 */
@Component
public class AddToCartFormsLinksRepository<E extends ItemIdentifier,
		I extends AddToCartFormsIdentifier> implements LinksRepository<ItemIdentifier, AddToCartFormsIdentifier> {


	@Reference(name = "resourceOperationContext")
	private ResourceOperationContext resourceOperationContext;

	@Reference(name = "multicartResolutionStrategyListHolder")
	private MultiCartResolutionStrategyHolder holder;

	@Reference(name = "customerSessionRepository")
	private CustomerSessionRepository customerSessionRepository;


	@Override
	public Observable<AddToCartFormsIdentifier> getElements(final ItemIdentifier identifier) {
		Subject subject = resourceOperationContext.getSubject();
		CustomerSession customerSession = customerSessionRepository.findOrCreateCustomerSession().blockingGet();

		return Observable.fromIterable(holder.getStrategies().stream().filter(strategy
				-> strategy.isApplicable(subject) && strategy.supportsCreate(subject, customerSession.getShopper(),
				identifier.getScope().getValue()))
				.map(strategy -> AddToCartFormsIdentifier.builder()
						.withCarts(CartsIdentifier.builder()
								.withScope(identifier.getScope())
								.build())
						.withItem(identifier)
						.build()).collect(Collectors.toSet()));
	}
}
