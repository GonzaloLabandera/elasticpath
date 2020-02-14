/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.cmclient.core.helpers.store;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;
import com.elasticpath.provider.payment.service.configuration.PaymentProviderConfigDTO;
import com.elasticpath.provider.payment.service.provider.PaymentProviderPluginDTO;

/**
 * Store payment provider configurations factory.
 */
public class StorePaymentConfigurationFactory {

	private static final String MISSING_PAYMENT_PROVIDER_NAME = "MISSING";

	/**
	 * Creates a store payment provider configurations.
	 *
	 * @param paymentProviderConfigDTO   paymentProviderConfigDTO
	 * @param paymentProviderPluginDTO   paymentProviderPluginDTO
	 * @param storePaymentProviderConfig storePaymentProviderConfig
	 * @return store payment provider configurations
	 */
	public StorePaymentConfigurationModel createPaymentConfiguration(
			final PaymentProviderConfigDTO paymentProviderConfigDTO,
			final PaymentProviderPluginDTO paymentProviderPluginDTO,
			final StorePaymentProviderConfig storePaymentProviderConfig) {
		if (paymentProviderPluginDTO == null) {
			return new StorePaymentConfigurationModel(storePaymentProviderConfig,
					paymentProviderConfigDTO.getConfigurationName(),
					paymentProviderConfigDTO.getGuid(),
					MISSING_PAYMENT_PROVIDER_NAME,
					MISSING_PAYMENT_PROVIDER_NAME,
					true);
		}

		return new StorePaymentConfigurationModel(storePaymentProviderConfig,
				paymentProviderConfigDTO.getConfigurationName(),
				paymentProviderConfigDTO.getGuid(),
				paymentProviderPluginDTO.getPaymentVendorId(),
				paymentProviderPluginDTO.getPaymentMethodId(),
				true);
	}

	/**
	 * Creates a store payment provider configurations.
	 *
	 * @param paymentProviderConfigDTO paymentProviderConfigDTO
	 * @param paymentProviderPluginDTO paymentProviderPluginDTO
	 * @return store payment provider configurations
	 */
	public StorePaymentConfigurationModel createPaymentConfiguration(
			final PaymentProviderConfigDTO paymentProviderConfigDTO,
			final PaymentProviderPluginDTO paymentProviderPluginDTO) {
		if (paymentProviderPluginDTO == null) {
			return new StorePaymentConfigurationModel(null,
					paymentProviderConfigDTO.getConfigurationName(),
					paymentProviderConfigDTO.getGuid(),
					MISSING_PAYMENT_PROVIDER_NAME,
					MISSING_PAYMENT_PROVIDER_NAME,
					false);
		}

		return new StorePaymentConfigurationModel(null,
				paymentProviderConfigDTO.getConfigurationName(),
				paymentProviderConfigDTO.getGuid(),
				paymentProviderPluginDTO.getPaymentVendorId(),
				paymentProviderPluginDTO.getPaymentMethodId(),
				false);
	}
}
