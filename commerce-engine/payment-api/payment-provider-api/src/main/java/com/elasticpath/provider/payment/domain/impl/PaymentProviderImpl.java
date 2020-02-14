/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.impl;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Maps;

import com.elasticpath.plugin.payment.provider.PaymentProviderPlugin;
import com.elasticpath.plugin.payment.provider.annotations.BillingAddressRequired;
import com.elasticpath.plugin.payment.provider.annotations.SingleReservePerPI;
import com.elasticpath.plugin.payment.provider.capabilities.Capability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ReverseChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PICCapability;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationFields;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationRequest;
import com.elasticpath.plugin.payment.provider.capabilities.creation.PaymentInstrumentCreationResponse;
import com.elasticpath.plugin.payment.provider.capabilities.credit.CreditCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICClientInteractionRequestCapability;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructions;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsFields;
import com.elasticpath.plugin.payment.provider.capabilities.instructions.PICInstructionsRequest;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.CancelCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ModifyCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.plugin.payment.provider.dto.PICFieldsRequestContextDTO;
import com.elasticpath.plugin.payment.provider.exception.PaymentInstrumentCreationFailedException;
import com.elasticpath.provider.payment.domain.PaymentProvider;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.service.provider.impl.ContextClassLoaderSwitcher;

/**
 * The payment provider, essentially payment provider plugin with a configuration.
 */
public class PaymentProviderImpl implements PaymentProvider {

	private final PaymentProviderPlugin plugin;
	private final PaymentProviderConfiguration configuration;
	private final Map<String, String> configurationAsMap;

	/**
	 * Constructor for PaymentProviderImpl.
	 *
	 * @param plugin        {@link PaymentProviderPlugin}
	 * @param configuration provider configuration map
	 */
	public PaymentProviderImpl(final PaymentProviderPlugin plugin, final PaymentProviderConfiguration configuration) {
		this.plugin = plugin;
		this.configuration = configuration;
		this.configurationAsMap = asMap(configuration);
	}

	/**
	 * Converts {@link PaymentProviderConfiguration} entity to map of strings.
	 *
	 * @param configuration entity
	 * @return map of strings
	 */
	protected final Map<String, String> asMap(final PaymentProviderConfiguration configuration) {
		Set<PaymentProviderConfigurationData> dataSet = configuration.getPaymentConfigurationData();
		Map<String, String> map = Maps.newHashMapWithExpectedSize(dataSet.size());
		for (PaymentProviderConfigurationData data : dataSet) {
			map.put(data.getKey(), data.getData());
		}
		return map;
	}

	@Override
	public String getPaymentVendorId() {
		return plugin.getPaymentVendorId();
	}

	@Override
	public String getPaymentMethodId() {
		return plugin.getPaymentMethodId();
	}

	@Override
	public String getPaymentProviderPluginId() {
		return plugin.getUniquePluginId();
	}

	@Override
	public String getConfigurationName() {
		return configuration.getConfigurationName();
	}

	@Override
	public Set<PaymentProviderConfigurationData> getConfiguration() {
		return configuration.getPaymentConfigurationData();
	}

	@Override
	public boolean isSingleReservePerPI() {
		return plugin.isAnnotationPresent(SingleReservePerPI.class);
	}

	@Override
	public boolean isBillingAddressRequired() {
		return plugin.isAnnotationPresent(BillingAddressRequired.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Capability> Optional<T> getCapability(final Class<T> type) {
		CapabilityClass clazz = CapabilityClass.valueOf(type);
		switch (clazz) {
			case MODIFY_CAPABILITY:
				return (Optional<T>) proxyModifyCapability();
			case CREDIT_CAPABILITY:
				return (Optional<T>) proxyCreditCapability();
			case CANCEL_CAPABILITY:
				return (Optional<T>) proxyCancelCapability();
			case CHARGE_CAPABILITY:
				return (Optional<T>) proxyChargeCapability();
			case RESERVE_CAPABILITY:
				return (Optional<T>) proxyReserveCapability();
			case REVERSE_CHARGE_CAPABILITY:
				return (Optional<T>) proxyReverseChargeCapability();
			case PIC_CLIENT_INTERACTION_REQUEST_CAPABILITY:
				return (Optional<T>) proxyPICClientInteractionCapability();
			case PIC_CAPABILITY:
				return (Optional<T>) proxyPICCapability();
			default:
				throw new IllegalArgumentException("Capability class is not supported: " + type);
		}
	}

	/**
	 * Proxies modify capability adding plugin configuration data to each request.
	 *
	 * @return proxy
	 */
	protected Optional<ModifyCapability> proxyModifyCapability() {
		return plugin.getCapability(ModifyCapability.class)
				.map(capability -> (ModifyCapability) request -> {
					request.setPluginConfigData(configurationAsMap);
					return capability.modify(request);
				})
				.map(capability -> createProxy(plugin.getClassLoader(), ModifyCapability.class, capability));
	}

	/**
	 * Proxies credit capability adding plugin configuration data to each request.
	 *
	 * @return proxy
	 */
	protected Optional<CreditCapability> proxyCreditCapability() {
		return plugin.getCapability(CreditCapability.class)
				.map(creditCapability -> (CreditCapability) request -> {
					request.setPluginConfigData(configurationAsMap);
					return creditCapability.credit(request);
				})
				.map(capability -> createProxy(plugin.getClassLoader(), CreditCapability.class, capability));
	}

	/**
	 * Proxies cancel capability adding plugin configuration data to each request.
	 *
	 * @return proxy
	 */
	protected Optional<CancelCapability> proxyCancelCapability() {
		return plugin.getCapability(CancelCapability.class)
				.map(capability -> (CancelCapability) request -> {
					request.setPluginConfigData(configurationAsMap);
					return capability.cancel(request);
				})
				.map(capability -> createProxy(plugin.getClassLoader(), CancelCapability.class, capability));
	}

	/**
	 * Proxies charge capability adding plugin configuration data to each request.
	 *
	 * @return proxy
	 */
	protected Optional<ChargeCapability> proxyChargeCapability() {
		return plugin.getCapability(ChargeCapability.class)
				.map(capability -> (ChargeCapability) request -> {
					request.setPluginConfigData(configurationAsMap);
					return capability.charge(request);
				})
				.map(capability -> createProxy(plugin.getClassLoader(), ChargeCapability.class, capability));
	}

	/**
	 * Proxies reserve capability adding plugin configuration data to each request.
	 *
	 * @return proxy
	 */
	protected Optional<ReserveCapability> proxyReserveCapability() {
		return plugin.getCapability(ReserveCapability.class)
				.map(capability -> (ReserveCapability) request -> {
					request.setPluginConfigData(configurationAsMap);
					return capability.reserve(request);
				})
				.map(capability -> createProxy(plugin.getClassLoader(), ReserveCapability.class, capability));
	}

	/**
	 * Proxies reverse charge capability adding plugin configuration data to each request.
	 *
	 * @return proxy
	 */
	protected Optional<ReverseChargeCapability> proxyReverseChargeCapability() {
		return plugin.getCapability(ReverseChargeCapability.class)
				.map(capability -> (ReverseChargeCapability) request -> {
					request.setPluginConfigData(configurationAsMap);
					return capability.reverseCharge(request);
				})
				.map(capability -> createProxy(plugin.getClassLoader(), ReverseChargeCapability.class, capability));
	}

	private Optional<PICClientInteractionRequestCapability> proxyPICClientInteractionCapability() {
		return plugin.getCapability(PICClientInteractionRequestCapability.class)
				.map(original -> new PICClientInteractionRequestCapability() {
					@Override
					public PICInstructionsFields getPaymentInstrumentCreationInstructionsFields(final PICFieldsRequestContextDTO context)
							throws PaymentInstrumentCreationFailedException {
						context.setPluginConfigData(configurationAsMap);
						return original.getPaymentInstrumentCreationInstructionsFields(context);
					}

					@Override
					public PICInstructions getPaymentInstrumentCreationInstructions(final PICInstructionsRequest request)
							throws PaymentInstrumentCreationFailedException {
						request.setPluginConfigData(configurationAsMap);
						return original.getPaymentInstrumentCreationInstructions(request);
					}
				})
				.map(capability -> createProxy(plugin.getClassLoader(), PICClientInteractionRequestCapability.class, capability));
	}

	private Optional<PICCapability> proxyPICCapability() {
		return plugin.getCapability(PICCapability.class)
				.map(original -> new PICCapability() {
					@Override
					public PaymentInstrumentCreationFields getPaymentInstrumentCreationFields(final PICFieldsRequestContextDTO context)
							throws PaymentInstrumentCreationFailedException {
						context.setPluginConfigData(configurationAsMap);
						return original.getPaymentInstrumentCreationFields(context);
					}

					@Override
					public PaymentInstrumentCreationResponse createPaymentInstrument(final PaymentInstrumentCreationRequest request)
							throws PaymentInstrumentCreationFailedException {
						request.setPluginConfigData(configurationAsMap);
						return original.createPaymentInstrument(request);
					}
				})
				.map(capability -> createProxy(plugin.getClassLoader(), PICCapability.class, capability));
	}

	/**
	 * Creates Java proxy to ensure correct Thread Context Class Loader is used.
	 *
	 * @param classLoader         class loader to use
	 * @param capabilityInterface capability interface
	 * @param capability          capability implementation
	 * @param <T>                 capability interface
	 * @return proxy around capability interface
	 */
	@SuppressWarnings("unchecked")
	protected <T extends Capability> T createProxy(final ClassLoader classLoader, final Class<T> capabilityInterface, final T capability) {
		return (T) Proxy.newProxyInstance(classLoader, new Class<?>[]{capabilityInterface}, new ContextClassLoaderSwitcher(classLoader, capability));
	}

	/**
	 * Enum containing all known capability class names.
	 */
	private enum CapabilityClass {
		MODIFY_CAPABILITY(ModifyCapability.class),
		CREDIT_CAPABILITY(CreditCapability.class),
		CANCEL_CAPABILITY(CancelCapability.class),
		CHARGE_CAPABILITY(ChargeCapability.class),
		RESERVE_CAPABILITY(ReserveCapability.class),
		REVERSE_CHARGE_CAPABILITY(ReverseChargeCapability.class),
		PIC_CLIENT_INTERACTION_REQUEST_CAPABILITY(PICClientInteractionRequestCapability.class),
		PIC_CAPABILITY(PICCapability.class);

		private final Class<? extends Capability> clazz;

		CapabilityClass(final Class<? extends Capability> clazz) {
			this.clazz = clazz;
		}

		static CapabilityClass valueOf(final Class<? extends Capability> capabilityClass) {
			for (CapabilityClass value : values()) {
				if (value.clazz.equals(capabilityClass)) {
					return value;
				}
			}
			throw new IllegalArgumentException("Capability class is not supported: " + capabilityClass);
		}
	}
}
