/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.impl;

import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;

import com.elasticpath.domain.orderpaymentapi.impl.PICFieldsRequestContext;
import com.elasticpath.domain.orderpaymentapi.impl.PICRequestContext;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsDTO;
import com.elasticpath.provider.payment.service.instructions.PICInstructionsFieldsDTO;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentCreationFieldsDTO;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiService;

/**
 * The facade for operations with {@link com.elasticpath.service.orderpaymentapi.OrderPaymentApiService}.
 */
@Singleton
@Named("orderPaymentApiRepository")
public class OrderPaymentApiRepositoryImpl implements OrderPaymentApiRepository {

	@Inject
	@Named("orderPaymentApiService")
	private OrderPaymentApiService orderPaymentApiService;

	@Inject
	@Named("reactiveAdapter")
	private ReactiveAdapter reactiveAdapter;

	@Override
	public Single<PICInstructionsFieldsDTO> getPICInstructionsFields(final String paymentProviderConfigGuid,
																	 final PICFieldsRequestContext picFieldsRequestContext) {
		return reactiveAdapter.fromServiceAsSingle(() ->
				orderPaymentApiService.getPICInstructionsFields(paymentProviderConfigGuid, picFieldsRequestContext));
	}

	@Override
	public Single<PICInstructionsDTO> getPICInstructions(final String paymentProviderConfigGuid, final Map<String, String> formData,
														 final PICRequestContext picRequestContext) {
		return reactiveAdapter.fromServiceAsSingle(() ->
				orderPaymentApiService.getPICInstructions(paymentProviderConfigGuid, formData, picRequestContext));
	}

	@Override
	public Single<PaymentInstrumentCreationFieldsDTO> getPICFields(final String paymentProviderConfigGuid,
																   final PICFieldsRequestContext picFieldsRequestContext) {
		return reactiveAdapter.fromServiceAsSingle(() -> orderPaymentApiService.getPICFields(paymentProviderConfigGuid, picFieldsRequestContext));
	}

	@Override
	public Single<String> createPI(final String paymentProviderConfigGuid, final Map<String, String> paymentInstrumentForm,
								   final PICRequestContext requestContext) {
		return reactiveAdapter.fromServiceAsSingle(() ->
				orderPaymentApiService.createPI(paymentProviderConfigGuid, paymentInstrumentForm, requestContext));
	}

	@Override
	@CacheResult
	public Single<Boolean> requiresBillingAddress(final String paymentProviderConfigGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> orderPaymentApiService.requiresBillingAddress(paymentProviderConfigGuid),
				"Could not resolve billing address requirements for a payment provider config with the guid: " + paymentProviderConfigGuid);
	}

	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
