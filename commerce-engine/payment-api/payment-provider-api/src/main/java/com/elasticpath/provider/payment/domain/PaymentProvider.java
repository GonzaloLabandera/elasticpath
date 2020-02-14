/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain;

import java.util.Optional;
import java.util.Set;

import com.elasticpath.plugin.payment.provider.capabilities.Capability;

/**
 * The interface for configured Payment provider (plugin).
 */
public interface PaymentProvider {
	/**
	 * Get payment vendor id.
	 *
	 * @return paymentVendorId for display
	 */
	String getPaymentVendorId();

	/**
	 * Gets payment method id.
	 *
	 * @return the payment method id
	 */
	String getPaymentMethodId();

	/**
	 * Get the unique plugin name.
	 *
	 * @return name.
	 */
	String getPaymentProviderPluginId();

	/**
	 * Get configuration name.
	 *
	 * @return configuration name
	 */
	String getConfigurationName();

	/**
	 * Get configuration.
	 *
	 * @return list of config keys.
	 */
	Set<PaymentProviderConfigurationData> getConfiguration();

	/**
	 * Returns true if payment instrument is single time use only.
	 *
	 * @return true if payment instrument is single time use only.
	 */
	boolean isSingleReservePerPI();

	/**
	 * Returns true if plugin that requires a billing address during payment instrument creation.
	 *
	 * @return true if plugin that requires a billing address during payment instrument creation.
	 */
	boolean isBillingAddressRequired();

	/**
	 * Retrieve the requested Capability from the plugin.
	 *
	 * @param <T>  the particular Capability type being requested
	 * @param type the class of the Capability being requested
	 * @return an instance of the requested Capability
	 */
	<T extends Capability> Optional<T> getCapability(Class<T> type);
}

