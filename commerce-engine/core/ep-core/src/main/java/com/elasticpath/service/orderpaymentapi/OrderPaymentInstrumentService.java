/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.Collection;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.service.EpPersistenceService;

/**
 * Service for storing and retrieving {@link OrderPaymentInstrument}.
 */
public interface OrderPaymentInstrumentService extends EpPersistenceService {
	/**
	 * Save or update an {@link OrderPaymentInstrument}.
	 *
	 * @param orderPaymentInstrument {@link OrderPaymentInstrument} to save or update
	 * @return {@link OrderPaymentInstrument} entity
	 */
	OrderPaymentInstrument saveOrUpdate(OrderPaymentInstrument orderPaymentInstrument);

	/**
	 * Delete a persisted {@link OrderPaymentInstrument}.
	 *
	 * @param orderPaymentInstrument persisted {@link OrderPaymentInstrument} entity
	 */
	void remove(OrderPaymentInstrument orderPaymentInstrument);

	/**
	 * Find an {@link OrderPaymentInstrument} by guid.
	 *
	 * @param guid guid
	 * @return {@link OrderPaymentInstrument} entity
	 */
	OrderPaymentInstrument findByGuid(String guid);

	/**
	 * Find all {@link OrderPaymentInstrument}s by the {@link Order}.
	 *
	 * @param order the {@link Order} entity
	 * @return collection of {@link OrderPaymentInstrument}s
	 */
	Collection<OrderPaymentInstrument> findByOrder(Order order);

    /**
     * Find the {@link OrderPaymentInstrument} by an {@link com.elasticpath.domain.orderpaymentapi.OrderPayment}.
     *
     * @param orderPayment the {@link OrderPayment}
     * @return {@link OrderPaymentInstrument} entity
     */
    OrderPaymentInstrument findByOrderPayment(OrderPayment orderPayment);
}
