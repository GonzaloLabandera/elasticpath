/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.cmclient.reporting.returnsandexchanges;

import com.elasticpath.cmclient.core.nls.BaseMessages;
import com.elasticpath.cmclient.core.nls.LocalizedMessagePostProcessor;
import com.elasticpath.domain.order.OrderReturnStatus;

/**
 * Messages class for the report plugin.
 */
@SuppressWarnings({ "PMD.TooManyFields", "PMD.VariableNamingConventions" })
public final class ReturnsAndExchangesReportMessages extends BaseMessages {

	private static final String BUNDLE_NAME = "com.elasticpath.cmclient.reporting.returnsandexchanges." + //$NON-NLS-1$
			"ReturnsAndExchangesReportPluginResources"; //$NON-NLS-1$

	private ReturnsAndExchangesReportMessages() {
	}

	public String reportTitle;
	public String report;

	// ----------------------------------------------------
	// Registration Report params UI
	// ----------------------------------------------------
	public String store;
	public String currency;
	public String fromDate;
	public String toDate;
	public String rmaType;
	public String statusGroupHeader;
	
	public String waitExchangeComplete;
	public String cancelled;
	public String complete;
	public String awaitingStockReturn;
	public String awatingCompletion;

	public String selectStore;
	
	public String returnsAndExchanges;
	public String returnsOnly;
	public String exchangesOnly;
	public String checkBoxNoneSelectedError;
	
	// ----------------------------------------------------
	// Registration Report service
	// ----------------------------------------------------
	public String no_as_string;
	public String yes_as_string;

	// ----------------------------------------------------
	// Order Return Status Text
	// ----------------------------------------------------
	public String OrderReturnStatus_Cancelled;
	public String OrderReturnStatus_Completed;
	public String OrderReturnStatus_AwaitingCompletion;
	public String OrderReturnStatus_AwaitingStockReturn;
	public String OrderReturnType_Exchange;
	public String OrderReturnType_Return;


	/**
	 * Provide the localization values for the enums.
	 */
	protected void instantiateEnums() {
		putLocalizedName(OrderReturnStatus.AWAITING_COMPLETION, OrderReturnStatus_AwaitingCompletion);
		putLocalizedName(OrderReturnStatus.AWAITING_STOCK_RETURN, OrderReturnStatus_AwaitingStockReturn);
		putLocalizedName(OrderReturnStatus.CANCELLED, OrderReturnStatus_Cancelled);
		putLocalizedName(OrderReturnStatus.COMPLETED, OrderReturnStatus_Completed);
	}

	/**
	 * Gets the NLS localize message class.
	 * @return the localized message class.
	 */
	public static ReturnsAndExchangesReportMessages get() {
		ReturnsAndExchangesReportMessages returnsAndExchangesReportMessages =
				LocalizedMessagePostProcessor.getUTF8Encoded(BUNDLE_NAME, ReturnsAndExchangesReportMessages.class);
		returnsAndExchangesReportMessages.initialize();
		return returnsAndExchangesReportMessages;
	}

}
