/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.cancel;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_RESERVATION_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Cancel reservation request builder.
 */
public final class CancelReservationRequestBuilder {
	private List<PaymentEvent> ledger;
	private Map<String, String> customRequestData;
	private List<PaymentEvent> selectedPaymentEventsToCancel;
	private MoneyDTO amount;
	private OrderContext orderContext;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;

	private CancelReservationRequestBuilder() {
	}

	/**
	 * An cancel reservation request builder.
	 *
	 * @return the builder
	 */
	public static CancelReservationRequestBuilder builder() {
		return new CancelReservationRequestBuilder();
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger the ledger
	 * @return the builder
	 */
	public CancelReservationRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * With custom data required by the plugin builder.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public CancelReservationRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With selectedPaymentEventsToCancel builder.
	 *
	 * @param selectedPaymentEventsToCancel the orderPaymentInstruments
	 * @return the builder
	 */
	public CancelReservationRequestBuilder withSelectedPaymentEventsToCancel(final List<PaymentEvent> selectedPaymentEventsToCancel) {
		this.selectedPaymentEventsToCancel = selectedPaymentEventsToCancel;
		return this;
	}

	/**
	 * With amount builder.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public CancelReservationRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext order context
	 * @return the builder
	 */
	public CancelReservationRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * With orderPaymentInstruments builder.
	 *
	 * @param orderPaymentInstruments the order payment instruments list
	 * @return the builder
	 */
	public CancelReservationRequestBuilder withOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
		return this;
	}

	/**
	 * Build cancel reservation request.
	 *
	 * @param beanFactory EP bean factory
	 * @return cancel reservation request
	 */
	public CancelReservationRequest build(final BeanFactory beanFactory) {
		if (orderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstruments list is missing");
		}
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
		}
		if (amount == null) {
			throw new IllegalStateException("Builder is not fully initialized, amount is missing");
		}
		if (selectedPaymentEventsToCancel == null) {
			throw new IllegalStateException("Builder is not fully initialized, selectedPaymentEventsToCancel list is missing");
		}
		if (customRequestData == null) {
			throw new IllegalStateException("Builder is not fully initialized, customRequestData map is missing");
		}
		if (orderContext == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderContext is missing");
		}
		final CancelReservationRequest request = beanFactory.getPrototypeBean(
				CANCEL_RESERVATION_REQUEST, CancelReservationRequest.class);
		request.setCustomRequestData(customRequestData);
		request.setOrderContext(orderContext);
		request.setLedger(ledger);
		request.setSelectedPaymentEventsToCancel(selectedPaymentEventsToCancel);
		request.setAmount(amount);
		request.setOrderPaymentInstruments(orderPaymentInstruments);
		return request;
	}
}
