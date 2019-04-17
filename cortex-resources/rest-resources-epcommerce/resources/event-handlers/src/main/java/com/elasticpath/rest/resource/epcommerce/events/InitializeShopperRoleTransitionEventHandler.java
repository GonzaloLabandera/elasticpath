/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.epcommerce.events;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.rest.relos.rs.events.RoleTransitionEvent;
import com.elasticpath.rest.relos.rs.events.ScopedEventEntityHandler;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.ShoppingCartRepository;

/**
 * Initialize the shopper and shopping cart.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=200",
		"eventType=" + RoleTransitionEvent.EVENT_TYPE })
public class InitializeShopperRoleTransitionEventHandler implements ScopedEventEntityHandler<RoleTransitionEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(InitializeShopperRoleTransitionEventHandler.class);

	@Reference
	private ShoppingCartRepository shoppingCartRepository;

	@Override
	public void handleEvent(final String scope, final RoleTransitionEvent event) {
			final String customerGuid = event.getNewUserGuid();
			shoppingCartRepository.getShoppingCartForCustomer(customerGuid).subscribe(
					cart -> { },
					error -> LOG.error("Error initializing shopper for customer guid: {}", customerGuid));
	}

}
