/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.transaction.credit;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.MANUAL_CREDIT_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Manual credit request builder.
 */
public final class ManualCreditRequestBuilder {

	private Map<String, String> customRequestData;
	private List<PaymentEvent> ledger;
	private MoneyDTO amount;
	private OrderContext orderContext;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;

	private ManualCreditRequestBuilder() {
	}

	/**
	 * A manual credit request builder.
	 *
	 * @return the builder
	 */
	public static ManualCreditRequestBuilder builder() {
		return new ManualCreditRequestBuilder();
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger the ledger
	 * @return the builder
	 */
	public ManualCreditRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * With custom data required by the plugin builder.
	 *
	 * @param customRequestData the custom request data
	 * @return the builder
	 */
	public ManualCreditRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With amount builder.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public ManualCreditRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext order context
	 * @return the builder
	 */
	public ManualCreditRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * With orderPaymentInstruments builder.
	 *
	 * @param orderPaymentInstruments the order payment instruments list
	 * @return the builder
	 */
	public ManualCreditRequestBuilder withOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
		return this;
	}

	/**
	 * Build manual credit request.
	 *
	 * @param beanFactory EP bean factory
	 * @return manual credit request
	 */
	public ManualCreditRequest build(final BeanFactory beanFactory) {
		if (orderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstruments list is missing");
		}
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
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
		final ManualCreditRequest request = beanFactory.getPrototypeBean(MANUAL_CREDIT_REQUEST, ManualCreditRequest.class);
		request.setCustomRequestData(customRequestData);
		request.setLedger(ledger);
		request.setAmount(amount);
		request.setOrderContext(orderContext);
		request.setOrderPaymentInstruments(orderPaymentInstruments);
		return request;
	}
}
