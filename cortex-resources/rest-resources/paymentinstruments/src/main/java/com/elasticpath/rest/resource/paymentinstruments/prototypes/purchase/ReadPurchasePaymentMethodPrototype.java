/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.purchase;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentMethodIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PurchasePaymentMethodResource;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.method.PaymentMethodRepository;

/**
 * Prototype for Read operations on a Purchase Payment Method.
 */
public class ReadPurchasePaymentMethodPrototype implements PurchasePaymentMethodResource.Read {

	private final PurchasePaymentMethodIdentifier purchasePaymentMethodIdentifier;

	private final PaymentMethodRepository repository;

	/**
	 * Constructor.
	 *
	 * @param purchasePaymentMethodIdentifier identifier
	 * @param repository                  identifier-entity repository
	 */
	@Inject
	public ReadPurchasePaymentMethodPrototype(
			@RequestIdentifier final PurchasePaymentMethodIdentifier purchasePaymentMethodIdentifier,
			@ResourceRepository final PaymentMethodRepository repository) {
		this.purchasePaymentMethodIdentifier = purchasePaymentMethodIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<PaymentMethodEntity> onRead() {
		return repository.findOnePaymentMethodEntityForMethodId(purchasePaymentMethodIdentifier.getPaymentMethodId().getValue());
	}
}
