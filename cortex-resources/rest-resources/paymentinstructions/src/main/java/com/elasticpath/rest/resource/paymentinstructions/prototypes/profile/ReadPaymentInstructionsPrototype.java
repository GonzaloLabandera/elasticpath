/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.profile;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.definition.paymentinstructions.PaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.PaymentInstructionsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Request Instructions Form prototype for Read operation.
 */
public class ReadPaymentInstructionsPrototype implements PaymentInstructionsResource.Read {

	private final PaymentInstructionsIdentifier paymentInstructionsIdentifier;
	private final Repository<InstructionsEntity, PaymentInstructionsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param paymentInstructionsIdentifier {@link PaymentInstructionsIdentifier}
	 * @param repository                    the corresponding identifier-entity repository
	 */
	@Inject
	public ReadPaymentInstructionsPrototype(@RequestIdentifier final PaymentInstructionsIdentifier paymentInstructionsIdentifier,
											@ResourceRepository final Repository<InstructionsEntity, PaymentInstructionsIdentifier> repository) {
		this.paymentInstructionsIdentifier = paymentInstructionsIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<InstructionsEntity> onRead() {
		return repository.findOne(paymentInstructionsIdentifier);
	}
}