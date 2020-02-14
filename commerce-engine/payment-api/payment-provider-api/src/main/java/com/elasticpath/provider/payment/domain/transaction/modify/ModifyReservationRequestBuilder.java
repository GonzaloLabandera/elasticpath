/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.modify;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MODIFY_RESERVATION_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Modify reservation request builder.
 */
public final class ModifyReservationRequestBuilder {
	private List<PaymentEvent> ledger;
	private Map<String, String> customRequestData;
	private MoneyDTO amount;
	private OrderContext orderContext;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;
	private boolean finalPayment;
	private boolean hasSingleReservePerPI;

	private ModifyReservationRequestBuilder() {
	}

	/**
	 * With the flag to do reserve leftovers builder.
	 *
	 * @param finalPayment the flag to do reserve leftovers
	 * @return the builder
	 */
	public ModifyReservationRequestBuilder withFinalPayment(final boolean finalPayment) {
		this.finalPayment = finalPayment;
		return this;
	}
	/**
	 * An modify reservation request builder.
	 *
	 * @return the builder
	 */
	public static ModifyReservationRequestBuilder builder() {
		return new ModifyReservationRequestBuilder();
	}

	/**
	 * With the flag order has a single reserve per payment instrument builder.
	 *
	 * @param hasSingleReservePerPI the flag order has a single reserve per payment instrument
	 * @return the builder
	 */
	public ModifyReservationRequestBuilder withSingleReservePerPI(final boolean hasSingleReservePerPI) {
		this.hasSingleReservePerPI = hasSingleReservePerPI;
		return this;
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger the ledger
	 * @return the builder
	 */
	public ModifyReservationRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * With custom data required by the plugin builder.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public ModifyReservationRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With amount builder.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public ModifyReservationRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext the order context
	 * @return the builder
	 */
	public ModifyReservationRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * With orderPaymentInstruments builder.
	 *
	 * @param orderPaymentInstruments the order payment instruments list
	 * @return the builder
	 */
	public ModifyReservationRequestBuilder withOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
		return this;
	}

	/**
	 * Build modify reservation request.
	 *
	 * @param beanFactory EP bean factory
	 * @return modify reservation request
	 */
	public ModifyReservationRequest build(final BeanFactory beanFactory) {
		if (orderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstruments list is missing");
		}
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
		}
		if (amount == null) {
			throw new IllegalStateException("Builder is not fully initialized, amount is missing");
		}
		if (customRequestData == null) {
			throw new IllegalStateException("Builder is not fully initialized, customRequestData map is missing");
		}
		if (orderContext == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderContext is missing");
		}
		final ModifyReservationRequest request = beanFactory.getPrototypeBean(
				MODIFY_RESERVATION_REQUEST, ModifyReservationRequest.class);
		request.setCustomRequestData(customRequestData);
		request.setLedger(ledger);
		request.setAmount(amount);
		request.setOrderContext(orderContext);
		request.setOrderPaymentInstruments(orderPaymentInstruments);
		request.setFinalPayment(finalPayment);
		request.setSingleReservePerPI(hasSingleReservePerPI);
		return request;
	}
}
