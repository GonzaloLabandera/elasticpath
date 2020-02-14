/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

import java.util.Currency;
import java.util.Locale;

/**
 * Contains additional information in request about Order.
 */
public class PICRequestContextDTO {
	private Locale locale;
	private Currency currency;
	private AddressDTO addressDTO;
	private CustomerContextDTO customerContextDTO;

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
	 * Get addressDTO.
	 *
	 * @return addressDTO.
	 */
	public AddressDTO getAddressDTO() {
		return addressDTO;
	}

	/**
	 * Set addressDTO.
	 *
	 * @param addressDTO addressDTO.
	 */
	public void setAddressDTO(final AddressDTO addressDTO) {
		this.addressDTO = addressDTO;
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
}
