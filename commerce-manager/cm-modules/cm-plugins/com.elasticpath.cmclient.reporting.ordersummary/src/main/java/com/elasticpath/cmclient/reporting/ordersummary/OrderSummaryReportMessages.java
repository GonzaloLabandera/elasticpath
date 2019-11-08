/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */

package com.elasticpath.cmclient.reporting.ordersummary;

import com.elasticpath.cmclient.core.nls.BaseMessages;
import com.elasticpath.cmclient.core.nls.LocalizedMessagePostProcessor;

import com.elasticpath.domain.order.OrderStatus;

/**
 * Messages class for the report plugin.
 */
@SuppressWarnings("PMD.TooManyFields")
public final class OrderSummaryReportMessages extends BaseMessages {

	private static final String BUNDLE_NAME = "com.elasticpath.cmclient.reporting.ordersummary.OrderSummaryReportPluginResources"; //$NON-NLS-1$

	private OrderSummaryReportMessages() {
	}

	public String reportTitle;
	public String report;
	public String emptyString;

	// ----------------------------------------------------
	// Order Summary Report params UI
	// ----------------------------------------------------
	public String store;
	public String fromDate;
	public String toDate;
	public String selectCurrency;
	public String currency;
	public String exchangeOnly;
	public String orderStatusGroupHeader;
	public String selectStore;

	// ----------------------------------------------------
	// Order Status Text
	// ----------------------------------------------------
	public String waitExchangeComplete;
	public String cancelled;
	public String complete;
	public String created;
	public String inProgress;
	public String onHold;
	public String partialShip;
	
	// ----------------------------------------------------
	// Order Source Drop down options UI
	// ----------------------------------------------------
	public String allsources;
	public String selectedStore;
	public String googleCheckout;
	public String webServices;
	
	// ----------------------------------------------------
	// Others
	// ----------------------------------------------------
	public String allStores;
	public String checkBoxNoneSelectedError;
	public String exchangeOrderOnly;

	@Override
	protected void instantiateEnums() {
		putLocalizedName(OrderStatus.CANCELLED, cancelled);
		putLocalizedName(OrderStatus.COMPLETED, complete);
		putLocalizedName(OrderStatus.CREATED, created);
		putLocalizedName(OrderStatus.ONHOLD, onHold);
		putLocalizedName(OrderStatus.IN_PROGRESS, inProgress);
		putLocalizedName(OrderStatus.AWAITING_EXCHANGE, waitExchangeComplete);
		putLocalizedName(OrderStatus.PARTIALLY_SHIPPED, partialShip);
	}

	/**
	 * Gets the NLS localize message class.
	 * @return the localized message class.
	 */
	public static OrderSummaryReportMessages get() {
		OrderSummaryReportMessages ordersByStatusReportMessages =
				LocalizedMessagePostProcessor.getUTF8Encoded(BUNDLE_NAME, OrderSummaryReportMessages.class);
		ordersByStatusReportMessages.initialize();
		return ordersByStatusReportMessages;
	}

}