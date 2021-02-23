/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentmethods.AccountPaymentMethodResource;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account Payment Method prototype for Read operation.
 */
public class ReadAccountPaymentMethodPrototype implements AccountPaymentMethodResource.Read {

	private final AccountPaymentMethodIdentifier methodIdentifier;
	private final Repository<PaymentMethodEntity, AccountPaymentMethodIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param methodIdentifier account payment method identifier
	 * @param repository       repository
	 */
	@Inject
	public ReadAccountPaymentMethodPrototype(@RequestIdentifier final AccountPaymentMethodIdentifier methodIdentifier,
											 @ResourceRepository final Repository<PaymentMethodEntity, AccountPaymentMethodIdentifier> repository) {
		this.methodIdentifier = methodIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentMethodEntity> onRead() {
		return repository.findOne(methodIdentifier);
	}
}
