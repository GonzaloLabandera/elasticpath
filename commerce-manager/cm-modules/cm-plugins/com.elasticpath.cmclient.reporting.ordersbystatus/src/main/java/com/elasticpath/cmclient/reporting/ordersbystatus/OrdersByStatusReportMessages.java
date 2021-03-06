/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.reporting.ordersbystatus;

import com.elasticpath.cmclient.core.nls.BaseMessages;
import com.elasticpath.cmclient.core.nls.LocalizedMessagePostProcessor;
import com.elasticpath.domain.order.OrderStatus;

/**
 * Messages class for the report plugin.
 */
@SuppressWarnings({ "PMD.TooManyFields", "PMD.VariableNamingConventions" })
public final class OrdersByStatusReportMessages extends BaseMessages {

	private static final String BUNDLE_NAME = "com.elasticpath.cmclient.reporting.ordersbystatus.OrdersByStatusReportMessages"; //$NON-NLS-1$

	private OrdersByStatusReportMessages() {
	}

	public String reportTitle;
	public String report;

	// ----------------------------------------------------
	// Errors
	// ----------------------------------------------------
	public String checkBoxNoneSelectedError;
	
	// ----------------------------------------------------
	// Registration Report params UI
	// ----------------------------------------------------
	public String store;
	public String fromDate;
	public String toDate;
	public String orderStatusGroupHeader;
	public String selectStore;
	public String exchangeOnly;
	public String currency;

	// ----------------------------------------------------
	// Order Source Drop down options UI
	// ----------------------------------------------------
	public String allsources;
	public String selectedStore;
	public String googleCheckout;
	public String webServices;

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
	// Others
	// ----------------------------------------------------
	public String emptyString;
	public String exchangeOrderOnly;
	public String no_as_string;
	public String yes_as_string;

	/**
	 * Initialize the localized values for the enumerations.
	 */
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
	public static OrdersByStatusReportMessages get() {
		OrdersByStatusReportMessages ordersByStatusReportMessages =
				LocalizedMessagePostProcessor.getUTF8Encoded(BUNDLE_NAME, OrdersByStatusReportMessages.class);
		ordersByStatusReportMessages.initialize();
		return ordersByStatusReportMessages;
	}


}
