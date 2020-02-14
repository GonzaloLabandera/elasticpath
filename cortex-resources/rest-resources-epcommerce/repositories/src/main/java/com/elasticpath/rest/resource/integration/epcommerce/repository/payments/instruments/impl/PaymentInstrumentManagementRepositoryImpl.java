/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;

import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.instruments.PaymentInstrumentManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.management.PaymentInstrumentManagementService;

/**
 * The implementation of {@link PaymentInstrumentManagementRepository} related operations.
 */
@Singleton
@Named("paymentInstrumentManagementRepository")
public class PaymentInstrumentManagementRepositoryImpl implements PaymentInstrumentManagementRepository {

	@Inject
	@Named("paymentInstrumentManagementService")
	private PaymentInstrumentManagementService paymentInstrumentManagementService;

	@Inject
	@Named("reactiveAdapter")
	private ReactiveAdapter reactiveAdapter;

	@Override
	@CacheResult
	public Single<PaymentInstrumentDTO> getPaymentInstrumentByGuid(final String corePaymentInstrumentId) {
		return reactiveAdapter.fromServiceAsSingle(() -> paymentInstrumentManagementService.getPaymentInstrument(corePaymentInstrumentId),
				"No payment instrument found for guid " + corePaymentInstrumentId + ".");
	}

	@Override
	@CacheResult(uniqueIdentifier = "getByOrderPaymentInstrumentGuid")
	public Single<PaymentInstrumentDTO> getPaymentInstrumentByOrderPaymentInstrumentGuid(final String orderPaymentInstrumentGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> paymentInstrumentManagementService
						.findByOrderPaymentInstrumentGuid(orderPaymentInstrumentGuid),
				"No payment instrument found for order payment instrument guid " + orderPaymentInstrumentGuid + ".");
	}

	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
