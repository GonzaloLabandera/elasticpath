/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.domain.history;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.PAYMENT_EVENT_HISTORY_RESPONSE;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;

/**
 * Payment event history response builder.
 */
public final class PaymentEventHistoryResponseBuilder {

	private MoneyDTO amountCharged;
	private MoneyDTO amountRefunded;


	private PaymentEventHistoryResponseBuilder() {
	}

	/**
	 * An modify reservation response builder.
	 *
	 * @return the builder
	 */
	public static PaymentEventHistoryResponseBuilder builder() {
		return new PaymentEventHistoryResponseBuilder();
	}

	/**
	 * With amount charged builder.
	 *
	 * @param amountCharged the amount charged
	 * @return the builder
	 */
	public PaymentEventHistoryResponseBuilder withAmountCharged(final MoneyDTO amountCharged) {
		this.amountCharged = amountCharged;
		return this;
	}

	/**
	 * With amount refunded builder.
	 *
	 * @param amountRefunded the amount refunded
	 * @return the builder
	 */
	public PaymentEventHistoryResponseBuilder withAmountRefunded(final MoneyDTO amountRefunded) {
		this.amountRefunded = amountRefunded;
		return this;
	}

	/**
	 * Build modify reservation response.
	 *
	 * @param beanFactory EP bean factory
	 * @return modify reservation response
	 */
	public PaymentEventHistoryResponse build(final BeanFactory beanFactory) {
		if (amountCharged == null) {
			throw new IllegalStateException("Builder is not fully initialized, amountCharged is missing");
		}
		if (amountRefunded == null) {
			throw new IllegalStateException("Builder is not fully initialized, amountRefunded is missing");
		}
		final PaymentEventHistoryResponse response = beanFactory.getPrototypeBean(
				PAYMENT_EVENT_HISTORY_RESPONSE, PaymentEventHistoryResponse.class);
		response.setAmountCharged(amountCharged);
		response.setAmountRefunded(amountRefunded);
		return response;
	}
}
