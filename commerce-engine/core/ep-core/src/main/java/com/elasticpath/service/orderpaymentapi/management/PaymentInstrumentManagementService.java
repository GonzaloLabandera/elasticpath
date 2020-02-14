/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.orderpaymentapi.management;

import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Service for accessing payment instruments from Cortex.
 */
public interface PaymentInstrumentManagementService {

	/**
	 * Retrieve the payment instrument with the given guid.
	 *
	 * @param paymentInstrumentGuid the guid
	 * @return {@link PaymentInstrumentDTO} mapped from the entity
	 */
	PaymentInstrumentDTO getPaymentInstrument(String paymentInstrumentGuid);

	/**
	 * Find instruments that were used for the order.
	 *
	 * @param order the order
	 * @return instruments
	 */
	List<PaymentInstrumentDTO> findOrderInstruments(Order order);

	/**
	 * Find unlimited instruments that were used for the order.
	 * </p>
	 * Unless split payments feature is supported only one unlimited instrument allowed per order,
	 * and such instrument presence is mandatory, so this method will always return list of size 1.
	 *
	 * @param order the order
	 * @return instruments
	 */
	List<PaymentInstrumentDTO> findUnlimitedOrderInstruments(Order order);

	/**
	 * Retrieve the payment instrument with the given payment instrument guid.
	 *
	 * @param orderPaymentInstrumentGuid order payment instrument guid
	 * @return {@link PaymentInstrumentDTO} mapped from the entity
	 */
	PaymentInstrumentDTO findByOrderPaymentInstrumentGuid(String orderPaymentInstrumentGuid);
}
