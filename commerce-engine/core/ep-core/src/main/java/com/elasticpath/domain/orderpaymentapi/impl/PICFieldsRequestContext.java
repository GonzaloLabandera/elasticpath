/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi.impl;

import java.util.Currency;
import java.util.Locale;

/**
 * <p>Interface encapsulating information required by payment plugins and their corresponding providers
 * when querying the relevant fields from the plugin during the payment instrument creation flow.</p>
 * <p>Extend this class to enrich such requests, i.e. in cases where a payment provider requires
 * additional information when querying plugin fields.</p>
 */
public class PICFieldsRequestContext {
	private Currency currency;
	private Locale locale;
	private CustomerContext customerContext;

	/**
	 * Constructor.
	 *
	 * @param currency        {@link Currency}
	 * @param locale          {@link Locale}
	 * @param customerContext {@link CustomerContext} containing customer information required during the payment instrument creation process
	 */
	public PICFieldsRequestContext(final Currency currency, final Locale locale, final CustomerContext customerContext) {
		this.currency = currency;
		this.locale = locale;
		this.customerContext = customerContext;
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
	 * Returns the relevant locale for this request.
	 *
	 * @param locale for the request
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
}
