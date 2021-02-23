/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.wiring;

import static org.ops4j.peaberry.Peaberry.service;

import javax.inject.Named;

import com.google.inject.multibindings.MapBinder;

import com.elasticpath.rest.authorization.parameter.PermissionParameterStrategy;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.addresses.AddressIdentifier;
import com.elasticpath.rest.definition.addresses.AddressesResource;
import com.elasticpath.rest.helix.api.AbstractHelixModule;
import com.elasticpath.rest.resource.accounts.permissions.AccountIdParameterStrategy;
import com.elasticpath.rest.resource.addresses.permissions.AddressIdParameterStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.addresses.AddressRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Peaberry plumbing class used for importing required services and registering additional parameter resolvers.
 */
@Named
public class AddressesWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return AddressesResource.FAMILY;
	}

	@Override
	protected void registerParameterResolvers(final MapBinder<String, PermissionParameterStrategy> resolvers) {
		super.registerParameterResolvers(resolvers);
		resolvers.addBinding(AddressIdentifier.ADDRESS_ID).toInstance(new AddressIdParameterStrategy());
		resolvers.addBinding(AccountIdentifier.ACCOUNT_ID).toInstance(new AccountIdParameterStrategy());
	}

	@Override
	protected void configurePrototypes() {
		bind(AddressRepository.class).toProvider(service(AddressRepository.class).single());
		bind(CustomerRepository.class).toProvider(service(CustomerRepository.class).single());
	}
}
