/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.orderpaymentapi;

import java.util.Collection;
import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.service.EpPersistenceService;

/**
 * Service for saving, deleting and retrieving {@link OrderPayment}.
 */
public interface OrderPaymentService extends EpPersistenceService {

	/**
	 * Save or update an {@link OrderPayment}.
	 *
	 * @param orderPayment {@link OrderPayment} to save or update
	 * @return persisted {@link OrderPayment}
	 */
	OrderPayment saveOrUpdate(OrderPayment orderPayment);

	/**
	 * Delete a persisted {@link OrderPayment}.
	 *
	 * @param orderPayment persisted {@link OrderPayment}
	 */
	void remove(OrderPayment orderPayment);

	/**
	 * Find an {@link OrderPayment} entity by UID.
	 *
	 * @param uidPk the UID of {@link OrderPayment}
	 * @return persisted {@link OrderPayment}
	 */
	OrderPayment findByUid(long uidPk);

	/**
	 * Find an {@link OrderPayment} by guid.
	 *
	 * @param guid the guid of {@link OrderPayment}
	 * @return persisted {@link OrderPayment}
	 */
	OrderPayment findByGuid(String guid);

	/**
	 * Find all {@link OrderPayment} by {@link com.elasticpath.provider.payment.domain.PaymentInstrument} guid.
	 *
	 * @param paymentInstrumentGuid {@link com.elasticpath.provider.payment.domain.PaymentInstrument} guid
	 * @return {@link OrderPayment} ordered list
	 */
	List<OrderPayment> findByPaymentInstrumentGuid(String paymentInstrumentGuid);

	/**
	 * Find all {@link OrderPayment} by {@link com.elasticpath.domain.order.Order}.
	 *
	 * @param order the {@link com.elasticpath.domain.order.Order} entity
	 * @return {@link OrderPayment} ordered list
	 */
	Collection<OrderPayment> findByOrder(Order order);
}
