/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.event;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.constants.PaymentProviderApiContextIdNames;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * The payment event builder.
 */
public final class PaymentEventBuilder {

	private String parentGuid;
	private Map<String, String> paymentEventData = Collections.emptyMap();
	private boolean originalPaymentInstrument;
	private TransactionType paymentType;
	private PaymentStatus paymentStatus;
	private String referenceId;
	private MoneyDTO amount;
	private OrderPaymentInstrumentDTO orderPaymentInstrumentDTO;
	private String internalMessage;
	private String externalMessage;
	private boolean temporaryFailure;
	private Date date = new Date();
	private String guid = UUID.randomUUID().toString();

	private PaymentEventBuilder() {
	}

	/**
	 * A payment event builder.
	 *
	 * @return the builder
	 */
	public static PaymentEventBuilder aPaymentEvent() {
		return new PaymentEventBuilder();
	}

	/**
	 * With payment event GUID builder.
	 *
	 * @param guid paiment event guid.
	 * @return the builder
	 */
	public PaymentEventBuilder withGuid(final String guid) {
		this.guid = guid;
		return this;
	}

	/**
	 * With parent payment event GUID builder.
	 *
	 * @param parentGuid parent payment event GUID
	 * @return the builder
	 */
	public PaymentEventBuilder withParentGuid(final String parentGuid) {
		this.parentGuid = parentGuid;
		return this;
	}

	/**
	 * With payment event data builder.
	 *
	 * @param paymentEventData the payment event data
	 * @return the builder
	 */
	public PaymentEventBuilder withPaymentEventData(final Map<String, String> paymentEventData) {
		this.paymentEventData = paymentEventData;
		return this;
	}

	/**
	 * With original payment instrument builder.
	 *
	 * @param originalPaymentInstrument the original payment instrument
	 * @return the builder
	 */
	public PaymentEventBuilder withOriginalPaymentInstrument(final boolean originalPaymentInstrument) {
		this.originalPaymentInstrument = originalPaymentInstrument;
		return this;
	}

	/**
	 * With payment type builder.
	 *
	 * @param paymentType the payment type
	 * @return the builder
	 */
	public PaymentEventBuilder withPaymentType(final TransactionType paymentType) {
		this.paymentType = paymentType;
		return this;
	}

	/**
	 * With payment status builder.
	 *
	 * @param paymentStatus the payment status
	 * @return the builder
	 */
	public PaymentEventBuilder withPaymentStatus(final PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
		return this;
	}

	/**
	 * With reference id builder.
	 *
	 * @param referenceId the reference id
	 * @return the builder
	 */
	public PaymentEventBuilder withReferenceId(final String referenceId) {
		this.referenceId = referenceId;
		return this;
	}

	/**
	 * With amount builder.
	 *
	 * @param amount the amount
	 * @return the builder
	 */
	public PaymentEventBuilder withAmount(final MoneyDTO amount) {
		this.amount = amount;
		return this;
	}

	/**
	 * With order payment instrument dto builder.
	 *
	 * @param orderPaymentInstrumentDTO the order payment instrument dto
	 * @return the builder
	 */
	public PaymentEventBuilder withOrderPaymentInstrumentDTO(final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO) {
		this.orderPaymentInstrumentDTO = orderPaymentInstrumentDTO;
		return this;
	}

	/**
	 * With internal message builder.
	 *
	 * @param internalMessage the internal message
	 * @return the builder
	 */
	public PaymentEventBuilder withInternalMessage(final String internalMessage) {
		this.internalMessage = internalMessage;
		return this;
	}

	/**
	 * With external message builder.
	 *
	 * @param externalMessage the external message
	 * @return the builder
	 */
	public PaymentEventBuilder withExternalMessage(final String externalMessage) {
		this.externalMessage = externalMessage;
		return this;
	}

	/**
	 * With temporary failure builder.
	 *
	 * @param temporaryFailure the temporary failure
	 * @return the builder
	 */
	public PaymentEventBuilder withTemporaryFailure(final boolean temporaryFailure) {
		this.temporaryFailure = temporaryFailure;
		return this;
	}

	/**
	 * With date failure builder.
	 *
	 * @param date date of payment event.
	 * @return the builder
	 */
	public PaymentEventBuilder withDate(final Date date) {
		this.date = date;
		return this;
	}

	/**
	 * Build payment event.
	 *
	 * @param beanFactory EP bean factory
	 * @return the payment event
	 */
	public PaymentEvent build(final BeanFactory beanFactory) {
		if (guid == null) {
			throw new IllegalStateException("Builder is not fully initialized, guid is missing");
		}
		if (paymentType == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentType is missing");
		}
		if (paymentStatus == null) {
			throw new IllegalStateException("Builder is not fully initialized, paymentStatus is missing");
		}
		if (referenceId == null) {
			throw new IllegalStateException("Builder is not fully initialized, referenceId is missing");
		}
		if (amount == null) {
			throw new IllegalStateException("Builder is not fully initialized, amount is missing");
		}
		final PaymentEvent paymentEvent = beanFactory.getPrototypeBean(
				PaymentProviderApiContextIdNames.PAYMENT_EVENT, PaymentEvent.class);
		paymentEvent.setGuid(guid);
		paymentEvent.setParentGuid(parentGuid);
		paymentEvent.setDate(date);
		paymentEvent.setPaymentEventData(paymentEventData);
		paymentEvent.setOriginalPaymentInstrument(originalPaymentInstrument);
		paymentEvent.setPaymentType(paymentType);
		paymentEvent.setPaymentStatus(paymentStatus);
		paymentEvent.setReferenceId(referenceId);
		paymentEvent.setAmount(amount);
		paymentEvent.setOrderPaymentInstrumentDTO(orderPaymentInstrumentDTO);
		paymentEvent.setInternalMessage(internalMessage);
		paymentEvent.setExternalMessage(externalMessage);
		paymentEvent.setTemporaryFailure(temporaryFailure);
		return paymentEvent;
	}
}
