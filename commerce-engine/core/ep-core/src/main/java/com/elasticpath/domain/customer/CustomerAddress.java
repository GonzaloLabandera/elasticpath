/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.customer;

/**
 * A customer's address.
 */
public interface CustomerAddress extends Address {
	/**
	 * Return customer uid.
	 * @return the uid.
	 */
	Long getCustomerUidPk();

	/**
	 * Set customer uid.
	 * @param customerUidPk the uid
	 */
	void setCustomerUidPk(Long customerUidPk);
}
