/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.domain.transaction.reservation;

import static com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames.RESERVE_REQUEST;

import java.util.List;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.OrderContext;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * Reservation request builder.
 */
public final class ReserveRequestBuilder {
	private List<OrderPaymentInstrumentDTO> selectedOrderPaymentInstruments;
	private MoneyDTO amount;
	private Map<String, String> customRequestData;
	private OrderContext orderContext;
	private int rereserveCount;

	private ReserveRequestBuilder() {
	}

	/**
	 * A reservation request builder.
	 *
	 * @return the builder
	 */
	public static ReserveRequestBuilder builder() {
		return new ReserveRequestBuilder();
	}

	/**
	 * With selected order payment instruments builder.
	 *
	 * @param selectedOrderPaymentInstruments the selected order payment instruments list
	 * @return the builder
	 */
	public ReserveRequestBuilder withSelectedOrderPaymentInstruments(final List<OrderPaymentInstrumentDTO> selectedOrderPaymentInstruments) {
		this.selectedOrderPaymentInstruments = selectedOrderPaymentInstruments;
		return this;
	}

	/**
	 * With amount builder.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public ReserveRequestBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * With custom request data builder.
	 *
	 * @param customRequestData custom request data
	 * @return the builder
	 */
	public ReserveRequestBuilder withCustomRequestData(final Map<String, String> customRequestData) {
		this.customRequestData = customRequestData;
		return this;
	}

	/**
	 * With order context builder.
	 *
	 * @param orderContext the order context
	 * @return the builder
	 */
	public ReserveRequestBuilder withOrderContext(final OrderContext orderContext) {
		this.orderContext = orderContext;
		return this;
	}

	/**
	 * With rereserve count builder.
	 *
	 * @param rereserveCount the rereserve coun
	 * @return the builder
	 */
	public ReserveRequestBuilder withRereserveCount(final int rereserveCount) {
		this.rereserveCount = rereserveCount;
		return this;
	}

	/**
	 * Build reservation request.
	 *
	 * @param beanFactory EP bean factory
	 * @return reservation request
	 */
	public ReserveRequest build(final BeanFactory beanFactory) {
		if (selectedOrderPaymentInstruments == null) {
			throw new IllegalStateException("Builder is not fully initialized, selectedOrderPaymentInstruments list is missing");
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
		final ReserveRequest request = beanFactory.getPrototypeBean(RESERVE_REQUEST, ReserveRequest.class);
		request.setSelectedOrderPaymentInstruments(selectedOrderPaymentInstruments);
		request.setAmount(amount);
		request.setCustomRequestData(customRequestData);
		request.setOrderContext(orderContext);
		request.setRereserveCount(rereserveCount);
		return request;
	}
}