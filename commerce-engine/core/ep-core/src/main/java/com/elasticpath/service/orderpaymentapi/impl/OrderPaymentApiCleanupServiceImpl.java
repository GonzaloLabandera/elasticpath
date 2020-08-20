/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.orderpaymentapi.impl;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;

/**
 * Default implementation of {@link OrderPaymentApiCleanupService}.
 */
public class OrderPaymentApiCleanupServiceImpl implements OrderPaymentApiCleanupService {

	private static final String LIST_PARAMETER_NAME = "list";

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

	@Override
	public void removeByOrderUidList(final List<Long> orderUidList) {
		sanityCheck();

		getPersistenceEngine().executeNamedQueryWithList(
				"DELETE_ALL_ORDER_PAYMENTS_BY_ORDER_UID_LIST", LIST_PARAMETER_NAME, orderUidList);
		getPersistenceEngine().executeNamedQueryWithList(
				"DELETE_ALL_ORDER_PAYMENT_INSTRUMENTS_BY_ORDER_UID_LIST", LIST_PARAMETER_NAME, orderUidList);
		getPersistenceEngine().flush();
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
