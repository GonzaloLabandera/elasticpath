/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.provider.payment.service.event;

import java.util.Date;
import java.util.Map;

import com.elasticpath.plugin.payment.provider.dto.MoneyDTO;
import com.elasticpath.plugin.payment.provider.dto.PaymentStatus;
import com.elasticpath.plugin.payment.provider.dto.TransactionType;
import com.elasticpath.provider.payment.service.instrument.OrderPaymentInstrumentDTO;

/**
 * The Payment event.
 */
public class PaymentEvent {

	private String guid;
	private String parentGuid;
	private Map<String, String> paymentEventData;
	private boolean originalPaymentInstrument;
	private TransactionType paymentType;
	private PaymentStatus paymentStatus;
	private String referenceId;
	private MoneyDTO amount;
	private OrderPaymentInstrumentDTO orderPaymentInstrumentDTO;
	private String internalMessage;
	private String externalMessage;
	private boolean temporaryFailure;
	private Date date;


	/**
	 * Gets payment event GUID.
	 *
	 * @return guid.
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * Sets payment event GUID.
	 *
	 * @param guid paiment event guid.
	 */
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Gets parent payment event GUID.
	 *
	 * @return parent payment event GUID
	 */
	public String getParentGuid() {
		return parentGuid;
	}

	/**
	 * Sets parent payment event GUID.
	 *
	 * @param parentGuid parent payment event GUID
	 */
	public void setParentGuid(final String parentGuid) {
		this.parentGuid = parentGuid;
	}

	/**
	 * Gets payment event data.
	 *
	 * @return the payment event data
	 */
	public Map<String, String> getPaymentEventData() {
		return paymentEventData;
	}

	/**
	 * Sets payment event data.
	 *
	 * @param paymentEventData the payment event data
	 */
	public void setPaymentEventData(final Map<String, String> paymentEventData) {
		this.paymentEventData = paymentEventData;
	}

	/**
	 * Sets order payment instrument dto.
	 *
	 * @param orderPaymentInstrumentDTO the order payment instrument dto
	 */
	public void setOrderPaymentInstrumentDTO(final OrderPaymentInstrumentDTO orderPaymentInstrumentDTO) {
		this.orderPaymentInstrumentDTO = orderPaymentInstrumentDTO;
	}

	/**
	 * Gets order payment instrument dto.
	 *
	 * @return the order payment instrument dto
	 */
	public OrderPaymentInstrumentDTO getOrderPaymentInstrumentDTO() {
		return orderPaymentInstrumentDTO;
	}

	/**
	 * Original payment instrument boolean.
	 *
	 * @return the boolean
	 */
	public boolean isOriginalPaymentInstrument() {
		return originalPaymentInstrument;
	}

	/**
	 * Sets original payment instrument.
	 *
	 * @param originalPaymentInstrument the original payment instrument
	 */
	public void setOriginalPaymentInstrument(final boolean originalPaymentInstrument) {
		this.originalPaymentInstrument = originalPaymentInstrument;
	}

	/**
	 * Gets payment type.
	 *
	 * @return the payment type
	 */
	public TransactionType getPaymentType() {
		return paymentType;
	}

	/**
	 * Sets payment type.
	 *
	 * @param paymentType the payment type
	 */
	public void setPaymentType(final TransactionType paymentType) {
		this.paymentType = paymentType;
	}

	/**
	 * Gets payment status.
	 *
	 * @return the payment status
	 */
	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	/**
	 * Sets payment status.
	 *
	 * @param paymentStatus the payment status
	 */
	public void setPaymentStatus(final PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	/**
	 * Gets order id.
	 *
	 * @return the order id
	 */
	public String getReferenceId() {
		return referenceId;
	}

	/**
	 * Sets order id.
	 *
	 * @param referenceId the order id
	 */
	public void setReferenceId(final String referenceId) {
		this.referenceId = referenceId;
	}

	/**
	 * Gets amount.
	 *
	 * @return the amount
	 */
	public MoneyDTO getAmount() {
		return amount;
	}

	/**
	 * Sets amount.
	 *
	 * @param amount the amount
	 */
	public void setAmount(final MoneyDTO amount) {
		this.amount = amount;
	}

	/**
	 * Gets internal message.
	 *
	 * @return the internal message
	 */
	public String getInternalMessage() {
		return internalMessage;
	}

	/**
	 * Sets internal message.
	 *
	 * @param internalMessage the internal message
	 */
	public void setInternalMessage(final String internalMessage) {
		this.internalMessage = internalMessage;
	}

	/**
	 * Gets external message.
	 *
	 * @return the external message
	 */
	public String getExternalMessage() {
		return externalMessage;
	}

	/**
	 * Sets external message.
	 *
	 * @param externalMessage the external message
	 */
	public void setExternalMessage(final String externalMessage) {
		this.externalMessage = externalMessage;
	}

	/**
	 * Is temporary failure boolean.
	 *
	 * @return the boolean
	 */
	public boolean isTemporaryFailure() {
		return temporaryFailure;
	}

	/**
	 * Sets temporary failure.
	 *
	 * @param temporaryFailure the temporary failure
	 */
	public void setTemporaryFailure(final boolean temporaryFailure) {
		this.temporaryFailure = temporaryFailure;
	}

	/**
	 * Gets date of payment event.
	 *
	 * @return date of payment event.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets date of payment event.
	 *
	 * @param date date of payment event.
	 */
	public void setDate(final Date date) {
		this.date = date;
	}

}
