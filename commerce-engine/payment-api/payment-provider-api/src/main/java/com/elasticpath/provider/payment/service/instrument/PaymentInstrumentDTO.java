/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.instrument;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * A DTO object of a payment Instrument, for example an individual credit card or gift certificate.
 */
public class PaymentInstrumentDTO {

	private String guid;
	private String name;
	private Map<String, String> data;
	private String paymentProviderConfigurationGuid;
	private Map<String, String> paymentProviderConfiguration;
	private String billingAddressGuid;
	private boolean singleReservePerPI;
	private boolean supportingMultiCharges;

	/**
	 * Returns true if payment instrument is single time use only.
	 *
	 * @return true if payment instrument is single time use only.
	 */
	public boolean isSingleReservePerPI() {
		return singleReservePerPI;
	}

	/**
	 * Set the payment instrument to be used single time only.
	 *
	 * @param singleReservePerPI true if order has single reserve per payment instrument.
	 */
	public void setSingleReservePerPI(final boolean singleReservePerPI) {
		this.singleReservePerPI = singleReservePerPI;
	}

	/**
	 * Returns true if payment instrument supports multi charges, else returns false.
	 *
	 * @return true if payment instrument supports multi charges, else returns false
	 */
	public boolean isSupportingMultiCharges() {
		return supportingMultiCharges;
	}

	/**
	 * Set the payment instrument supports Multi Charges.
	 *
	 * @param supportingMultiCharges the supports Multi Charges
	 */
	public void setSupportingMultiCharges(final boolean supportingMultiCharges) {
		this.supportingMultiCharges = supportingMultiCharges;
	}

	/**
	 * Gets guid.
	 *
	 * @return the guid
	 */
	public String getGUID() {
		return guid;
	}

	/**
	 * Sets guid.
	 *
	 * @param guid the guid
	 */
	public void setGUID(final String guid) {
		this.guid = guid;
	}

	/**
	 * Returns the name.
	 *
	 * @return the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the name.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Returns the Payment Instrument data, representing the dynamic fields attached to this Payment Instrument.
	 *
	 * @return a map representing the Payment Instrument's dynamic fields.
	 */
	public Map<String, String> getData() {
		return data;
	}

	/**
	 * Sets the Payment Instrument data, representing dynamic fields attached to this Payment Instrument.
	 *
	 * @param data a map of the dynamic fields to attach to this Payment Instrument.
	 */
	public void setData(final Map<String, String> data) {
		this.data = ImmutableMap.copyOf(data);
	}

	/**
	 * Gets payment provider configuration guid.
	 *
	 * @return the payment provider configuration guid
	 */
	public String getPaymentProviderConfigurationGuid() {
		return paymentProviderConfigurationGuid;
	}

	/**
	 * Sets payment provider configuration guid.
	 *
	 * @param paymentProviderConfigurationGuid the payment provider configuration guid
	 */
	public void setPaymentProviderConfigurationGuid(final String paymentProviderConfigurationGuid) {
		this.paymentProviderConfigurationGuid = paymentProviderConfigurationGuid;
	}

	/**
	 * Gets payment provider configuration for the PI.
	 *
	 * @return the payment provider configuration
	 */
	public Map<String, String> getPaymentProviderConfiguration() {
		return paymentProviderConfiguration;
	}

	/**
	 * Sets payment provider configuration for the PI.
	 *
	 * @param paymentProviderConfiguration the payment provider configuration
	 */
	public void setPaymentProviderConfiguration(final Map<String, String> paymentProviderConfiguration) {
		this.paymentProviderConfiguration = ImmutableMap.copyOf(paymentProviderConfiguration);
	}

	/**
	 * Gets billing address guid.
	 *
	 * @return the billing address guid.
	 */
	public String getBillingAddressGuid() {
		return billingAddressGuid;
	}

	/**
	 * Sets billing address guid.
	 *
	 * @param billingAddressGuid the billing address guid.
	 */
	public void setBillingAddressGuid(final String billingAddressGuid) {
		this.billingAddressGuid = billingAddressGuid;
	}
}
