/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.impl;

import java.util.Collection;
import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;

/**
 * Default implementation for {@link OrderPaymentService}.
 */
public class OrderPaymentServiceImpl extends AbstractEpPersistenceServiceImpl implements OrderPaymentService {

	@Override
	public OrderPayment saveOrUpdate(final OrderPayment orderPayment) {
		sanityCheck();

		return getPersistenceEngine().saveOrUpdate(orderPayment);
	}

	@Override
	public void remove(final OrderPayment orderPayment) {
		sanityCheck();

		getPersistenceEngine().delete(orderPayment);
	}

	@Override
	public OrderPayment findByUid(final long uidPk) {
		sanityCheck();

		return (OrderPayment) getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_PAYMENT_BY_UID", uidPk).stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public OrderPayment findByGuid(final String guid) {
		sanityCheck();

		return (OrderPayment) getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_PAYMENT_BY_GUID", guid)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public List<OrderPayment> findByPaymentInstrumentGuid(final String paymentInstrumentGuid) {
		sanityCheck();

		return getPersistenceEngine()
				.retrieveByNamedQuery("FIND_ORDER_PAYMENTS_BY_PAYMENT_INSTRUMENT_ALL", paymentInstrumentGuid);
	}

	@Override
	public Object getObject(final long uid) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_PAYMENT_BY_UID", uid).stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public Collection<OrderPayment> findByOrder(final Order order) {
        sanityCheck();

        return getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_PAYMENTS_BY_ORDER_NUMBER_ALL", order.getOrderNumber());
    }
}
