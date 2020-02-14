/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.definition.paymentinstructions.OrderPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.OrderPaymentInstructionsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Order Request Instructions prototype for Read operation.
 */
public class ReadOrderPaymentInstructionsPrototype implements OrderPaymentInstructionsResource.Read {

	private final OrderPaymentInstructionsIdentifier orderPaymentInstructionsIdentifier;
	private final Repository<InstructionsEntity, OrderPaymentInstructionsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentInstructionsIdentifier {@link OrderPaymentInstructionsIdentifier}
	 * @param repository                         the corresponding identifier-entity repository
	 */
	@Inject
	public ReadOrderPaymentInstructionsPrototype(@RequestIdentifier final OrderPaymentInstructionsIdentifier orderPaymentInstructionsIdentifier,
												 @ResourceRepository final Repository<InstructionsEntity, OrderPaymentInstructionsIdentifier>
														 repository) {
		this.orderPaymentInstructionsIdentifier = orderPaymentInstructionsIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<InstructionsEntity> onRead() {
		return repository.findOne(orderPaymentInstructionsIdentifier);
	}
}