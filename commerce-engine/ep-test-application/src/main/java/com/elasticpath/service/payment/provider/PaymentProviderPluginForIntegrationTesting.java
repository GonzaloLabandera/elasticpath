/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.payment.provider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.elasticpath.plugin.payment.provider.AbstractPaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.util.Utils;

/**
 * Dummy plugin for Spring test.
 */
public class PaymentProviderPluginForIntegrationTesting extends AbstractPaymentProviderPlugin {

	private static final String PAYMENT_VENDOR_ID = Utils.uniqueCode("PAYMENT_VENDOR_ID");
	private static final String PAYMENT_METHOD_ID = Utils.uniqueCode("PAYMENT_METHOD_ID");

	private static final Map<Class<? extends Capability>, Capability> CAPABILITIES = new HashMap<>();
	private static Class<? extends BasicSpringContextTest> currentCaller;

	/**
	 * Constructor. By default all capabilities are responding with success to any request.
	 */
	public PaymentProviderPluginForIntegrationTesting() {
		initCapabilities();
	}

	private static void initCapabilities() {
		clearCapabilities();

		CAPABILITIES.put(ReserveCapability.class, (ReserveCapability) request -> new PaymentCapabilityResponseForIntegrationTesting());
		CAPABILITIES.put(ModifyCapability.class, (ModifyCapability) request -> new PaymentCapabilityResponseForIntegrationTesting());
		CAPABILITIES.put(CreditCapability.class, (CreditCapability) request -> new PaymentCapabilityResponseForIntegrationTesting());
		CAPABILITIES.put(CancelCapability.class, (CancelCapability) request -> new PaymentCapabilityResponseForIntegrationTesting());
		CAPABILITIES.put(ChargeCapability.class, (ChargeCapability) request -> new PaymentCapabilityResponseForIntegrationTesting());
		CAPABILITIES.put(ReverseChargeCapability.class, (ReverseChargeCapability) request -> new PaymentCapabilityResponseForIntegrationTesting());
		CAPABILITIES.put(PICCapability.class, new PICCapabilityImplementationForTesting());
	}

	/**
	 * Add payment provider capability for test. Ensures that capability configuration does not escape the test its defined by.
	 *
	 * @param caller          test class
	 * @param capabilityClass capability class
	 * @param capability      capability implementation
	 * @param <T>             capability class
	 */
	public static <T extends Capability> void addCapability(final Class<? extends BasicSpringContextTest> caller,
															final Class<T> capabilityClass,
															final T capability) {
		checkCapabilityConfigurationDidNotEscapeTheCaller(caller);
		CAPABILITIES.put(capabilityClass, capability);
	}

	private static void checkCapabilityConfigurationDidNotEscapeTheCaller(Class<? extends BasicSpringContextTest> caller) {
		if (caller == null) {
			throw new IllegalArgumentException("Caller test class cannot be null");
		}
		if (currentCaller != null && currentCaller != caller) {
			throw new IllegalStateException(currentCaller.getName() + " did not reset capabilities in clean up phase");
		}
	}

	/**
	 * Resets capabilities. Each test redefining them must at least reset them on clean up.
	 */
	public static void resetCapabilities() {
		initCapabilities();
		currentCaller = null;
	}

	/**
	 * Removes all capabilities, so this plugin turns into 'No Capability' mode.
	 */
	public static void clearCapabilities() {
		CAPABILITIES.clear();
	}

	/**
	 * Remove payment provider capability for test. Ensures that new capability configuration does not escape the test its defined by.
	 *
	 * @param caller          test class
	 * @param capabilityClass capability class
	 */
	public static void removeCapability(final Class<? extends BasicSpringContextTest> caller,
										final Class<? extends Capability> capabilityClass) {
		checkCapabilityConfigurationDidNotEscapeTheCaller(caller);
		CAPABILITIES.remove(capabilityClass);
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
	public String getPaymentVendorId() {
		return PAYMENT_VENDOR_ID;
	}

	@Override
	public String getPaymentMethodId() {
		return PAYMENT_METHOD_ID;
	}

	@Override
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return Collections.emptyList();
	}
}
