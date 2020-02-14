/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.Collection;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.EpPersistenceService;

/**
 * Service for saving, deleting and retrieving {@link StorePaymentProviderConfig}.
 */
public interface StorePaymentProviderConfigService extends EpPersistenceService {
	/**
	 * Save or update a {@link StorePaymentProviderConfig}.
	 *
	 * @param storePaymentProviderConfig {@link StorePaymentProviderConfig} to save or update
	 * @return persisted {@link StorePaymentProviderConfig}
	 */
	StorePaymentProviderConfig saveOrUpdate(StorePaymentProviderConfig storePaymentProviderConfig);

	/**
	 * Delete a persisted {@link StorePaymentProviderConfig}.
	 *
	 * @param storePaymentProviderConfig persisted {@link StorePaymentProviderConfig}
	 */
	void remove(StorePaymentProviderConfig storePaymentProviderConfig);

	/**
	 * Find a {@link StorePaymentProviderConfig} entity by a given GUID.
	 *
	 * @param guid the GUID
	 * @return {@link StorePaymentProviderConfig}
	 */
	StorePaymentProviderConfig findByGuid(String guid);

	/**
	 * Find all {@link StorePaymentProviderConfig} entities by a given payment provider config guid.
	 *
	 * @param guid the payment provider config guid.
	 * @return collection of {@link StorePaymentProviderConfig}
	 */
	Collection<StorePaymentProviderConfig> findByPaymentProviderConfigGuid(String guid);

	/**
	 * Find all {@link StorePaymentProviderConfig} entities by {@link Store} entity.
	 *
	 * @param store the {@link Store} entity
	 * @return {@link StorePaymentProviderConfig} collection
	 */
	Collection<StorePaymentProviderConfig> findByStore(Store store);

	/**
	 * Find all {@link Store} names related to a PaymentProviderConfigDTO guid.
	 *
	 * @param paymentProviderConfigGuid the PaymentProviderConfigDTO guid
	 * @return {@link String} collection of store names
	 */
	Collection<String> findStoreNameByProviderConfig(String paymentProviderConfigGuid);

	/**
	 * Delete all {@link StorePaymentProviderConfig} entities by {@link Store} entity.
	 *
	 * @param store the {@link Store} entity
	 */
	void deleteByStore(Store store);

}
