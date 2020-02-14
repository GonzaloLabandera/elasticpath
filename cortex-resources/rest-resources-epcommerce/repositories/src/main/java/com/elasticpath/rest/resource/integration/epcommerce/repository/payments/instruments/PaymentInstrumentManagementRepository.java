/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments;

import io.reactivex.Single;

import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Payment instrument management repository.
 */
public interface PaymentInstrumentManagementRepository {

	/**
	 * Retrieve the payment instrument with the given guid.
	 *
	 * @param corePaymentInstrumentId the guid
	 * @return {@link PaymentInstrumentDTO} mapped from the entity
	 */
	Single<PaymentInstrumentDTO> getPaymentInstrumentByGuid(String corePaymentInstrumentId);

	/**
	 * Retrieve the payment instrument with the given order payment instrument guid.
	 *
	 * @param orderPaymentInstrumentGuid order payment instrument guid.
	 * @return {@link PaymentInstrumentDTO} mapped from the entity
	 */
	Single<PaymentInstrumentDTO> getPaymentInstrumentByOrderPaymentInstrumentGuid(String orderPaymentInstrumentGuid);
}
