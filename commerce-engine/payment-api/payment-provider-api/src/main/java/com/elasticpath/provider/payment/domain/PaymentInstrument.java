/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain;

import java.util.Set;

import com.elasticpath.persistence.api.Entity;

/**
 * A specific instance of a payment method, for example an individual credit card or gift certificate.
 */
public interface PaymentInstrument extends Entity {

	/**
	 * @return The billing address GUID.
	 */
	String getBillingAddressGuid();

	/**
	 * @param guid The billing address GUID.
	 */
	void setBillingAddressGuid(String guid);

	/**
	 * Returns the name.
	 *
	 * @return the name.
	 */
	String getName();

	/**
	 * Sets the name.
	 *
	 * @param name the name.
	 */
	void setName(String name);

	/**
	 * Returns the Payment Instrument data, representing the dynamic fields attached to this Payment Instrument.
	 *
	 * @return a list representing the Payment Instrument's dynamic fields.
	 */
	Set<PaymentInstrumentData> getPaymentInstrumentData();

	/**
	 * Sets the Payment Instrument data, representing dynamic fields attached to this Payment Instrument.
	 *
	 * @param paymentInstrumentData a list of the dynamic fields to attach to this Payment Instrument.
	 */
	void setPaymentInstrumentData(Set<PaymentInstrumentData> paymentInstrumentData);

	/**
	 * Gets payment provider configuration.
	 *
	 * @return the payment provider configuration
	 */
	PaymentProviderConfiguration getPaymentProviderConfiguration();

	/**
	 * Sets payment provider configuration.
	 *
	 * @param paymentProviderConfiguration the payment provider configuration
	 */
	void setPaymentProviderConfiguration(PaymentProviderConfiguration paymentProviderConfiguration);

	/**
	 * Returns true if payment instrument is single time use only.
	 *
	 * @return true if payment instrument is single time use only.
	 */
	boolean isSingleReservePerPI();

	/**
	 * Set the payment instrument to be used single time only.
	 *
	 * @param singleReservePerPI true if order has single reserve per payment instrument.
	 */
	void setSingleReservePerPI(boolean singleReservePerPI);

	/**
	 * Returns true if payment instrument supports multi charges, else returns false.
	 *
	 * @return true if payment instrument supports multi charges, else returns false
	 */
	boolean isSupportingMultiCharges();

	/**
	 * Set the payment instrument supports Multi Charges.
	 *
	 * @param supportingMultiCharges supports Multi Charges
	 */
	void setSupportingMultiCharges(boolean supportingMultiCharges);
}
