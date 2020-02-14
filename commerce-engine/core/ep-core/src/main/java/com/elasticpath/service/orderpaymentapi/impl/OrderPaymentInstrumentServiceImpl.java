/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.orderpaymentapi.impl;

import java.util.Collection;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;

/**
 * Default implementation of {@link OrderPaymentInstrumentService}.
 */
public class OrderPaymentInstrumentServiceImpl extends AbstractEpPersistenceServiceImpl implements OrderPaymentInstrumentService {

	@Override
	public OrderPaymentInstrument saveOrUpdate(final OrderPaymentInstrument orderPaymentInstrument) {
		sanityCheck();
		return getPersistenceEngine().saveOrUpdate(orderPaymentInstrument);
	}

	@Override
	public void remove(final OrderPaymentInstrument orderPaymentInstrument) {
		sanityCheck();
		getPersistenceEngine().delete(orderPaymentInstrument);
	}

	@Override
	public OrderPaymentInstrument findByGuid(final String guid) {
		sanityCheck();

		return (OrderPaymentInstrument) getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_PAYMENT_INSTRUMENT_BY_GUID", guid)
				.stream()
				.findFirst()
				.orElse(null);
	}

	@Override
	public Collection<OrderPaymentInstrument> findByOrder(final Order order) {
        sanityCheck();

        return getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_PAYMENT_INSTRUMENT_BY_ORDER_NUMBER_ALL", order.getOrderNumber());
    }

    @Override
    public OrderPaymentInstrument findByOrderPayment(final OrderPayment orderPayment) {
        sanityCheck();

        return (OrderPaymentInstrument) getPersistenceEngine().retrieveByNamedQuery(
                "FIND_ORDER_PAYMENT_INSTRUMENT_BY_ORDER_NUMBER_AND_PAYMENT_INSTRUMENT_GUID",
                orderPayment.getOrderNumber(), orderPayment.getPaymentInstrumentGuid())
                .stream()
                .findFirst()
                .orElse(null);
    }

	@Override
	public Object getObject(final long uid) {
		sanityCheck();

		return getPersistenceEngine().retrieveByNamedQuery("FIND_ORDER_PAYMENT_INSTRUMENT_BY_UID", uid)
				.stream()
				.findFirst()
				.orElse(null);
	}
}
