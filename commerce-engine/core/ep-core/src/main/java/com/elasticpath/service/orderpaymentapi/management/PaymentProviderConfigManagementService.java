/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi.management;

import java.util.List;

import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;

/**
 * Service to interact with payment provider config workflow.
 */
public interface PaymentProviderConfigManagementService {

	/**
	 * Find all {@link PaymentProviderConfigDTO}s.
	 * @return all payment provider configuration.
	 */
	List<PaymentProviderConfigDTO> findAll();

	/**
	 * Find all active {@link PaymentProviderConfigDTO}s.
	 * @return all active payment provider configuration.
	 */
	List<PaymentProviderConfigDTO> findAllActive();

	/**
	 * Find {@link PaymentProviderConfigDTO} by guid.
	 * @param guid  given guid.
	 * @return instance of {@link PaymentProviderConfigDTO} if it found, otherwise null.
	 */
	PaymentProviderConfigDTO findByGuid(String guid);

	/**
	 * Find {@link PaymentProviderConfigDTO}s by guids.
	 * @param guids list of payment provider configuration guid.
	 * @return list of payment provider configurations.
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
