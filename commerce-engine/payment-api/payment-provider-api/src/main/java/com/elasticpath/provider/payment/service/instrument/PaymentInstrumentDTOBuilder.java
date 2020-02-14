/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instrument;

import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;

/**
 * The payment instrument DTO builder.
 */
public final class PaymentInstrumentDTOBuilder {

	private String guid;
	private String name;
	private Map<String, String> data;
	private String paymentProviderConfigurationGuid;
	private Map<String, String> paymentProviderConfiguration;
	private String billingAddressGuid;
	private boolean singleReservePerPI;
	private boolean supportingMultiCharges;

	private PaymentInstrumentDTOBuilder() {
	}

	/**
	 * A payment instrument DTO builder.
	 *
	 * @return the builder
	 */
	public static PaymentInstrumentDTOBuilder builder() {
		return new PaymentInstrumentDTOBuilder();
	}

	/**
	 * With Single Reserve Per PI.
	 *
	 * @param singleReservePerPI true if order has single reserve per payment instrument.
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withSingleReservePerPI(final boolean singleReservePerPI) {
		this.singleReservePerPI = singleReservePerPI;
		return this;
	}

	/**
	 * With support Multi Charges.
	 *
	 * @param supportingMultiCharges supports Multi Charges
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withSupportingMultiCharges(final boolean supportingMultiCharges) {
		this.supportingMultiCharges = supportingMultiCharges;
		return this;
	}

	/**
	 * With guid builder.
	 *
	 * @param guid the guid
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * With name builder.
	 *
	 * @param name the name
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withName(final String name) {
		this.name = name;
		return this;
	}

	/**
	 * With data builder.
	 *
	 * @param data the data
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withData(final Map<String, String> data) {
		this.data = data;
		return this;
	}

	/**
	 * With payment provider configuration guid builder.
	 *
	 * @param paymentProviderConfigurationGuid the payment provider configuration guid
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withPaymentProviderConfigurationGuid(final String paymentProviderConfigurationGuid) {
		this.paymentProviderConfigurationGuid = paymentProviderConfigurationGuid;
		return this;
	}

	/**
	 * With payment provider configuration builder.
	 *
	 * @param paymentProviderConfiguration the payment provider configuration
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withPaymentProviderConfiguration(final Map<String, String> paymentProviderConfiguration) {
		this.paymentProviderConfiguration = paymentProviderConfiguration;
		return this;
	}

	/**
	 * With billing address guid builder.
	 *
	 * @param billingAddressGuid the billing address guid
	 * @return the builder
	 */
	public PaymentInstrumentDTOBuilder withBillingAddressGuid(final String billingAddressGuid) {
		this.billingAddressGuid = billingAddressGuid;
		return this;
	}


	/**
	 * Build payment instrument DTO.
	 *
	 * @param beanFactory EP bean factory
	 * @return the payment instrument DTO
	 */
	public PaymentInstrumentDTO build(final BeanFactory beanFactory) {
		if (guid == null) {
			throw new IllegalStateException("Builder is not fully initialized, guid is missing");
		}
		if (name == null) {
			throw new IllegalStateException("Builder is not fully initialized, name is missing");
		}
		if (data == null) {
			throw new IllegalStateException("Builder is not fully initialized, data map is missing");
		}
		if (paymentProviderConfigurationGuid == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentProviderConfigurationGuid is missing");
		}
		if (paymentProviderConfiguration == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentProviderConfiguration map is missing");
		}
		final PaymentInstrumentDTO paymentInstrumentDTO = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_INSTRUMENT_DTO, PaymentInstrumentDTO.class);
		paymentInstrumentDTO.setGUID(guid);
		paymentInstrumentDTO.setName(name);
		paymentInstrumentDTO.setData(data);
		paymentInstrumentDTO.setPaymentProviderConfigurationGuid(paymentProviderConfigurationGuid);
		paymentInstrumentDTO.setPaymentProviderConfiguration(paymentProviderConfiguration);
		paymentInstrumentDTO.setBillingAddressGuid(billingAddressGuid);
		paymentInstrumentDTO.setSingleReservePerPI(singleReservePerPI);
		paymentInstrumentDTO.setSupportingMultiCharges(supportingMultiCharges);
		return paymentInstrumentDTO;
	}
}
