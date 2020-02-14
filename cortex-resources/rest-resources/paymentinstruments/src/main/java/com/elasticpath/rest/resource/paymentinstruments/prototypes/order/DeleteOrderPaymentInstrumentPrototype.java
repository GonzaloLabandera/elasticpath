/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Order Payment Instrument - Delete Operation.
 */
public class DeleteOrderPaymentInstrumentPrototype implements OrderPaymentInstrumentResource.Delete {

	private final OrderPaymentInstrumentIdentifier orderPaymentInstrumentIdentifier;
	private final Repository<OrderPaymentInstrumentEntity, OrderPaymentInstrumentIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentInstrumentIdentifier identifier
	 * @param repository                       repository
	 */
	@Inject
	public DeleteOrderPaymentInstrumentPrototype(
			@RequestIdentifier final OrderPaymentInstrumentIdentifier orderPaymentInstrumentIdentifier,
			@ResourceRepository final Repository<OrderPaymentInstrumentEntity, OrderPaymentInstrumentIdentifier> repository) {
		this.orderPaymentInstrumentIdentifier = orderPaymentInstrumentIdentifier;
		this.repository = repository;
	}

	@Override
	public Completable onDelete() {
		return repository.delete(orderPaymentInstrumentIdentifier);
	}
}
