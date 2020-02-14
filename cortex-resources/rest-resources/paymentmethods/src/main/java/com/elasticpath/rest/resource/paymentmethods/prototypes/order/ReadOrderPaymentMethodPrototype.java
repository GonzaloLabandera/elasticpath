/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodResource;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Order Payment Method prototype for Read operation.
 */
public class ReadOrderPaymentMethodPrototype implements OrderPaymentMethodResource.Read {

	private final OrderPaymentMethodIdentifier methodIdentifier;

	private final Repository<PaymentMethodEntity, OrderPaymentMethodIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param methodIdentifier order payment method identifier
	 * @param repository       repository
	 */
	@Inject
	public ReadOrderPaymentMethodPrototype(@RequestIdentifier final OrderPaymentMethodIdentifier methodIdentifier,
										   @ResourceRepository final Repository<PaymentMethodEntity, OrderPaymentMethodIdentifier> repository) {
		this.methodIdentifier = methodIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentMethodEntity> onRead() {
		return repository.findOne(methodIdentifier);
	}
}
