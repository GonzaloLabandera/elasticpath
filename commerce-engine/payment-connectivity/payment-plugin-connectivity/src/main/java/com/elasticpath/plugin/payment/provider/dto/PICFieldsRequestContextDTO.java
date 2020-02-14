/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

/**
 * Contains additional information in request about Order.
 */
public class PICFieldsRequestContextDTO {
	private Locale locale;
	private Currency currency;
	private CustomerContextDTO customerContextDTO;
	private Map<String, String> pluginConfigData;

	/**
	 * Get locale.
	 *
	 * @return locale.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Set locale.
	 *
	 * @param locale locale.
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * Get currency.
	 *
	 * @return currency.
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * Set currency.
	 *
	 * @param currency currency.
	 */
	public void setCurrency(final Currency currency) {
		this.currency = currency;
	}

	/**
	 * Get customerContextDTO.
	 *
	 * @return customerContextDTO.
	 */
	public CustomerContextDTO getCustomerContextDTO() {
		return customerContextDTO;
	}

	/**
	 * Set customerContextDTO.
	 *
	 * @param customerContextDTO customerContextDTO.
	 */
	public void setCustomerContextDTO(final CustomerContextDTO customerContextDTO) {
		this.customerContextDTO = customerContextDTO;
	}

	/**
	 * Gets plugin config data.
	 *
	 * @return the plugin config data
	 */
	public Map<String, String> getPluginConfigData() {
		return pluginConfigData;
	}

	/**
	 * Sets plugin config data.
	 *
	 * @param pluginConfigData the plugin config data
	 */
	public void setPluginConfigData(final Map<String, String> pluginConfigData) {
		this.pluginConfigData = pluginConfigData;
	}
}
