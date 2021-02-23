/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.wiring;

import static org.ops4j.peaberry.Peaberry.service;

import javax.inject.Named;

import com.google.inject.multibindings.MapBinder;

import com.elasticpath.rest.authorization.parameter.PermissionParameterStrategy;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodsResource;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.api.AbstractHelixModule;
import com.elasticpath.rest.resource.accounts.permissions.AccountIdParameterStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.profiles.permissions.ProfileIdParameterStrategy;

/**
 * Peaberry plumbing class used for importing required services and registering additional parameter resolvers.
 */
@Named
public class PaymentMethodsWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return ProfilePaymentMethodsResource.FAMILY;
	}

	@Override
	protected void registerParameterResolvers(final MapBinder<String, PermissionParameterStrategy> resolvers) {
		super.registerParameterResolvers(resolvers);
		resolvers.addBinding(ProfileIdentifier.PROFILE_ID).toInstance(new ProfileIdParameterStrategy());
		resolvers.addBinding(AccountIdentifier.ACCOUNT_ID).toInstance(new AccountIdParameterStrategy());

	}

	@Override
	protected void configurePrototypes() {
		bind(CustomerRepository.class).toProvider(service(CustomerRepository.class).single());
	}
}