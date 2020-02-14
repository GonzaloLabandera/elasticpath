/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.history;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_HISTORY_REQUEST;

import java.util.List;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.provider.payment.service.event.PaymentEvent;

/**
 * Payment event history request builder.
 */
public final class PaymentEventHistoryRequestBuilder {
	private List<PaymentEvent> ledger;

	private PaymentEventHistoryRequestBuilder() {
	}

	/**
	 * An modify reservation request builder.
	 *
	 * @return the builder
	 */
	public static PaymentEventHistoryRequestBuilder builder() {
		return new PaymentEventHistoryRequestBuilder();
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger the ledger
	 * @return the builder
	 */
	public PaymentEventHistoryRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * Build modify reservation request.
	 *
	 * @param beanFactory EP bean factory
	 * @return modify reservation request
	 */
	public PaymentEventHistoryRequest build(final BeanFactory beanFactory) {
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
		}
		final PaymentEventHistoryRequest request = beanFactory.getPrototypeBean(
				PAYMENT_EVENT_HISTORY_REQUEST, PaymentEventHistoryRequest.class);
		request.setLedger(ledger);
		return request;
	}
}
