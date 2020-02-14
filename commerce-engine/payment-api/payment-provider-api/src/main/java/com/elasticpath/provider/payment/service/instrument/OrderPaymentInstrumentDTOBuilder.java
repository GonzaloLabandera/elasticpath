/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.provider.payment.service.instrument;

import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.AddressDTO;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;

/**
 * An order payment instrument DTO builder.
 */
public final class OrderPaymentInstrumentDTOBuilder {
	private String guid;
	private PaymentInstrumentDTO paymentInstrument;
	private MoneyDTO limit;
	private String orderNumber;
	private Map<String, String> orderPaymentInstrumentData;
	private String customerEmail;
	private AddressDTO billingAddress;

	private OrderPaymentInstrumentDTOBuilder() {
	}

	/**
	 * An order payment instrument DTO builder.
	 *
	 * @return the builder
	 */
	public static OrderPaymentInstrumentDTOBuilder builder() {
		return new OrderPaymentInstrumentDTOBuilder();
	}

	/**
	 * With guid builder.
	 *
	 * @param guid guid
	 * @return builder
	 */
	public OrderPaymentInstrumentDTOBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * With payment instrument builder.
	 *
	 * @param paymentInstrument payment instrument
	 * @return builder
	 */
	public OrderPaymentInstrumentDTOBuilder withPaymentInstrument(final PaymentInstrumentDTO paymentInstrument) {
		this.paymentInstrument = paymentInstrument;
		return this;
	}

	/**
	 * With limit builder.
	 *
	 * @param limit limit
	 * @return builder
	 */
	public OrderPaymentInstrumentDTOBuilder withLimit(final MoneyDTO limit) {
		this.limit = limit;
		return this;
	}

	/**
	 * With order number builder.
	 *
	 * @param orderNumber order number
	 * @return builder
	 */
	public OrderPaymentInstrumentDTOBuilder withOrderNumber(final String orderNumber) {
		this.orderNumber = orderNumber;
		return this;
	}

	/**
	 * With data builder.
	 *
	 * @param orderPaymentInstrumentData additional data
	 * @return builder
	 */
	public OrderPaymentInstrumentDTOBuilder withOrderPaymentInstrumentData(final Map<String, String> orderPaymentInstrumentData) {
		this.orderPaymentInstrumentData = orderPaymentInstrumentData;
		return this;
	}

	/**
	 * With customer email builder.
	 *
	 * @param customerEmail customer email
	 * @return builder
	 */
	public OrderPaymentInstrumentDTOBuilder withCustomerEmail(final String customerEmail) {
		this.customerEmail = customerEmail;
		return this;
	}

	/**
	 * With billing address builder.
	 *
	 * @param billingAddress billing address
	 * @return builder
	 */
	public OrderPaymentInstrumentDTOBuilder withBillingAddress(final AddressDTO billingAddress) {
		this.billingAddress = billingAddress;
		return this;
	}

	/**
	 * Build {@link OrderPaymentInstrumentDTO}.
	 *
	 * @param beanFactory EP bean factory
	 * @return DTO
	 */
	public OrderPaymentInstrumentDTO build(final BeanFactory beanFactory) {
		if (guid == null) {
			throw new IllegalStateException("Builder is not fully initialized, guid is missing");
		}
		if (paymentInstrument == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentInstrument is missing");
		}
		if (limit == null) {
			throw new IllegalStateException("Builder is not fully initialized, limit is missing");
		}
		if (orderNumber == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderNumber is missing");
		}
		if (orderPaymentInstrumentData == null) {
			throw new IllegalStateException("Builder is not fully initialized, orderPaymentInstrumentData map is missing");
		}
		final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.ORDER_PAYMENT_INSTRUMENT_DTO, OrderPaymentInstrumentDTO.class);
		orderPaymentInstrumentDTO.setGUID(guid);
		orderPaymentInstrumentDTO.setPaymentInstrument(paymentInstrument);
		orderPaymentInstrumentDTO.setLimit(limit);
		orderPaymentInstrumentDTO.setOrderNumber(orderNumber);
		orderPaymentInstrumentDTO.setOrderPaymentInstrumentData(orderPaymentInstrumentData);
		orderPaymentInstrumentDTO.setCustomerEmail(customerEmail);
		orderPaymentInstrumentDTO.setBillingAddress(billingAddress);
		return orderPaymentInstrumentDTO;
	}
}