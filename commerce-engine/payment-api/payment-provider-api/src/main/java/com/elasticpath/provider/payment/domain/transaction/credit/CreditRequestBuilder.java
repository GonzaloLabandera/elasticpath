/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.credit;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CREDIT_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Credit request builder.
 */
public final class CreditRequestBuilder {

	private Map<String, String> customRequestData;
	private List<PaymentEvent> ledger;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;
	private List<OrderPaymentInstrumentDTO> selectedOrderPaymentInstruments;
	private MoneyDTO amount;
	private OrderContext orderContext;

	private CreditRequestBuilder() {
	}

	/**
	 * An credit request builder.
	 *
	 * @return the builder
	 */
	public static CreditRequestBuilder builder() {
		return new CreditRequestBuilder();
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger the ledger
	 * @return the builder
	 */
	public CreditRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * With custom data required by the plugin builder.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public CreditRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With orderPaymentInstruments builder.
	 *
	 * @param orderPaymentInstruments the order payment instruments list
	 * @return the builder
	 */
	public CreditRequestBuilder withOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
		return this;
	}

	/**
	 * With selectedInstruments builder.
	 *
	 * @param selectedOrderPaymentInstruments the selected order Payment Instruments
	 * @return the builder
	 */
	public CreditRequestBuilder withSelectedOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> selectedOrderPaymentInstruments) {
		this.selectedOrderPaymentInstruments = selectedOrderPaymentInstruments;
		return this;
	}

	/**
	 * With amount builder.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public CreditRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext order context
	 * @return the builder
	 */
	public CreditRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * Build credit request.
	 *
	 * @param beanFactory EP bean factory
	 * @return credit request
	 */
	public CreditRequest build(final BeanFactory beanFactory) {
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
		}
		if (orderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstruments list is missing");
		}
		if (selectedOrderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, selectedOrderPaymentInstruments list is missing");
		}
		if (amount == null) {
			throw new IllegalStateException("Builder is not fully initialized, amount is missing");
		}
		if (orderContext == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderContext is missing");
		}
		if (customRequestData == null) {
			throw new IllegalStateException("Builder is not fully initialized, customRequestData map is missing");
		}
		final CreditRequest request = beanFactory.getPrototypeBean(CREDIT_REQUEST, CreditRequest.class);
		request.setCustomRequestData(customRequestData);
		request.setLedger(ledger);
		request.setSelectedOrderPaymentInstruments(selectedOrderPaymentInstruments);
		request.setOrderPaymentInstruments(orderPaymentInstruments);
		request.setAmount(amount);
		request.setOrderContext(orderContext);
		return request;
	}
}
