/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain;

import java.util.Locale;
import java.util.Set;

import com.elasticpath.persistence.api.Entity;
import com.elasticpath.provider.payment.PaymentLocalizedProperties;

/**
 * The interface for payment provider configuration.
 */
public interface PaymentProviderConfiguration extends Entity {

	/**
	 * Returns the display name of the <code>PaymentProviderConfiguration</code> with the given locale.
	 *
	 * @param locale the locale
	 * @return the display name of the PaymentProviderConfiguration displayName
	 */
	String getDisplayName(Locale locale);

	/**
	 * Returns the default display name of the <code>PaymentProviderConfiguration</code>.
	 *
	 * @return the default display name of the PaymentProviderConfiguration
	 */
	String getDefaultDisplayName();

	/**
	 * Set the default display name of the <code>PaymentProviderConfiguration</code>.
	 *
	 * @param defaultDisplayName the default display name
	 */
	void setDefaultDisplayName(String defaultDisplayName);

	/**
	 * Returns the <code>PaymentLocalizedProperties</code>, i.e. <code>PaymentProviderConfiguration</code> name.
	 *
	 * @return the <code>PaymentLocalizedProperties</code>
	 */
	PaymentLocalizedProperties getPaymentLocalizedProperties();

	/**
	 * Set the <code>PaymentLocalizedProperties</code>, i.e. <code>PaymentProviderConfiguration</code> name.
	 *
	 * @param localizedProperties - the <code>PaymentLocalizedProperties</code>
	 */
	void setPaymentLocalizedProperties(PaymentLocalizedProperties localizedProperties);

	/**
	 * Get payment provider plugin id.
	 *
	 * @return payment provider plugin id
	 */
	String getPaymentProviderPluginId();

	/**
	 * Set payment provider plugin id.
	 *
	 * @param paymentProviderPluginId payment provider plugin id
	 */
	void setPaymentProviderPluginId(String paymentProviderPluginId);

	/**
	 * Get configuration name.
	 *
	 * @return configuration name
	 */
	String getConfigurationName();

	/**
	 * Set configuration name.
	 *
	 * @param configurationName configuration name
	 */
	void setConfigurationName(String configurationName);

	/**
	 * Get payment configuration data.
	 *
	 * @return payment configuration data
	 */
	Set<PaymentProviderConfigurationData> getPaymentConfigurationData();

	/**
	 * Set payment configuration data.
	 *
	 * @param paymentConfigurationData payment configuration data
	 */
	void setPaymentConfigurationData(Set<PaymentProviderConfigurationData> paymentConfigurationData);

	/**
	 * Get status.
	 *
	 * @return status
	 */
	PaymentProviderConfigurationStatus getStatus();

	/**
	 * Set status, see {@link PaymentProviderConfigurationStatus}.
	 *
	 * @param status status
	 */
	void setStatus(PaymentProviderConfigurationStatus status);

}
