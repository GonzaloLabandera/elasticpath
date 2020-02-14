/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi;

import com.elasticpath.persistence.api.Entity;

/**
 * Linking a store to a payment provider config.
 */
public interface StorePaymentProviderConfig extends Entity {

	/**
	 * Get the payment provider config guid.
	 *
	 * @return payment provider config guid
	 */
	String getPaymentProviderConfigGuid();

	/**
	 * Sets the payment provider config guid.
	 *
	 * @param paymentProviderConfigGuid payment provider config guid
	 */
	void setPaymentProviderConfigGuid(String paymentProviderConfigGuid);

    /**
     * Get the store code.
     *
     * @return store code
     */
    String getStoreCode();

    /**
     * Set the store code.
     *
     * @param storeCode store code
     */
    void setStoreCode(String storeCode);

}
