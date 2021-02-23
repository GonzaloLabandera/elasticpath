/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.orderpaymentapi.impl;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;

/**
 * Default implementation of {@link OrderPaymentApiCleanupService}.
 */
public class OrderPaymentApiCleanupServiceImpl implements OrderPaymentApiCleanupService {

	private PersistenceEngine persistenceEngine;

	@Override
	public void removeByCustomer(final Customer customer) {
		sanityCheck();

		getPersistenceEngine().executeNamedQuery("DELETE_ALL_CUSTOMER_PAYMENT_INSTRUMENTS_BY_CUSTOMER", customer.getUidPk());
	}

	@Override
	public void removeByOrder(final Order order) {
        sanityCheck();

        getPersistenceEngine().executeNamedQuery("DELETE_ORDER_PAYMENT_INSTRUMENT_BY_ORDER_NUMBER", order.getOrderNumber());
    }

	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * Get persistence engine.
	 *
	 * @return persistence engine
	 */
	protected PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	/**
	 * Sanity check of this service instance.
	 *
	 * @throws EpServiceException - if something goes wrong.
	 */
	protected void sanityCheck() throws EpServiceException {
		if (getPersistenceEngine() == null) {
			throw new EpServiceException("The persistence engine is not correctly initialized.");
		}
	}

}
