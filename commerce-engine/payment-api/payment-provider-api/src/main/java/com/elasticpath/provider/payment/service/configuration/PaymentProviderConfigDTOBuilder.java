/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.configuration;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_PROVIDER_CONFIGURATION_DTO;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.domain.PaymentProviderConfigurationStatus;

/**
 * A DTO object of a payment provider configuration.
 */
public final class PaymentProviderConfigDTOBuilder {

	private String guid;

	private String paymentProviderPluginBeanName;

	private String configurationName;

	private String defaultDisplayName;

	private Map<String, String> paymentConfigurationData;

	private PaymentProviderConfigurationStatus status = PaymentProviderConfigurationStatus.DRAFT;

	private Map<String, String> localizedNames = new HashMap<>();

	private PaymentProviderConfigDTOBuilder() {
	}

	/**
	 * A payment instrument DTO builder.
	 *
	 * @return the builder
	 */
	public static PaymentProviderConfigDTOBuilder builder() {
		return new PaymentProviderConfigDTOBuilder();
	}

	/**
	 * With guid builder.
	 *
	 * @param guid the guid
	 * @return the builder
	 */
	public PaymentProviderConfigDTOBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * With payment provider plugin bean name builder.
	 *
	 * @param paymentProviderPluginBeanName the payment provider plugin bean name
	 * @return the builder
	 */
	public PaymentProviderConfigDTOBuilder withPaymentProviderPluginBeanName(final String paymentProviderPluginBeanName) {
		this.paymentProviderPluginBeanName = paymentProviderPluginBeanName;
		return this;
	}

	/**
	 * With configuration name builder.
	 *
	 * @param configurationName the configuration name
	 * @return the builder
	 */
	public PaymentProviderConfigDTOBuilder withConfigurationName(final String configurationName) {
		this.configurationName = configurationName;
		return this;
	}

	/**
	 * With default display name builder.
	 *
	 * @param defaultDisplayName the default display name.
	 * @return the builder
	 */
	public PaymentProviderConfigDTOBuilder withDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
		return this;
	}

	/**
	 * With payment configuration data builder.
	 *
	 * @param paymentConfigurationData the payment configuration data
	 * @return the builder
	 */
	public PaymentProviderConfigDTOBuilder withPaymentConfigurationData(final Map<String, String> paymentConfigurationData) {
		this.paymentConfigurationData = paymentConfigurationData;
		return this;
	}

	/**
	 * With payment provider configuration builder.
	 *
	 * @param status the status
	 * @return the builder
	 */
	public PaymentProviderConfigDTOBuilder withStatus(final PaymentProviderConfigurationStatus status) {
		this.status = status;
		return this;
	}

	/**
	 * With payment provider localized names.
	 *
	 * @param localizedNames the map of payment provider configuration localized names
	 * @return the builder
	 */
	public PaymentProviderConfigDTOBuilder withLocalizedNames(final Map<String, String> localizedNames) {
		this.localizedNames = localizedNames;
		return this;
	}


	/**
	 * Build payment provider config DTO.
	 *
	 * @param beanFactory EP bean factory
	 * @return the payment provider config DTO.
	 */
	public PaymentProviderConfigDTO build(final BeanFactory beanFactory) {
		if (guid == null) {
			throw new IllegalStateException("Builder is not fully initialized, guid is missing");
		}
		if (configurationName == null) {
			throw new IllegalStateException("Builder is not fully initialized, configurationName is missing");
		}
		if (paymentConfigurationData == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentConfigurationData map is missing");
		}
		if (paymentProviderPluginBeanName == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentProviderPluginBeanName is missing");
		}
		if (status == null) {
			throw new IllegalStateException("Builder is not fully initialized, status is missing");
		}
		if (localizedNames == null) {
			throw new IllegalStateException("Builder is not fully initialized, localizedNames map is missing");
		}
		final PaymentProviderConfigDTO paymentProviderConfigDTO = beanFactory.getPrototypeBean(
				PAYMENT_PROVIDER_CONFIGURATION_DTO, PaymentProviderConfigDTO.class);
		paymentProviderConfigDTO.setGuid(guid);
		paymentProviderConfigDTO.setConfigurationName(configurationName);
		paymentProviderConfigDTO.setDefaultDisplayName(defaultDisplayName);
		paymentProviderConfigDTO.setPaymentConfigurationData(paymentConfigurationData);
		paymentProviderConfigDTO.setPaymentProviderPluginBeanName(paymentProviderPluginBeanName);
		paymentProviderConfigDTO.setStatus(status);
		paymentProviderConfigDTO.setLocalizedNames(localizedNames);
		return paymentProviderConfigDTO;
	}
}
