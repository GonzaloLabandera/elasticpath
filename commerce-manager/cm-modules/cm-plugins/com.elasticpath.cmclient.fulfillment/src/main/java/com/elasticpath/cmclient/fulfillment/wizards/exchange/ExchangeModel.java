/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.cmclient.fulfillment.wizards.exchange;

import java.util.Collection;
import java.util.List;

import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderReturn;
import com.elasticpath.provider.payment.service.instrument.PaymentInstrumentDTO;
import com.elasticpath.service.order.ReturnExchangeRefundTypeEnum;
import com.elasticpath.service.orderpaymentapi.management.PaymentStatistic;

/**
 * Utility container for information related to exchange process.
 */
public class ExchangeModel {
	private OrderReturn orderReturn;
	private final List<PaymentInstrumentDTO> orderInstruments;

	private List<PaymentInstrumentDTO> reservationInstruments;
	private Collection<PaymentStatistic> reservationTransactions;
	private Collection<PaymentStatistic> refundTransactions;
	private Order exchangeOrder;
	private ReturnExchangeRefundTypeEnum refundType;
	private ExchangeWizard.ExchangeWizardType exchangeWizardType;

	/**
	 * Constructor.
	 *
	 * @param orderReturn      order return
	 * @param orderInstruments order instruments
	 */
	public ExchangeModel(final OrderReturn orderReturn, final List<PaymentInstrumentDTO> orderInstruments) {
		this.orderReturn = orderReturn;
		this.orderInstruments = orderInstruments;
	}

	public OrderReturn getOrderReturn() {
		return orderReturn;
	}

	public List<PaymentInstrumentDTO> getOrderInstruments() {
		return orderInstruments;
	}

	public Collection<PaymentStatistic> getReservationTransactions() {
		return reservationTransactions;
	}

	public void setReservationTransactions(final Collection<PaymentStatistic> reservationTransactions) {
		this.reservationTransactions = reservationTransactions;
	}

	public Collection<PaymentStatistic> getRefundTransactions() {
		return refundTransactions;
	}

	public void setRefundTransactions(final Collection<PaymentStatistic> refundTransactions) {
		this.refundTransactions = refundTransactions;
	}

	public List<PaymentInstrumentDTO> getReservationInstruments() {
		return reservationInstruments;
	}

	public void setReservationInstruments(final List<PaymentInstrumentDTO> reservationInstruments) {
		this.reservationInstruments = reservationInstruments;
	}

	/**
	 * Updates order return in this container.
	 *
	 * @param orderReturn updated order return
	 */
	public void updateOrderReturn(final OrderReturn orderReturn) {
		this.orderReturn = orderReturn;
	}

	public Order getOrder() {
		return orderReturn.getOrder();
	}

	/**
	 * Gets refund type.
	 *
	 * @return refund type.
	 */
	public ReturnExchangeRefundTypeEnum getRefundType() {
		return refundType;
	}

	/**
	 * Sets refund type.
	 *
	 * @param refundType refund type.
	 */
	public void setRefundType(final ReturnExchangeRefundTypeEnum refundType) {
		this.refundType = refundType;
	}

	/**
	 * Get the Exchange Wizard Type.
	 *
	 * @return the exchange order wizard type.
	 */
	public ExchangeWizard.ExchangeWizardType getExchangeWizardType() {
		return exchangeWizardType;
	}

	/**
	 * Set the Exchange Wizard Type.
	 *
	 * @param exchangeWizardType the exchange order wizard type.
	 */
	public void setExchangeWizardType(final ExchangeWizard.ExchangeWizardType exchangeWizardType) {
		this.exchangeWizardType = exchangeWizardType;
	}

	/**
	 * Get the exchange order.
	 *
	 * @return the exchange order
	 */
	public Order getExchangeOrder() {
		return exchangeOrder;
	}

	/**
	 * Set the exchange order.
	 *
	 * @param exchangeOrder the exchangeOrder
	 */
	public void setExchangeOrder(final Order exchangeOrder) {
		this.exchangeOrder = exchangeOrder;
	}
}
