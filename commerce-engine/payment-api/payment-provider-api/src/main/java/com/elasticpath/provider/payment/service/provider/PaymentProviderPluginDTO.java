/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.provider;

import java.util.List;
import java.util.Objects;

import com.elasticpath.plugin.payment.provider.PluginConfigurationKey;

/**
 * A DTO object of a payment provider plugin.
 */
public class PaymentProviderPluginDTO {

	private String pluginBeanName;
	private String paymentVendorId;
	private String paymentMethodId;
	private List<PluginConfigurationKey> configurationKeys;

	/**
	 * Gets payment provider bean name.
	 *
	 * @return the payment provider bean name.
	 */
	public String getPluginBeanName() {
		return pluginBeanName;
	}

	/**
	 * Sets payment provider bean name.
	 *
	 * @param pluginBeanName the payment provider bean name.
	 */
	public void setPluginBeanName(final String pluginBeanName) {
		this.pluginBeanName = pluginBeanName;
	}

	/**
	 * Gets payment vendor id.
	 *
	 * @return the payment vendor id.
	 */
	public String getPaymentVendorId() {
		return paymentVendorId;
	}

	/**
	 * Sets payment vendor id.
	 *
	 * @param paymentVendorId the payment vendor id.
	 */
	public void setPaymentVendorId(final String paymentVendorId) {
		this.paymentVendorId = paymentVendorId;
	}

	/**
	 * Gets payment method id.
	 *
	 * @return the payment method id.
	 */
	public String getPaymentMethodId() {
		return paymentMethodId;
	}

	/**
	 * Sets payment method id.
	 *
	 * @param paymentMethodId the payment method id.
	 */
	public void setPaymentMethodId(final String paymentMethodId) {
		this.paymentMethodId = paymentMethodId;
	}

	/**
	 * Gets PluginConfigurationKey objects.
	 *
	 * @return the PluginConfigurationKey objects.
	 */
	public List<PluginConfigurationKey> getConfigurationKeys() {
		return configurationKeys;
	}

	/**
	 * Sets PluginConfigurationKey objects.
	 *
	 * @param configurationKeys the PluginConfigurationKey objects.
	 */
	public void setConfigurationKeys(final List<PluginConfigurationKey> configurationKeys) {
		this.configurationKeys = configurationKeys;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getPluginBeanName());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof PaymentProviderPluginDTO) {
			PaymentProviderPluginDTO other = (PaymentProviderPluginDTO) obj;
			return Objects.equals(other.getPluginBeanName(), this.getPluginBeanName());
		}
		return false;
	}
}
