/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.processor;

import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIResponse;
import com.elasticpath.provider.payment.domain.transaction.reservation.ReserveRequest;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Payment reservation processor.
 */
public interface ReservationProcessor {
	/**
	 * Payment reservation processing.
	 *
	 * @param reserveRequest the reserve request
	 * @return list of payment events
	 */
	PaymentAPIResponse reserve(ReserveRequest reserveRequest);

	/**
	 * Payment reservation processing to simulate modify reservation when modify reservation capability is absent.
	 *
	 * @param amount the amount of money to reserve
	 * @param paymentInstrument the instrument on which to make reservation
	 * @param customRequestData additional data to be passed to payment provider
	 * @param orderContext context of the current order
	 * @return list of payment events
	 */
	PaymentAPIResponse reserveToSimulateModify(MoneyDTO amount, OrderPaymentInstrumentDTO paymentInstrument,
											   Map<String, String> customRequestData, OrderContext orderContext);
}
