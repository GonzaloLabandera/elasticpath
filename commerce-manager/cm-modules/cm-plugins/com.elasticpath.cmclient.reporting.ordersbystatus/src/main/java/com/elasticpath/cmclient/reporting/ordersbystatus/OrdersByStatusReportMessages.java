/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.cmclient.reporting.ordersbystatus;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.cmclient.core.nls.LocalizedMessagePostProcessor;
import com.elasticpath.domain.order.OrderStatus;

/**
 * Messages class for the report plugin.
 */
@SuppressWarnings({ "PMD.TooManyFields", "PMD.VariableNamingConventions" })
public final class OrdersByStatusReportMessages {

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
	
	// Define the map of enum constants to localized names
	private final Map<OrderStatus, String> localizedExtensibleEnums = new HashMap<>();

	private void instantiateEnums() {
		if (localizedExtensibleEnums.isEmpty()) {
			localizedExtensibleEnums.put(OrderStatus.CANCELLED, cancelled);
			localizedExtensibleEnums.put(OrderStatus.COMPLETED, complete);
			localizedExtensibleEnums.put(OrderStatus.CREATED, created);
			localizedExtensibleEnums.put(OrderStatus.ONHOLD, onHold);
			localizedExtensibleEnums.put(OrderStatus.IN_PROGRESS, inProgress);
			localizedExtensibleEnums.put(OrderStatus.AWAITING_EXCHANGE, waitExchangeComplete);
			localizedExtensibleEnums.put(OrderStatus.PARTIALLY_SHIPPED, partialShip);
		}
	}

	/**
	 * Returns the localized name of the given enum constant.
	 * 
	 * @param enumValue the enum to be localized
	 * @return the localized string for the enum
	 */
	public String getLocalizedName(final OrderStatus enumValue) {
		return localizedExtensibleEnums.get(enumValue);
	}

	/**
	 * Gets the NLS localize message class.
	 * @return the localized message class.
	 */
	public static OrdersByStatusReportMessages get() {
		OrdersByStatusReportMessages ordersByStatusReportMessages =
				LocalizedMessagePostProcessor.getUTF8Encoded(BUNDLE_NAME, OrdersByStatusReportMessages.class);
		ordersByStatusReportMessages.instantiateEnums();
		return ordersByStatusReportMessages;
	}


}
