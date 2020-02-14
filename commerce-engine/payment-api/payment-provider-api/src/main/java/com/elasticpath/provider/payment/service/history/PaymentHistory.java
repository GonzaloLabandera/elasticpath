/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.history;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Represents an interface of a helper service that would help scavenge through the list of PaymentEvents and give out useful information.
 */
public interface PaymentHistory {

	/**
	 * Returns list of MoneyDTO for available reserved amount.
	 *
	 * @param paymentEvents list of payment events.
	 * @return money amount.
	 */
	MoneyDTO getAvailableReservedAmount(List<PaymentEvent> paymentEvents);

	/**
	 * Returns committed amount.
	 *
	 * @param paymentEvents list of payment events.
	 * @return MoneyDTO.
	 */
	MoneyDTO getChargedAmount(List<PaymentEvent> paymentEvents);

	/**
	 * Returns refunded amount.
	 *
	 * @param paymentEvents list of payment events.
	 * @return MoneyDTO.
	 */
	MoneyDTO getRefundedAmount(List<PaymentEvent> paymentEvents);

	/**
	 * Returns the map of Payment instruments that have reservation on them and can be charged, with the amount available.
	 *
	 * @param paymentEvents list of payment events.
	 * @return map of payment instruments with the amount available.
	 */
	Multimap<PaymentEvent, MoneyDTO> getChargeablePaymentEvents(List<PaymentEvent> paymentEvents);

	/**
	 * Returns the map of Payment instruments that have some charges already committed on them.
	 *
	 * @param paymentEvents list of payment events.
	 * @return map of payment instruments with the amount available.
	 */
	Multimap<PaymentEvent, MoneyDTO> getRefundablePaymentEvents(List<PaymentEvent> paymentEvents);

	/**
	 * Returns reservable order payment instruments.
	 *
	 * @param ledger list of payment events.
	 * @param orderPaymentInstruments list of order payment instruments.
	 * @return reservable order payment instruments.
	 */
	Map<OrderPaymentInstrumentDTO, MoneyDTO> getReservableOrderPaymentInstruments(List<PaymentEvent> ledger,
																				  List<OrderPaymentInstrumentDTO> orderPaymentInstruments);

}
