/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment;

import com.elasticpath.persistence.api.Persistable;

/**
 * Represents a payment localized property value.
 */
public interface PaymentLocalizedPropertyValue extends Persistable {

	/**
	 * Get the value.
	 * @return the value
	 */
	String getValue();

	/**
	 * Set the value.
	 *
	 * @param value the value
	 */
	void setValue(String value);

	/**
	 * Get the localized property key.
	 *
	 * @return the key
	 */
	String getPaymentLocalizedPropertyKey();

	/**
	 * Set the localized property key.
	 *
	 * @param localizedPropertyKey the key
	 */
	void setPaymentLocalizedPropertyKey(String localizedPropertyKey);

}