/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.credit;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.REVERSE_CHARGE_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Reverse charge request builder.
 */
public final class ReverseChargeRequestBuilder {
	private Map<String, String> customRequestData;
	private List<PaymentEvent> ledger;
	private List<PaymentEvent> selectedPaymentEvents;
	private OrderContext orderContext;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;

	private ReverseChargeRequestBuilder() {
	}

	/**
	 * An Reverse charge request builder.
	 *
	 * @return the builder
	 */
	public static ReverseChargeRequestBuilder builder() {
		return new ReverseChargeRequestBuilder();
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger the ledger
	 * @return the builder
	 */
	public ReverseChargeRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * With custom data required by the plugin builder.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public ReverseChargeRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With selectedPaymentEvents builder.
	 *
	 * @param selectedPaymentEvents the paymentEvents
	 * @return the builder
	 */
	public ReverseChargeRequestBuilder withSelectedPaymentEvents(final List<PaymentEvent> selectedPaymentEvents) {
		this.selectedPaymentEvents = selectedPaymentEvents;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext order context
	 * @return the builder
	 */
	public ReverseChargeRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * With orderPaymentInstruments builder.
	 *
	 * @param orderPaymentInstruments the order payment instruments list
	 * @return the builder
	 */
	public ReverseChargeRequestBuilder withOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
		return this;
	}

	/**
	 * Build reverse charge request.
	 *
	 * @param beanFactory EP bean factory
	 * @return reverse charge request
	 */
	public ReverseChargeRequest build(final BeanFactory beanFactory) {
		if (orderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstruments list is missing");
		}
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
		}
		if (selectedPaymentEvents == null) {
			throw new IllegalStateException("Builder is not fully initialized, selectedPaymentEvents list is missing");
		}
		if (orderContext == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderContext is missing");
		}
		if (customRequestData == null) {
			throw new IllegalStateException("Builder is not fully initialized, customRequestData map is missing");
		}
		final ReverseChargeRequest request = beanFactory.getPrototypeBean(REVERSE_CHARGE_REQUEST, ReverseChargeRequest.class);
		request.setLedger(ledger);
		request.setSelectedPaymentEvents(selectedPaymentEvents);
		request.setCustomRequestData(customRequestData);
		request.setOrderContext(orderContext);
		request.setOrderPaymentInstruments(orderPaymentInstruments);
		return request;
	}
}
