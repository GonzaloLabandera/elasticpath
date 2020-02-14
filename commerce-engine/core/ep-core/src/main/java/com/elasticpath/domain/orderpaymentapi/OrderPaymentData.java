/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.orderpaymentapi;

import com.elasticpath.persistence.api.Persistable;

/**
 * Additional key value pairs to add information in order payment.
 */
public interface OrderPaymentData extends Persistable {

	/**
	 * Sets the key.
	 * @param key key
	 */
	void setKey(String key);

	/**
	 * Get the key.
	 * @return key
	 */
	String getKey();

	/**
	 * Sets the value.
	 * @param value value
	 */
	void setValue(String value);

	/**
	 * Get the value.
	 * @return value
	 */
	String getValue();
}
