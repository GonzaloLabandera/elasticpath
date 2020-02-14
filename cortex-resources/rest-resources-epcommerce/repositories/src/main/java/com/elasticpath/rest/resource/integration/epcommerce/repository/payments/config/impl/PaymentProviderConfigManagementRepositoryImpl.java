/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Single;

import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.PaymentProviderConfigManagementRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.management.PaymentProviderConfigManagementService;

/**
 * Repository for operations with payment provider configurations.
 */
@Singleton
@Named("paymentProviderConfigManagementRepository")
public class PaymentProviderConfigManagementRepositoryImpl implements PaymentProviderConfigManagementRepository {

	@Inject
	@Named("reactiveAdapter")
	private ReactiveAdapter reactiveAdapter;
	@Inject
	@Named("paymentProviderConfigManagementService")
	private PaymentProviderConfigManagementService paymentProviderConfigManagementService;

	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}

	protected ReactiveAdapter getReactiveAdapter() {
		return reactiveAdapter;
	}

	@Override
	@CacheResult
	public Single<PaymentProviderConfigDTO> findByGuid(final String guid) {
		return reactiveAdapter.fromServiceAsSingle(() -> paymentProviderConfigManagementService.findByGuid(guid),
				"No payment provider configuration found for guid " + guid + ".");
	}
}
