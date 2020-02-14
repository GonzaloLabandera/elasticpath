/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.core.helpers.store;

import com.elasticpath.domain.orderpaymentapi.StorePaymentProviderConfig;

/**
 * Store payment provider configurations model.
 */
public class StorePaymentConfigurationModel {

	private String configurationName;

	private String configurationGuid;

	private String provider;

	private String method;

	private boolean selected;

	private String storeCode;

	private StorePaymentProviderConfig storePaymentProviderConfig;

	/**
	 * Create a StorePaymentConfigurationModel.
	 *
	 * @param storePaymentProviderConfig the store payment provider configuration
	 * @param configurationName the config name
	 * @param configurationGuid the provider guid
	 * @param provider          the provider
	 * @param method            the methodd
	 * @param selected          is it used
	 */
	public StorePaymentConfigurationModel(
			final StorePaymentProviderConfig storePaymentProviderConfig, final String configurationName, final String configurationGuid,
			final String provider, final String method, final boolean selected) {
		this.storePaymentProviderConfig = storePaymentProviderConfig;
		this.configurationName = configurationName;
		this.configurationGuid = configurationGuid;
		this.provider = provider;
		this.method = method;
		this.selected = selected;
	}

	/**
	 * Get the store code.
	 *
	 * @return the store code
	 */
	public String getStoreCode() {
		return storeCode;
	}


	public String getConfigurationName() {
		return configurationName;
	}

	public void setConfigurationName(final String configurationName) {
		this.configurationName = configurationName;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(final String provider) {
		this.provider = provider;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(final String method) {
		this.method = method;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(final boolean selected) {
		this.selected = selected;
	}

	public StorePaymentProviderConfig getStorePaymentProviderConfig() {
		return storePaymentProviderConfig;
	}

	public void setStorePaymentProviderConfig(final StorePaymentProviderConfig storePaymentProviderConfig) {
		this.storePaymentProviderConfig = storePaymentProviderConfig;
	}


	public String getConfigurationGuid() {
		return configurationGuid;
	}

	public void setConfigurationGuid(final String configurationGuid) {
		this.configurationGuid = configurationGuid;
	}

	/**
	 * Set the store code.
	 *
	 * @param storeCode the store code
	 */
	public void setStoreCode(final String storeCode) {
		this.storeCode = storeCode;
	}
}
