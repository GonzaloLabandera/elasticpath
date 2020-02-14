/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi;

import com.elasticpath.persistence.api.Entity;

/**
 * Links a customer to a payment instrument.
 */
public interface CustomerPaymentInstrument extends Entity {

	/**
	 * Get the payment instrument guid.
	 *
	 * @return payment instrument guid
	 */
	String getPaymentInstrumentGuid();

	/**
	 * Sets the payment instrument guid.
	 *
	 * @param paymentInstrumentGuid payment instrument guid
	 */
	void setPaymentInstrumentGuid(String paymentInstrumentGuid);

    /**
     * Get the customer UID.
     *
     * @return customer UID
     */
    long getCustomerUid();

    /**
     * Set the customer UID.
     *
     * @param customerUid customer UID
     */
    void setCustomerUid(long customerUid);

}
