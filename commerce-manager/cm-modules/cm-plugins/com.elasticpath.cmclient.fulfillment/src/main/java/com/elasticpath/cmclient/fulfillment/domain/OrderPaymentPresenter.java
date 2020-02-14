/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.cmclient.fulfillment.domain;


/**
 * Provides methods for retrieving presentation strings of various 
 * OrderPayment fields.
 */
public interface OrderPaymentPresenter {

	/**
	 * @return the date the OrderPayment was created, as a formatted string
	 */
	String getDisplayCreatedDate();
	
	/**
	 * @return the amount that was paid, including currency symbol.
	 */
	String getDisplayPaymentAmount();
	
	/**
	 * @return the transaction type
	 */
	String getDisplayTransactionType();
	
	/**
	 * @return the string description of the order payment. The payment string
	 * will vary depending on the payment method and the user's permissions.
	 * (e.g. the user will only see the full credit card number if authorized).
	 */
	String getDisplayPaymentDetails();

	/**
	 * Get payment method for display. It retrieves payment instrument details based on the payment instrument associated with this Order Payment.
	 * @return Payment method String for display.
	 */
	String getDisplayPaymentMethod();

	/**
	 * @return the status string for display purposes.
	 */
	String getDisplayStatus();
	
	/**
	 * @return the result of IsOriginalPI in a string format
	 */
	String getDisplayIsOriginalPI();
}
