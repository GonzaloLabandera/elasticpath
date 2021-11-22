/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.epcommerce.events;

import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.rest.chain.Assign;
import com.elasticpath.rest.chain.ExecutionResultChain;
import com.elasticpath.rest.command.ExecutionResult;
import com.elasticpath.rest.relos.rs.events.RoleTransitionEvent;
import com.elasticpath.rest.relos.rs.events.ScopedEventEntityHandler;
import com.elasticpath.rest.resource.integration.epcommerce.common.authentication.AuthenticationConstants;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.ShopperRepository;
import com.elasticpath.service.shoppingcart.ShoppingCartService;

/**
 * Merges the customer session on transition from public to registered role.
 */
@Component(property = {
		Constants.SERVICE_RANKING + ":Integer=100",
		"eventType=" + RoleTransitionEvent.EVENT_TYPE })
public class MergeCustomerRoleTransitionEventHandler implements ScopedEventEntityHandler<RoleTransitionEvent> {

	private static final Logger LOG = LoggerFactory.getLogger(MergeCustomerRoleTransitionEventHandler.class);

	@Reference
	private CustomerRepository customerRepository;

	@Reference
	private ShopperRepository shopperRepository;

	@Reference
	private ShoppingCartService shoppingCartService;

	@Override
	public void handleEvent(final String scope, final RoleTransitionEvent event) {
		if (isPublicToRegisteredTransition(event)) {
			String anonymousUserGuid = event.getOldUserGuid();
			String registeredUserGuid = event.getNewUserGuid();
			ExecutionResult<Void> mergeResult = mergeCustomerSession(scope, anonymousUserGuid, registeredUserGuid);

			if (mergeResult.isFailure()) {
				LOG.error("Error merging cart: {}", mergeResult.getErrorMessage());
			}
		}
	}

	private boolean isPublicToRegisteredTransition(final RoleTransitionEvent event) {
		return AuthenticationConstants.PUBLIC_ROLENAME.equals(event.getOldRole())
				&& AuthenticationConstants.REGISTERED_ROLE.equals(event.getNewRole());
	}

	private ExecutionResult<Void> mergeCustomerSession(final String storeCode, final String anonymousCustomerGuid,
														final String registeredCustomerGuid) {
		return new ExecutionResultChain() {
			public ExecutionResult<?> build() {
				Shopper anonymousShopper = shopperRepository.findOrCreateShopper(anonymousCustomerGuid, storeCode).blockingGet();
				anonymousShopper.setCurrentShoppingCart(shoppingCartService.findOrCreateDefaultCartByShopper(anonymousShopper));
				Customer registeredCustomer = Assign.ifSuccessful(customerRepository.findCustomerByGuidAndStoreCode(registeredCustomerGuid,
						storeCode));

				return customerRepository.mergeCustomer(anonymousShopper, registeredCustomer, storeCode);
			}
		}.execute();
	}
}
