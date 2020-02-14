/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Order-Payment Instrument Form.
 */
public class ReadOrderPaymentInstrumentFormPrototype implements OrderPaymentInstrumentFormResource.Read {

	private final OrderPaymentInstrumentFormIdentifier orderPaymentInstrumentFormIdentifier;
	private final Repository<OrderPaymentInstrumentForFormEntity, OrderPaymentInstrumentFormIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param orderPaymentInstrumentFormIdentifier identifier
	 * @param repository                           repository
	 */
	@Inject
	public ReadOrderPaymentInstrumentFormPrototype(
			@RequestIdentifier final OrderPaymentInstrumentFormIdentifier orderPaymentInstrumentFormIdentifier,
			@ResourceRepository final Repository<OrderPaymentInstrumentForFormEntity, OrderPaymentInstrumentFormIdentifier> repository) {
		this.orderPaymentInstrumentFormIdentifier = orderPaymentInstrumentFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<OrderPaymentInstrumentForFormEntity> onRead() {
		return repository.findOne(orderPaymentInstrumentFormIdentifier);
	}
}
