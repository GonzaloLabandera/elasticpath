/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

import java.util.Currency;
import java.util.Locale;

/**
 * PICFieldsRequestContextDTO builder.
 */
public final class PICFieldsRequestContextDTOBuilder {
	private Locale locale;
	private Currency currency;
	private CustomerContextDTO customerContextDTO;

	private PICFieldsRequestContextDTOBuilder() {
	}

	/**
	 * PICRequestContextDTO builder.
	 *
	 * @return the builder
	 */
	public static PICFieldsRequestContextDTOBuilder builder() {
		return new PICFieldsRequestContextDTOBuilder();
	}

	/**
	 * With Locale.
	 *
	 * @param locale {@link Locale}.
	 * @return the builder
	 */
	public PICFieldsRequestContextDTOBuilder withLocale(final Locale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * With Currency.
	 *
	 * @param currency {@link Currency}.
	 * @return the builder
	 */
	public PICFieldsRequestContextDTOBuilder withCurrency(final Currency currency) {
		this.currency = currency;
		return this;
	}

	/**
	 * With CustomerContextDTO.
	 *
	 * @param customerContextDTO {@link CustomerContextDTO}.
	 * @return the builder
	 */
	public PICFieldsRequestContextDTOBuilder withCustomerContextDTO(final CustomerContextDTO customerContextDTO) {
		this.customerContextDTO = customerContextDTO;
		return this;
	}

	/**
	 * Build PICFieldsRequestContext DTO.
	 *
	 * @param prototype bean prototype
	 * @return PICFieldsRequestContext DTO
	 */
	public PICFieldsRequestContextDTO build(final PICFieldsRequestContextDTO prototype) {
		if (locale == null) {
			throw new IllegalStateException("Builder is not fully initialized, locale is missing");
		}
		if (currency == null) {
			throw new IllegalStateException("Builder is not fully initialized, currency is missing");
		}
		if (customerContextDTO == null) {
			throw new IllegalStateException("Builder is not fully initialized, customerContextDTO is missing");
		}
		prototype.setLocale(locale);
		prototype.setCurrency(currency);
		prototype.setCustomerContextDTO(customerContextDTO);
		return prototype;
	}
}
