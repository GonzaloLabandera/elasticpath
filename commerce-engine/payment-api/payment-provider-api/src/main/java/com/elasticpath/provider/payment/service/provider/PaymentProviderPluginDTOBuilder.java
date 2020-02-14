/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.provider;

import java.util.List;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;

/**
 * A DTO object of a payment provider configuration.
 */
public final class PaymentProviderPluginDTOBuilder {

	private String pluginBeanName;
	private String paymentVendorId;
	private String paymentMethodId;
	private List<PluginConfigurationKey> configurationKeys;

	private PaymentProviderPluginDTOBuilder() {
	}

	/**
	 * A payment instrument DTO builder.
	 *
	 * @return the builder
	 */
	public static PaymentProviderPluginDTOBuilder builder() {
		return new PaymentProviderPluginDTOBuilder();
	}

	/**
	 * With payment provider plugin bean name builder.
	 *
	 * @param pluginBeanName the payment provider plugin bean name
	 * @return the builder
	 */
	public PaymentProviderPluginDTOBuilder withPluginBeanName(final String pluginBeanName) {
		this.pluginBeanName = pluginBeanName;
		return this;
	}

	/**
	 * With payment vendor id builder.
	 *
	 * @param paymentVendorId the payment vendor id
	 * @return the builder
	 */
	public PaymentProviderPluginDTOBuilder withPaymentVendorId(final String paymentVendorId) {
		this.paymentVendorId = paymentVendorId;
		return this;
	}

	/**
	 * With payment method id builder.
	 *
	 * @param paymentMethodId the payment method id
	 * @return the builder
	 */
	public PaymentProviderPluginDTOBuilder withPaymentMethodId(final String paymentMethodId) {
		this.paymentMethodId = paymentMethodId;
		return this;
	}

	/**
	 * With PluginConfigurationKey builder.
	 *
	 * @param configurationKeys the PluginConfigurationKey object
	 * @return the builder
	 */
	public PaymentProviderPluginDTOBuilder withConfigurationKeys(final List<PluginConfigurationKey> configurationKeys) {
		this.configurationKeys = configurationKeys;
		return this;
	}

	/**
	 * Build payment provider plugin DTO.
	 *
	 * @param beanFactory EP bean factory
	 * @return the payment provider plugin DTO
	 */
	public PaymentProviderPluginDTO build(final BeanFactory beanFactory) {
		if (pluginBeanName == null) {
			throw new IllegalStateException("Builder is not fully initialized, pluginBeanName is missing");
		}
		if (paymentVendorId == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentVendorId is missing");
		}
		if (paymentMethodId == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentMethodId is missing");
		}
		if (configurationKeys == null) {
			throw new IllegalStateException("Builder is not fully initialized, configurationKeys list is missing");
		}
		final PaymentProviderPluginDTO paymentProviderPluginDTO = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_PLUGIN_DTO, PaymentProviderPluginDTO.class);
		paymentProviderPluginDTO.setPluginBeanName(pluginBeanName);
		paymentProviderPluginDTO.setPaymentVendorId(paymentVendorId);
		paymentProviderPluginDTO.setPaymentMethodId(paymentMethodId);
		paymentProviderPluginDTO.setConfigurationKeys(configurationKeys);
		return paymentProviderPluginDTO;
	}
}
