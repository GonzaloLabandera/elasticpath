/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.cmclient.fulfillment.wizards.reauth;

import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.money.Money;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;

/**
 * Utility container for information related to reauthorization process.
 */
public class ReAuthorizationItem {

	/**
	 * The order for which reauthorization should be made.
	 */
	private Order order;

	/**
	 * Amount of money to be reauthorized.
	 */
	private Money originalAuthorizedAmount;

	/**
	 * Amount of money to be reauthorized.
	 */
	private Money newAuthorizedAmount;

	/**
	 * The payment instruments used for the order. May be empty if order is free.
	 */
	private List<PaymentInstrumentDTO> originalInstruments;

	/**
	 * Reauthorization payment instruments.
	 */
	private List<PaymentInstrumentDTO> newInstruments;

	/**
	 * Exception which was thrown on reauthorization. Can be null if reauthorization was successful.
	 */
	private PaymentsException paymentsException;

	/**
	 * @return the order
	 */
	public Order getOrder() {
		return order;
	}

	/**
	 * @param order the order to set
	 */
	public void setOrder(final Order order) {
		this.order = order;
	}

	/**
	 * @return the reauthorizedAmount
	 */
	public Money getNewAuthorizedAmount() {
		return newAuthorizedAmount;
	}

	/**
	 * @param newAuthorizedAmount the amount to set
	 */
	public void setNewAuthorizedAmount(final Money newAuthorizedAmount) {
		this.newAuthorizedAmount = newAuthorizedAmount;
	}

	/**
	 * @return the originalAuthorizedAmount
	 */
	public Money getOriginalAuthorizedAmount() {
		return originalAuthorizedAmount;
	}

	/**
	 * @param originalAuthorizedAmount the originalAuthorizedAmount to set
	 */
	public void setOriginalAuthorizedAmount(final Money originalAuthorizedAmount) {
		this.originalAuthorizedAmount = originalAuthorizedAmount;
	}

	/**
	 * @return the originalInstruments
	 */
	public List<PaymentInstrumentDTO> getOriginalInstruments() {
		return originalInstruments;
	}

	/**
	 * @param originalInstruments the originalInstruments to set
	 */
	public void setOriginalInstruments(final List<PaymentInstrumentDTO> originalInstruments) {
		this.originalInstruments = originalInstruments;
	}

	/**
	 * @return the newInstruments
	 */
	public List<PaymentInstrumentDTO> getNewInstruments() {
		return newInstruments;
	}

	/**
	 * @param newInstruments the newInstruments to set
	 */
	public void setNewInstruments(final List<PaymentInstrumentDTO> newInstruments) {
		this.newInstruments = newInstruments;
	}

	/**
	 * @return the error
	 */
	public PaymentsException getPaymentsException() {
		return paymentsException;
	}

	/**
	 * @param paymentsException the error to set
	 */
	public void setPaymentsException(final PaymentsException paymentsException) {
		this.paymentsException = paymentsException;
	}

}
