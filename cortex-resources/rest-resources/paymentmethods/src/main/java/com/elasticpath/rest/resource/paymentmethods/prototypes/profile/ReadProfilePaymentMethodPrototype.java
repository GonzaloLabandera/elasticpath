/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.ProfilePaymentMethodResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Profile Payment Method prototype for Read operation.
 */
public class ReadProfilePaymentMethodPrototype implements ProfilePaymentMethodResource.Read {

	private final ProfilePaymentMethodIdentifier methodIdentifier;

	private final Repository<PaymentMethodEntity, ProfilePaymentMethodIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param methodIdentifier profile payment method identifier
	 * @param repository       repository
	 */
	@Inject
	public ReadProfilePaymentMethodPrototype(@RequestIdentifier final ProfilePaymentMethodIdentifier methodIdentifier,
											 @ResourceRepository final Repository<PaymentMethodEntity, ProfilePaymentMethodIdentifier> repository) {
		this.methodIdentifier = methodIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentMethodEntity> onRead() {
		return repository.findOne(methodIdentifier);
	}
}
