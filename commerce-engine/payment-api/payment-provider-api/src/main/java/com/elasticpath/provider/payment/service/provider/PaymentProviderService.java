/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.provider;

import java.util.Collection;

import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;

/**
 * Service for configurations of payment plugins.
 */
public interface PaymentProviderService {
	/**
	 * Get all available plugins.
	 *
	 * @return collection of plugins
	 */
	Collection<PaymentProviderPlugin> getPlugins();

	/**
	 * Creates payment provider based on selected plugin configuration.
	 *
	 * @param configuration provider configuration
	 * @return configured payment provider
	 */
	PaymentProvider createProvider(PaymentProviderConfiguration configuration);
}