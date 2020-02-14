/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.configuration;

import java.util.Set;

import com.elasticpath.provider.payment.PaymentLocalizedProperties;
import com.elasticpath.provider.payment.domain.PaymentProviderConfiguration;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationData;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;

/**
 * A DTO object of a payment provider configuration.
 */
public final class PaymentProviderConfigurationBuilder {

	private String guid;
	private String paymentProviderPluginId;
	private String configurationName;
	private String defaultDisplayName;
	private Set<PaymentProviderConfigurationData> paymentConfigurationData;
	private PaymentProviderConfigurationStatus status = PaymentProviderConfigurationStatus.DRAFT;
	private PaymentLocalizedProperties paymentLocalizedProperties;

	private PaymentProviderConfigurationBuilder() {
	}

	/**
	 * A payment instrument DTO builder.
	 *
	 * @return the builder
	 */
	public static PaymentProviderConfigurationBuilder builder() {
		return new PaymentProviderConfigurationBuilder();
	}

	/**
	 * With guid builder.
	 *
	 * @param guid the guid
	 * @return the builder
	 */
	public PaymentProviderConfigurationBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * With payment provider plugin id builder.
	 *
	 * @param paymentProviderPluginId the payment provider plugin id
	 * @return the builder
	 */
	public PaymentProviderConfigurationBuilder withPaymentProviderPluginId(final String paymentProviderPluginId) {
		this.paymentProviderPluginId = paymentProviderPluginId;
		return this;
	}

	/**
	 * With configuration name builder.
	 *
	 * @param configurationName the configuration name
	 * @return the builder
	 */
	public PaymentProviderConfigurationBuilder withConfigurationName(final String configurationName) {
		this.configurationName = configurationName;
		return this;
	}

	/**
	 * With default display name builder.
	 *
	 * @param defaultDisplayName the default display name.
	 * @return the builder
	 */
	public PaymentProviderConfigurationBuilder withDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
		return this;
	}

	/**
	 * With payment configuration data builder.
	 *
	 * @param paymentConfigurationData the payment configuration data
	 * @return the builder
	 */
	public PaymentProviderConfigurationBuilder withPaymentConfigurationData(final Set<PaymentProviderConfigurationData> paymentConfigurationData) {
		this.paymentConfigurationData = paymentConfigurationData;
		return this;
	}

	/**
	 * With payment provider configuration builder.
	 *
	 * @param status the status
	 * @return the builder
	 */
	public PaymentProviderConfigurationBuilder withStatus(final PaymentProviderConfigurationStatus status) {
		this.status = status;
		return this;
	}

	/**
	 * With payment provider configuration builder.
	 *
	 * @param paymentLocalizedProperties the payment localized properties
	 * @return the builder
	 */
	public PaymentProviderConfigurationBuilder withPaymentLocalizedProperties(final PaymentLocalizedProperties paymentLocalizedProperties) {
		this.paymentLocalizedProperties = paymentLocalizedProperties;
		return this;
	}

	/**
	 * Build payment provider configuration.
	 *
	 * @param paymentProviderConfiguration the payment provider configuration
	 * @return the payment provider configuration
	 */
	public PaymentProviderConfiguration build(final PaymentProviderConfiguration paymentProviderConfiguration) {
		if (guid == null) {
			throw new IllegalStateException("Builder is not fully initialized, guid is missing");
		}
		if (configurationName == null) {
			throw new IllegalStateException("Builder is not fully initialized, configurationName is missing");
		}
		if (paymentConfigurationData == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentConfigurationData set is missing");
		}
		if (paymentProviderPluginId == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentProviderPluginId is missing");
		}
		if (status == null) {
			throw new IllegalStateException("Builder is not fully initialized, status is missing");
		}
		if (paymentLocalizedProperties == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentLocalizedProperties entity is missing");
		}
		paymentProviderConfiguration.setGuid(guid);
		paymentProviderConfiguration.setConfigurationName(configurationName);
		paymentProviderConfiguration.setDefaultDisplayName(defaultDisplayName);
		paymentProviderConfiguration.setPaymentConfigurationData(paymentConfigurationData);
		paymentProviderConfiguration.setPaymentProviderPluginId(paymentProviderPluginId);
		paymentProviderConfiguration.setStatus(status);
		paymentProviderConfiguration.setPaymentLocalizedProperties(paymentLocalizedProperties);
		return paymentProviderConfiguration;
	}
}
