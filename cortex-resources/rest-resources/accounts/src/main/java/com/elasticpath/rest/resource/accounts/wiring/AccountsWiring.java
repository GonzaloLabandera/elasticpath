/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.wiring;

import static org.ops4j.peaberry.Peaberry.service;

import javax.inject.Named;

import com.google.inject.multibindings.MapBinder;

import com.elasticpath.rest.authorization.parameter.PermissionParameterStrategy;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsResource;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.helix.api.AbstractHelixModule;
import com.elasticpath.rest.resource.accounts.permissions.AccountIdParameterStrategy;
import com.elasticpath.rest.resource.accounts.permissions.AssociateIdParameterStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;

/**
 * Accounts Wiring.
 */
@Named
public class AccountsWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return AccountsResource.FAMILY;
	}

	@Override
	protected void registerParameterResolvers(final MapBinder<String, PermissionParameterStrategy> resolvers) {
		super.registerParameterResolvers(resolvers);
		resolvers.addBinding(AccountIdentifier.ACCOUNT_ID).toInstance(new AccountIdParameterStrategy());
		resolvers.addBinding(AssociateIdentifier.ASSOCIATE_ID).toInstance(new AssociateIdParameterStrategy());
	}

	@Override
	protected void configurePrototypes() {
		bind(CustomerRepository.class).toProvider(service(CustomerRepository.class).single());
	}
}
