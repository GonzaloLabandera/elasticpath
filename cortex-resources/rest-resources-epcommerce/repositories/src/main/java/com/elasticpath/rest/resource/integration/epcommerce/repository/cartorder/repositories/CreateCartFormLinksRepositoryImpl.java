/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.repositories;

import java.util.stream.Collectors;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.carts.CartsIdentifier;
import com.elasticpath.rest.definition.carts.CreateCartFormIdentifier;
import com.elasticpath.rest.identity.Subject;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.MultiCartResolutionStrategyHolder;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerSessionRepository;

/**
 * Repository for cart.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class CreateCartFormLinksRepositoryImpl<E extends CartsIdentifier, I extends CreateCartFormIdentifier>
		implements LinksRepository<CartsIdentifier, CreateCartFormIdentifier> {


	@Reference(name = "resourceOperationContext")
	private  ResourceOperationContext resourceOperationContext;

	@Reference(name = "multicartResolutionStrategyListHolder")
	private MultiCartResolutionStrategyHolder holder;

	@Reference(name = "customerSessionRepository")
	private CustomerSessionRepository customerSessionRepository;



	@Override
	public Observable<CreateCartFormIdentifier> getElements(final CartsIdentifier identifier) {
		Subject subject = resourceOperationContext.getSubject();
		CustomerSession customerSession = customerSessionRepository.findOrCreateCustomerSessionAsSingle().blockingGet();

		return Observable.fromIterable(holder.getStrategies().stream().filter(strategy
				-> strategy.isApplicable(subject) && strategy.supportsCreate(subject, customerSession.getShopper(),
				identifier.getScope().getValue()))
				.map(strategy -> CreateCartFormIdentifier.builder()
						.withCarts(identifier).build()).collect(Collectors.toSet()));

	}
}
