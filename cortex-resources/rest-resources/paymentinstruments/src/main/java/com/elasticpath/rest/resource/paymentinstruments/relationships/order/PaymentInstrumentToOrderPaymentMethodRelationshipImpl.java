/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.relationships.order;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.PaymentInstrumentToOrderPaymentMethodRelationship;
import com.elasticpath.rest.definition.paymentmethods.OrderPaymentMethodIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentToPaymentMethodLinkRepository;

/**
 * Payment Instrument to Order Payment Method link.
 */
public class PaymentInstrumentToOrderPaymentMethodRelationshipImpl implements PaymentInstrumentToOrderPaymentMethodRelationship.LinkTo {

	private final OrderPaymentInstrumentIdentifier orderPaymentInstrumentIdentifier;
	private final PaymentInstrumentToPaymentMethodLinkRepository repository;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentInstrumentIdentifier order payment instrument identifier.
	 * @param repository                       repository
	 */
	@Inject
	public PaymentInstrumentToOrderPaymentMethodRelationshipImpl(
			@RequestIdentifier final OrderPaymentInstrumentIdentifier orderPaymentInstrumentIdentifier,
			@ResourceRepository final PaymentInstrumentToPaymentMethodLinkRepository repository) {
		this.orderPaymentInstrumentIdentifier = orderPaymentInstrumentIdentifier;
		this.repository = repository;
	}

	/**
	 * Link generator.
	 *
	 * @return OrderPaymentMethodIdentifier.
	 */
	@Override
	public Observable<OrderPaymentMethodIdentifier> onLinkTo() {
		return repository.getOrderPaymentMethodIdentifier(orderPaymentInstrumentIdentifier);
	}
}
