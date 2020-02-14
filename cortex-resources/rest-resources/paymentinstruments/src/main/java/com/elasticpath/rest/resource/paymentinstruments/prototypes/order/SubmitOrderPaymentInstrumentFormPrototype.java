/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.paymentinstruments.prototypes.order;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentForFormEntity;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentFormResource;
import com.elasticpath.rest.definition.paymentinstruments.OrderPaymentInstrumentIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentRepository;

/**
 * Prototype for submission of Order-Payment Instrument form.
 */
public class SubmitOrderPaymentInstrumentFormPrototype implements OrderPaymentInstrumentFormResource.SubmitWithResult {

	private final OrderPaymentInstrumentForFormEntity formEntity;
	private final IdentifierPart<String> scope;
	private final PaymentInstrumentRepository repository;

	/**
	 * Constructor.
	 *
	 * @param formEntity form entity
	 * @param scope      scope
	 * @param repository payment instrument repository
	 */
	@Inject
	public SubmitOrderPaymentInstrumentFormPrototype(
			@RequestForm final OrderPaymentInstrumentForFormEntity formEntity,
			@UriPart(OrderIdentifier.SCOPE) final IdentifierPart<String> scope,
			@ResourceRepository final PaymentInstrumentRepository repository) {
		this.formEntity = formEntity;
		this.scope = scope;
		this.repository = repository;
	}

	@Override
	public Single<SubmitResult<OrderPaymentInstrumentIdentifier>> onSubmitWithResult() {
		return repository.submitOrderPaymentInstrument(scope, formEntity);
	}
}
