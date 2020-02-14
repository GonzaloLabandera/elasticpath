/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain;

import com.elasticpath.persistence.api.Persistable;

/**
 * A specific payment instrument's data.
 */
public interface PaymentInstrumentData extends Persistable {

	/**
	 * Get key.
	 *
	 * @return key
	 */
	String getKey();

	/**
	 * Set key.
	 *
	 * @param key key
	 */
	void setKey(String key);

	/**
	 * Get data.
	 *
	 * @return data
	 */
	String getData();

	/**
	 * Set data.
	 *
	 * @param data data
	 */
	void setData(String data);
}
