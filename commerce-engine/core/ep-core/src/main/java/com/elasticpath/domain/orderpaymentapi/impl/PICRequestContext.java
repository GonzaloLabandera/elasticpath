/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi.impl;

import java.util.Currency;
import java.util.Locale;

import com.elasticpath.common.dto.AddressDTO;

/**
 * <p>Class encapsulating information required by payment plugins and their corresponding providers
 * during the payment instrument creation flow.</p>
 * <p>Extend this class to enrich such requests, i.e. in cases where a payment provider requires
 * additional information beyond what is currently provided here.</p>
 */
public class PICRequestContext {
	private Currency currency;
	private Locale locale;
	private CustomerContext customerContext;
	private AddressDTO billingAddress;

	/**
	 * Constructor for PICRequestContext.
	 *
	 * @param currency        {@link Currency}.
	 * @param locale          {@link Locale}.
	 * @param customerContext {@link CustomerContext} containing customer information required during the payment instrument creation process.
	 * @param billingAddress  {@link AddressDTO} representing the selected billing address.
	 */
	public PICRequestContext(final Currency currency, final Locale locale, final CustomerContext customerContext,
							 final AddressDTO billingAddress) {
		this.currency = currency;
		this.locale = locale;
		this.customerContext = customerContext;
		this.billingAddress = billingAddress;
	}

	/**
	 * Returns the relevant currency for this request.
	 *
	 * @return currency for the request
	 */
	public Currency getCurrency() {
		return currency;
	}

	/**
	 * Sets the {@link Currency} for this request.
	 *
	 * @param currency {@link Currency}
	 */
	public void setCurrency(final Currency currency) {
		this.currency = currency;
	}

	/**
	 * Returns the relevant locale for this request.
	 *
	 * @return locale for the request
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Sets the {@link Locale} for this request.
	 *
	 * @param locale {@link Locale}
	 */
	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	/**
	 * Returns the {@link CustomerContext} for this request.
	 *
	 * @return the {@link CustomerContext}
	 */
	public CustomerContext getCustomerContext() {
		return customerContext;
	}

	/**
	 * Sets the {@link CustomerContext} for this request.
	 *
	 * @param customerContext {@link CustomerContext}
	 */
	public void setCustomerContext(final CustomerContext customerContext) {
		this.customerContext = customerContext;
	}

	/**
	 * Returns the customer's billing address for this request.
	 *
	 * @return {@link AddressDTO} representing the billing address
	 */
	public AddressDTO getBillingAddress() {
		return billingAddress;
	}

	/**
	 * Sets the {@link AddressDTO} for this request.
	 *
	 * @param billingAddress {@link AddressDTO}
	 */
	public void setBillingAddress(final AddressDTO billingAddress) {
		this.billingAddress = billingAddress;
	}
}
