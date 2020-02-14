/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.domain.transaction.charge;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.CHARGE_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.event.PaymentEvent;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Charge request builder.
 */
public final class ChargeRequestBuilder {
	private MoneyDTO totalChargeableAmount;
	private Map<String, String> customRequestData;
	private List<PaymentEvent> ledger;
	private OrderContext orderContext;
	private boolean finalPayment;
	private boolean hasSingleReservePerPI;
	private List<OrderPaymentInstrumentDTO> orderPaymentInstruments;

	private ChargeRequestBuilder() {
	}

	/**
	 * A charge request builder.
	 *
	 * @return the builder
	 */
	public static ChargeRequestBuilder builder() {
		return new ChargeRequestBuilder();
	}

	/**
	 * With the flag order has a single reserve per payment instrument builder.
	 *
	 * @param hasSingleReservePerPI the flag order has a single reserve per payment instrument
	 * @return the builder
	 */
	public ChargeRequestBuilder withSingleReservePerPI(final boolean hasSingleReservePerPI) {
		this.hasSingleReservePerPI = hasSingleReservePerPI;
		return this;
	}

	/**
	 * With total chargeable amount builder.
	 *
	 * @param totalChargeableAmount total chargeable amount
	 * @return the builder
	 */
	public ChargeRequestBuilder withTotalChargeableAmount(final MoneyDTO totalChargeableAmount) {
		this.totalChargeableAmount = totalChargeableAmount;
		return this;
	}

	/**
	 * With custom request data builder.
	 *
	 * @param customRequestData custom request data
	 * @return the builder
	 */
	public ChargeRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With the flag to do reserve leftovers builder.
	 *
	 * @param finalPayment the flag to do reserve leftovers
	 * @return the builder
	 */
	public ChargeRequestBuilder withFinalPayment(final boolean finalPayment) {
		this.finalPayment = finalPayment;
		return this;
	}

	/**
	 * With ledger builder.
	 *
	 * @param ledger ledger
	 * @return the builder
	 */
	public ChargeRequestBuilder withLedger(final List<PaymentEvent> ledger) {
		this.ledger = ledger;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext order context
	 * @return the builder
	 */
	public ChargeRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * With orderPaymentInstruments builder.
	 *
	 * @param orderPaymentInstruments the order payment instruments list
	 * @return the builder
	 */
	public ChargeRequestBuilder withOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> orderPaymentInstruments) {
		this.orderPaymentInstruments = orderPaymentInstruments;
		return this;
	}

	/**
	 * Build charge request.
	 *
	 * @param beanFactory EP bean factory
	 * @return charge request
	 */
	public ChargeRequest build(final BeanFactory beanFactory) {
		if (orderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstruments list is missing");
		}
		if (ledger == null) {
			throw new IllegalStateException("Builder is not fully initialized, ledger is missing");
		}
		if (totalChargeableAmount == null) {
			throw new IllegalStateException("Builder is not fully initialized, totalChargeableAmount is missing");
		}
		if (customRequestData == null) {
			throw new IllegalStateException("Builder is not fully initialized, customRequestData map is missing");
		}
		if (orderContext == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderContext is missing");
		}
		final ChargeRequest chargeRequest = beanFactory.getPrototypeBean(CHARGE_REQUEST, ChargeRequest.class);
		chargeRequest.setTotalChargeableAmount(totalChargeableAmount);
		chargeRequest.setCustomRequestData(customRequestData);
		chargeRequest.setLedger(ledger);
		chargeRequest.setOrderContext(orderContext);
		chargeRequest.setFinalPayment(finalPayment);
		chargeRequest.setSingleReservePerPI(hasSingleReservePerPI);
		chargeRequest.setOrderPaymentInstruments(orderPaymentInstruments);
		return chargeRequest;
	}
}