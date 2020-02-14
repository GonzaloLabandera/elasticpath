/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.impl;

import java.util.Collection;
import java.util.List;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.orderpaymentapi.StorePaymentProviderConfigService;

/**
 * Default implementation for {@link StorePaymentProviderConfigService}.
 */
public class StorePaymentProviderConfigServiceImpl extends AbstractEpPersistenceServiceImpl implements StorePaymentProviderConfigService {

	@Override
	public StorePaymentProviderConfig saveOrUpdate(final StorePaymentProviderConfig storePaymentProviderConfig) {
		sanityCheck();

		return getPersistenceEngine().saveOrUpdate(storePaymentProviderConfig);
	}

	@Override
	public void remove(final StorePaymentProviderConfig storePaymentProviderConfig) {
		sanityCheck();

		getPersistenceEngine().delete(storePaymentProviderConfig);
	}

	@Override
	public StorePaymentProviderConfig findByGuid(final String guid) {
		sanityCheck();

		List<StorePaymentProviderConfig> configurations = getPersistenceEngine()
				.retrieveByNamedQuery("FIND_STORE_PAYMENT_PROVIDER_CONFIG_BY_GUID", guid);

		if (configurations.isEmpty()) {
			return null;
		}

		return configurations.get(0);
	}

	@Override
	public Collection<StorePaymentProviderConfig> findByPaymentProviderConfigGuid(final String guid) {
		sanityCheck();

		return getPersistenceEngine()
				.retrieveByNamedQuery("FIND_STORE_PAYMENT_PROVIDER_CONFIG_BY_PAYMENT_PROVIDER_CONFIG_GUID", guid);
	}

	@Override
	public List<StorePaymentProviderConfig> findByStore(final Store store) {
		sanityCheck();

        return getPersistenceEngine()
                .retrieveByNamedQuery("FIND_STORE_PAYMENT_PROVIDER_CONFIGS_BY_STORE_CODE", store.getCode());
	}

	@Override
	public Collection<String> findStoreNameByProviderConfig(final String paymentProviderConfigGuid) {
		sanityCheck();

		return getPersistenceEngine()
				.retrieveByNamedQuery("FIND_STORE_NAME_BY_PAYMENT_PROVIDER_CONFIG", paymentProviderConfigGuid);
	}

	@Override
	public void deleteByStore(final Store store) {
        sanityCheck();

        getPersistenceEngine().executeNamedQuery("DELETE_STORE_PAYMENT_PROVIDER_CONFIGS_BY_STORE_CODE", store.getCode());
    }

	@Override
	public Object getObject(final long uid) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQuery("FIND_STORE_PAYMENT_PROVIDER_CONFIG_BY_UID", uid).stream()
				.findFirst()
				.orElse(null);
	}
}
