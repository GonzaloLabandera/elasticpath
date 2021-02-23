/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstruments.wiring;

import static org.ops4j.peaberry.Peaberry.service;

import javax.inject.Named;

import com.google.inject.multibindings.MapBinder;

import com.elasticpath.rest.authorization.parameter.PermissionParameterStrategy;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentsResource;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.api.AbstractHelixModule;
import com.elasticpath.rest.resource.accounts.permissions.AccountIdParameterStrategy;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.CustomerRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;
import com.elasticpath.rest.resource.paymentinstruments.permissions.PaymentInstrumentIdParameterStrategy;
import com.elasticpath.rest.resource.profiles.permissions.ProfileIdParameterStrategy;

/**
 * Peaberry plumbing class used for importing required services and registering additional parameter resolvers.
 */
@Named
public class PaymentInstrumentsWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return PaymentInstrumentsResource.FAMILY;
	}

	@Override
	protected void registerParameterResolvers(final MapBinder<String, PermissionParameterStrategy> resolvers) {
		super.registerParameterResolvers(resolvers);
		resolvers.addBinding(PaymentInstrumentIdentifier.PAYMENT_INSTRUMENT_ID).toInstance(new PaymentInstrumentIdParameterStrategy());
		resolvers.addBinding(ProfileIdentifier.PROFILE_ID).toInstance(new ProfileIdParameterStrategy());
		resolvers.addBinding(AccountIdentifier.ACCOUNT_ID).toInstance(new AccountIdParameterStrategy());
	}

	@Override
	protected void configurePrototypes() {
		bind(PaymentInstrumentRepository.class).toProvider(service(PaymentInstrumentRepository.class).single());
		bind(CustomerRepository.class).toProvider(service(CustomerRepository.class).single());
	}

}