/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.impl;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config.StorePaymentProviderConfigRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.payments.orderpayment.OrderPaymentApiRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.transform.ReactiveAdapter;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;

/**
 * Repository for store payment provider configurations.
 */
@Singleton
@Named("storePaymentProviderConfigRepository")
public class StorePaymentProviderConfigRepositoryImpl implements StorePaymentProviderConfigRepository {

	@Inject
	@Named("reactiveAdapter")
	private ReactiveAdapter reactiveAdapter;
	@Inject
	@Named("storePaymentProviderConfigService")
	private StorePaymentProviderConfigService storePaymentProviderConfigService;
	@Inject
	@Named("orderPaymentApiRepository")
	private OrderPaymentApiRepository orderPaymentApiRepository;

	@Override
	public Single<Boolean> requiresBillingAddress(final String storeProviderConfigGuid) {
		return getPaymentProviderConfigIdByStorePaymentProviderConfigId(storeProviderConfigGuid)
				.flatMap(guid -> orderPaymentApiRepository.requiresBillingAddress(guid));
	}

	@Override
	@CacheResult
	public Single<StorePaymentProviderConfig> findByGuid(final String storeProviderConfigGuid) {
		return reactiveAdapter.fromServiceAsSingle(() -> storePaymentProviderConfigService.findByGuid(storeProviderConfigGuid),
				"No store payment provider config found for the given guid: " + storeProviderConfigGuid);
	}

	@Override
	public Single<String> getPaymentProviderConfigIdByStorePaymentProviderConfigId(final String methodId) {
		return findByGuid(methodId).map(StorePaymentProviderConfig::getPaymentProviderConfigGuid);
	}

	@Override
	@CacheResult
	public Observable<StorePaymentProviderConfig> findByStore(final Store store) {
		return reactiveAdapter.fromService(() -> storePaymentProviderConfigService.findByStore(store))
				.flatMap(Observable::fromIterable);
	}

	public void setReactiveAdapter(final ReactiveAdapter reactiveAdapter) {
		this.reactiveAdapter = reactiveAdapter;
	}
}
