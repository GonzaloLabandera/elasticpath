/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.plugin.payment.provider.dto;

import java.util.Currency;
import java.util.Locale;

/**
 * PICRequestContextDTO builder.
 */
public final class PICRequestContextDTOBuilder {
	private Locale locale;
	private Currency currency;
	private AddressDTO addressDTO;
	private CustomerContextDTO customerContextDTO;

	private PICRequestContextDTOBuilder() {
	}

	/**
	 * PICRequestContextDTO builder.
	 *
	 * @return the builder
	 */
	public static PICRequestContextDTOBuilder builder() {
		return new PICRequestContextDTOBuilder();
	}

	/**
	 * With Locale.
	 *
	 * @param locale {@link Locale}.
	 * @return the builder
	 */
	public PICRequestContextDTOBuilder withLocale(final Locale locale) {
		this.locale = locale;
		return this;
	}

	/**
	 * With Currency.
	 *
	 * @param currency {@link Currency}.
	 * @return the builder
	 */
	public PICRequestContextDTOBuilder withCurrency(final Currency currency) {
		this.currency = currency;
		return this;
	}

	/**
	 * With AddressDTO.
	 *
	 * @param addressDTO {@link AddressDTO}.
	 * @return the builder
	 */
	public PICRequestContextDTOBuilder withAddressDTO(final AddressDTO addressDTO) {
		this.addressDTO = addressDTO;
		return this;
	}

	/**
	 * With CustomerContextDTO.
	 *
	 * @param customerContextDTO {@link CustomerContextDTO}.
	 * @return the builder
	 */
	public PICRequestContextDTOBuilder withCustomerContextDTO(final CustomerContextDTO customerContextDTO) {
		this.customerContextDTO = customerContextDTO;
		return this;
	}

	/**
	 * Build PICRequestContext DTO.
	 *
	 * @param prototype bean prototype
	 * @return PICRequestContext DTO
	 */
	public PICRequestContextDTO build(final PICRequestContextDTO prototype) {
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
		prototype.setAddressDTO(addressDTO);
		prototype.setCustomerContextDTO(customerContextDTO);
		return prototype;
	}
}
