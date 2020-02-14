/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.payments.config;

import io.reactivex.Observable;
import io.reactivex.Single;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;

/**
 * Store payment provider config repository.
 */
public interface StorePaymentProviderConfigRepository {

	/**
	 * Indicates whether the {@link StorePaymentProviderConfig} links to a payment provider
	 * requiring billing addresses for payment instrument creation.
	 *
	 * @param storeProviderConfigGuid unique identifier for the {@link StorePaymentProviderConfig}
	 * @return boolean indicating whether the links payment provider requires a billing address during payment instrument creation
	 */
	Single<Boolean> requiresBillingAddress(String storeProviderConfigGuid);

	/**
	 * Find a {@link StorePaymentProviderConfig} entity by a given GUID.
	 *
	 * @param storeProviderConfigGuid the store payment provider configuration identifier
	 * @return {@link StorePaymentProviderConfig}
	 */
	Single<StorePaymentProviderConfig> findByGuid(String storeProviderConfigGuid);

	/**
	 * Gets the payment provider configuration GUID for a given store payment provider configuration GUID.
	 *
	 * @param methodId store payment provider configuration identifier
	 * @return payment provider configuration GUID
	 */
	Single<String> getPaymentProviderConfigIdByStorePaymentProviderConfigId(String methodId);

	/**
	 * Find all {@link StorePaymentProviderConfig} entities for the provided {@link Store}.
	 *
	 * @param store the {@link Store} entity
	 * @return {@link StorePaymentProviderConfig} collection
	 */
	Observable<StorePaymentProviderConfig> findByStore(Store store);
}
