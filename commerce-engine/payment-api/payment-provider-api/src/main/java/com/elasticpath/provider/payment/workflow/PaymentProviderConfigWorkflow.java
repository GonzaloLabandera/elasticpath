/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.workflow;

import java.util.List;

import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;

/**
 * Payment provider configuration workflow facade for Order Payment API (ep-core) interaction.
 */
public interface PaymentProviderConfigWorkflow {

	/**
	 * Find all {@link PaymentProviderConfigDTO}s.
	 * @return all payment provider configuration.
	 */
	List<PaymentProviderConfigDTO> findAll();

	/**
	 * Find all {@link PaymentProviderConfigDTO}s by status.
	 *
	 * @param status config status to find by.
	 * @return all payment provider configuration.
	 */
	List<PaymentProviderConfigDTO> findByStatus(PaymentProviderConfigurationStatus status);

	/**
	 * Find {@link PaymentProviderConfigDTO} by guid.
	 * @param guid  given guid.
	 * @return instance of {@link PaymentProviderConfigDTO} if it found, otherwise null.
	 */
	PaymentProviderConfigDTO findByGuid(String guid);

	/**
	 * Find all {@link PaymentProviderConfigDTO}s by guid.
	 * @param guids list of guids to find by.
	 * @return list of the payment provider configuration.
	 */
	List<PaymentProviderConfigDTO> findByGuids(List<String> guids);

	/**
	 * Save or update payment provider configuration.
	 * @param paymentProviderConfiguration the payment provider configuration to save or update.
	 * @return the saved payment provider configuration.
	 */
	PaymentProviderConfigDTO saveOrUpdate(
			PaymentProviderConfigDTO paymentProviderConfiguration);
}