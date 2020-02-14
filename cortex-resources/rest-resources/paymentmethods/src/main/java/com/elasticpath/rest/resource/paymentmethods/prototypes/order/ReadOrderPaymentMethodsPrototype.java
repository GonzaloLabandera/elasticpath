/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodsResource;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * ORder Payment Methods prototype for Read operation.
 */
public class ReadOrderPaymentMethodsPrototype implements OrderPaymentMethodsResource.Read {

	private final IdentifierPart<String> scope;

	private final Repository<PaymentMethodEntity, OrderPaymentMethodIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param scope      scope
	 * @param repository repository
	 */
	@Inject
	public ReadOrderPaymentMethodsPrototype(@UriPart(ProfileIdentifier.SCOPE) final IdentifierPart<String> scope,
											@ResourceRepository final Repository<PaymentMethodEntity, OrderPaymentMethodIdentifier> repository) {
		this.scope = scope;
		this.repository = repository;
	}

	@Override
	public Observable<OrderPaymentMethodIdentifier> onRead() {
		return repository.findAll(scope);
	}
}
