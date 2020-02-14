/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.orderpaymentapi;

import com.elasticpath.persistence.api.Persistable;

/**
 * Customer default payment instrument association as an entity.
 */
public interface CustomerDefaultPaymentInstrument extends Persistable {

	/**
	 * Get the default {@link CustomerPaymentInstrument}.
	 *
	 * @return default customer instrument entity
	 */
	CustomerPaymentInstrument getCustomerPaymentInstrument();

	/**
	 * Set the default {@link CustomerPaymentInstrument}.
	 *
	 * @param customerPaymentInstrument default customer instrument entity
	 */
	void setCustomerPaymentInstrument(CustomerPaymentInstrument customerPaymentInstrument);

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
