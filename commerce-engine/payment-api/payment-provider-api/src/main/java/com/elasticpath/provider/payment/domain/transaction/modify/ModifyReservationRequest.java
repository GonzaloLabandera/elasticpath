/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.modify;

import java.util.List;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.domain.transaction.PaymentAPIRequest;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * The Modify Reservation request for Payment API.
 */
public class ModifyReservationRequest implements PaymentAPIRequest {
	private List<PaymentEvent> ledger;
	private Map<String, String> customRequestData;
	private MoneyDTO amount;
	private OrderContext orderContext;
	private boolean finalPayment;
	private boolean singleReservePerPI;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;


	/**
	 * Gets the flag that order has single reserve per payment instrument.
	 *
	 * @return true if order has single reserve per payment instrument.
	 */
	public boolean hasSingleReservePerPI() {
		return singleReservePerPI;
	}

	/**
	 * Sets the flag order has single reserve per payment instrument or not.
	 *
	 * @param singleReservePerPI true if order has single reserve per payment instrument.
	 */
	public void setSingleReservePerPI(final boolean singleReservePerPI) {
		this.singleReservePerPI = singleReservePerPI;
	}

	/**
	 * Gets the modifier that triggers reserving sum of money left after charge.
	 * Usually only last charge should use up the reservation - all others should create extra reservation to track the leftovers.
	 *
	 * @return true if this is not the last charge and we still want to keep the reservation.
	 */
	public boolean isFinalPayment() {
		return finalPayment;
	}

	/**
	 * Sets the flag is this final payment for Order or not.
	 *
	 * @param finalPayment true if this is the last payment, false - otherwise.
	 */
	public void setFinalPayment(final boolean finalPayment) {
		this.finalPayment = finalPayment;
	}

	/**
	 * Gets instruments list.
	 *
	 * @return the instruments list
	 */
	public List<OrderPaymentInstrumentDTO> getOrderPaymentInstruments() {
		return orderPaymentInstruments;
	}

	/**
	 * Sets instruments list.
	 *
	 * @param orderPaymentInstruments the instruments list
	 */
	public void setOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
	}

	/**
	 * Gets ledger.
	 *
	 * @return the ledger
	 */
	public List<PaymentEvent> getLedger() {
		return ledger;
	}

	/**
	 * Sets ledger.
	 *
	 * @param ledger the ledger
	 */
	public void setLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
	}

	@Override
	public Map<String, String> getCustomRequestData() {
		return customRequestData;
	}

	@Override
	public void setCustomRequestData(final Map<String, String> data) {
		this.customRequestData = data;
	}

	/**
	 * Gets amount.
	 *
	 * @return the amount
	 */
	public MoneyDTO getAmount() {
		return amount;
	}

	/**
	 * Sets amount.
	 *
	 * @param amount the amount
	 */
	public void setAmount(final MoneyDTO amount) {
		this.amount = amount;
	}

	@Override
	public OrderContext getOrderContext() {
		return orderContext;
	}

	@Override
	public void setOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
	}
}
