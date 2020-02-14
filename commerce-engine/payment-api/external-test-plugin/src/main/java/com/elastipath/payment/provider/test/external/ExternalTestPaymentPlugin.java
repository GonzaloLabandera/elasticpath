/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elastipath.payment.provider.test.external;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.capabilities.Capability;

/**
 * Test payment plugin loaded by {@link com.elasticpath.provider.payment.service.provider.ExternalPluginLoader}.
 */
@Component
public class ExternalTestPaymentPlugin extends AbstractPaymentProviderPlugin {

	private static PaymentProviderPlugin callback;
	private static final Map<Class<? extends Capability>, Capability> CAPABILITIES = new HashMap<>();

	/**
	 * Set callback for plugin methods.
	 *
	 * @param callback callback
	 */
	public static void setCallback(final PaymentProviderPlugin callback) {
		ExternalTestPaymentPlugin.callback = callback;
	}

	/**
	 * Add payment provider capability for test.
	 *
	 * @param capabilityClass capability class
	 * @param capability      capability implementation
	 * @param <T>             capability class
	 */
	public static <T extends Capability> void addCapability(final Class<T> capabilityClass, final T capability) {
		CAPABILITIES.put(capabilityClass, capability);
	}

	/**
	 * Removes all capabilities, so this plugin turns into 'No Capability' mode.
	 */
	public static void clearCapabilities() {
		CAPABILITIES.clear();
	}

	@Override
	public String getPaymentVendorId() {
		return callback.getPaymentVendorId();
	}

	@Override
	public String getPaymentMethodId() {
		return callback.getPaymentMethodId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Capability> Optional<T> getCapability(final Class<T> type) {
		return (Optional<T>) Optional.ofNullable(CAPABILITIES.get(type));
	}

	@Override
	protected <T extends Capability> boolean hasCapability(final Class<T> capability) {
		return CAPABILITIES.containsKey(capability);
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return callback.getConfigurationKeys();
	}

}
