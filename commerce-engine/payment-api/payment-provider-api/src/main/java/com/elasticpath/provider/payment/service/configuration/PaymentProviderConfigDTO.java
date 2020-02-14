/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.configuration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;

/**
 * A DTO object of a payment provider configuration.
 */
public class PaymentProviderConfigDTO implements Serializable {

	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	private String guid;
	private String paymentProviderPluginBeanName;
	private String configurationName;
	private String defaultDisplayName;
	private Map<String, String> paymentConfigurationData;
	private PaymentProviderConfigurationStatus status = PaymentProviderConfigurationStatus.DRAFT;
	private Map<String, String> localizedNames = new HashMap<>();

	/**
	 * Gets guid.
	 *
	 * @return the guid.
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * Sets guid.
	 *
	 * @param guid the guid.
	 */
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Gets payment provider plugin bean name.
	 *
	 * @return the payment provider id.
	 */
	public String getPaymentProviderPluginBeanName() {
		return paymentProviderPluginBeanName;
	}

	/**
	 * Sets payment provider plugin bean name.
	 *
	 * @param paymentProviderPluginBeanName the payment provider id.
	 */
	public void setPaymentProviderPluginBeanName(final String paymentProviderPluginBeanName) {
		this.paymentProviderPluginBeanName = paymentProviderPluginBeanName;
	}

	/**
	 * Gets configuration name.
	 *
	 * @return the configuration name.
	 */
	public String getConfigurationName() {
		return configurationName;
	}

	/**
	 * Sets configuration name.
	 *
	 * @param configurationName the configuration name.
	 */
	public void setConfigurationName(final String configurationName) {
		this.configurationName = configurationName;
	}

	/**
	 * Gets the default display name.
	 *
	 * @return the default display name.
	 */
	public String getDefaultDisplayName() {
		return defaultDisplayName;
	}

	/**
	 * Sets the default display name.
	 *
	 * @param defaultDisplayName the default display name.
	 */
	public void setDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}

	/**
	 * Gets payment configuration data.
	 *
	 * @return the payment configuration data.
	 */
	public Map<String, String> getPaymentConfigurationData() {
		return paymentConfigurationData;
	}

	/**
	 * Sets payment configuration data.
	 *
	 * @param paymentConfigurationData the payment configuration data.
	 */
	public void setPaymentConfigurationData(final Map<String, String> paymentConfigurationData) {
		this.paymentConfigurationData = paymentConfigurationData;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status.
	 */
	public PaymentProviderConfigurationStatus getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the status.
	 */
	public void setStatus(final PaymentProviderConfigurationStatus status) {
		this.status = status;
	}

	/**
	 * Gets the map of payment provider localized names.
	 *
	 * @return the map of payment provider localized names.
	 */
	public Map<String, String> getLocalizedNames() {
		return localizedNames;
	}

	/**
	 * Sets the map of payment provider localized names.
	 *
	 * @param localizedNames the map of payment provider localized names.
	 */
	public void setLocalizedNames(final Map<String, String> localizedNames) {
		this.localizedNames = localizedNames;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getGuid());
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof PaymentProviderConfigDTO) {
			PaymentProviderConfigDTO other = (PaymentProviderConfigDTO) obj;
			return Objects.equals(other.getGuid(), this.getGuid());
		}
		return false;
	}
}
