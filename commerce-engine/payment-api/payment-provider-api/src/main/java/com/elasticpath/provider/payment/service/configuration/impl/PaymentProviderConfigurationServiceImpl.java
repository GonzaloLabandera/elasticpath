/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.configuration.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.domain.impl.PaymentProviderConfigurationImpl;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigurationService;

/**
 * Perform CRUD operations with PaymentProviderConfiguration entity.
 */
public class PaymentProviderConfigurationServiceImpl implements PaymentProviderConfigurationService {

	private static final String PLACEHOLDER_FOR_LIST = "list";

	private final PersistenceEngine persistenceEngine;

	/**
	 * Constructor.
	 *
	 * @param persistenceEngine persistence engine
	 */
	@Autowired
	public PaymentProviderConfigurationServiceImpl(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * Get persistence engine.
	 *
	 * @return persistence engine
	 */
	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	@Override
	public PaymentProviderConfiguration get(final long uid) {
		return persistenceEngine.load(PaymentProviderConfigurationImpl.class, uid);
	}

	@Override
	public PaymentProviderConfiguration findByGuid(final String guid) {
		return (PaymentProviderConfiguration) persistenceEngine.retrieveByNamedQuery("PAYMENT_PROVIDER_CONFIG_BY_GUID", guid)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public void remove(final PaymentProviderConfiguration paymentProvider) {
		persistenceEngine.delete(paymentProvider);
	}

	@Override
	public PaymentProviderConfiguration saveOrUpdate(final PaymentProviderConfiguration paymentProviderConfiguration) {
		return persistenceEngine.saveOrUpdate(paymentProviderConfiguration);
	}

	@Override
	public PaymentProviderConfiguration update(final PaymentProviderConfiguration paymentProviderConfiguration) {
		return persistenceEngine.update(paymentProviderConfiguration);
	}

	@Override
	public List<PaymentProviderConfiguration> findAll() {
		return persistenceEngine.retrieveByNamedQuery("PAYMENT_PROVIDER_CONFIG_FIND_ALL");
	}

	@Override
	public List<PaymentProviderConfiguration> findByStatus(final PaymentProviderConfigurationStatus status) {
		return persistenceEngine.retrieveByNamedQuery("PAYMENT_PROVIDER_CONFIG_BY_STATUS", status);
	}

	@Override
	public List<PaymentProviderConfiguration> findByGuids(final List<String> guids) {
		return persistenceEngine.retrieveByNamedQueryWithList("PAYMENT_PROVIDER_CONFIG_BY_GUIDS", PLACEHOLDER_FOR_LIST, guids);
	}

}
