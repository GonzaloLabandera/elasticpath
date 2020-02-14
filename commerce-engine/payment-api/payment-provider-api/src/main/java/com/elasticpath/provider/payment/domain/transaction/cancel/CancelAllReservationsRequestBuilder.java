/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.cancel;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CANCEL_ALL_RESERVATIONS_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Cancel reservation request builder.
 */
public final class CancelAllReservationsRequestBuilder {
	private List<PaymentEvent> ledger;
	private Map<String, String> customRequestData;
	private OrderContext orderContext;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;

	private CancelAllReservationsRequestBuilder() {
	}

	/**
	 * An cancel reservation request builder.
	 *
	 * @return the builder
	 */
	public static CancelAllReservationsRequestBuilder builder() {
		return new CancelAllReservationsRequestBuilder();
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger the ledger
	 * @return the builder
	 */
	public CancelAllReservationsRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * With custom data required by the plugin builder.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public CancelAllReservationsRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext order context
	 * @return the builder
	 */
	public CancelAllReservationsRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * With orderPaymentInstruments builder.
	 *
	 * @param orderPaymentInstruments the order payment instruments list
	 * @return the builder
	 */
	public CancelAllReservationsRequestBuilder withOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
		return this;
	}

	/**
	 * Build cancel reservation request.
	 *
	 * @param beanFactory EP bean factory
	 * @return cancel reservation request
	 */
	public CancelAllReservationsRequest build(final BeanFactory beanFactory) {
		if (orderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstruments list is missing");
		}
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
		}
		if (orderContext == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderContext is missing");
		}
		if (customRequestData == null) {
			throw new IllegalStateException("Builder is not fully initialized, customRequestData map is missing");
		}
		final CancelAllReservationsRequest request = beanFactory.getPrototypeBean(
				CANCEL_ALL_RESERVATIONS_REQUEST, CancelAllReservationsRequest.class);
		request.setLedger(ledger);
		request.setOrderContext(orderContext);
		request.setCustomRequestData(customRequestData);
		request.setOrderPaymentInstruments(orderPaymentInstruments);
		return request;
	}
}
