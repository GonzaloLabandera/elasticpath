/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.configuration;

import java.util.List;

import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;

/**
 * Interface to perform CRUD operations with {@link PaymentProviderConfiguration} entity.
 */
public interface PaymentProviderConfigurationService {

	/**
	 * Load the {@link PaymentProviderConfiguration} with the given UID.
	 *
	 * @param uid the {@link PaymentProviderConfiguration} UID
	 * @return the {@link PaymentProviderConfiguration} if UID exists, otherwise null
	 */
	PaymentProviderConfiguration get(long uid);

	/**
	 * Retrieve the {@link PaymentProviderConfiguration} with the given guid.
	 *
	 * @param guid the guid of the {@link PaymentProviderConfiguration}
	 * @return the {@link PaymentProviderConfiguration} with the given guid
	 */
	PaymentProviderConfiguration findByGuid(String guid);

	/**
	 * Deleting a {@link PaymentProviderConfiguration}.
	 *
	 * @param paymentProviderConfiguration the {@link PaymentProviderConfiguration} to remove
	 */
	void remove(PaymentProviderConfiguration paymentProviderConfiguration);

	/**
	 * Saves or updates the given {@link PaymentProviderConfiguration}.
	 *
	 * @param paymentProviderConfiguration the {@link PaymentProviderConfiguration} to save or update
	 * @return the updated {@link PaymentProviderConfiguration}
	 */
	PaymentProviderConfiguration saveOrUpdate(PaymentProviderConfiguration paymentProviderConfiguration);

	/**
	 * Updates the given {@link PaymentProviderConfiguration}.
	 *
	 * @param paymentProviderConfiguration the {@link PaymentProviderConfiguration} to update
	 * @return the updated {@link PaymentProviderConfiguration}
	 */
	PaymentProviderConfiguration update(PaymentProviderConfiguration paymentProviderConfiguration);

	/**
	 * Find all payment provider configuration.
	 *
	 * @return all payment provider configuration.
	 */
	List<PaymentProviderConfiguration> findAll();

	/**
	 * Find all payment provider configuration by status.
	 *
	 * @param status config status to find by.
	 * @return all payment provider configuration.
	 */
	List<PaymentProviderConfiguration> findByStatus(PaymentProviderConfigurationStatus status);

	/**
	 * Find payment provider configurations by guids.
	 *
	 * @param guids list of payment provider guid.
	 * @return list of payment provider configuration
	 */
	List<PaymentProviderConfiguration> findByGuids(List<String> guids);
}
