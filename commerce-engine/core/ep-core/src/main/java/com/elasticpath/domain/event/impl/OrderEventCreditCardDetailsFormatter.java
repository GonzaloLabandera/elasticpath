/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.event.impl;

import com.elasticpath.domain.event.OrderEventPaymentDetailFormatter;
import com.elasticpath.domain.order.OrderPayment;

/**
 * Formats Credit Card Details for {@link com.elasticpath.domain.order.OrderEvent}. 
 */
public class OrderEventCreditCardDetailsFormatter implements OrderEventPaymentDetailFormatter {

	private static final char SPACE_CHAR = ' ';

	@Override
	public String formatPaymentDetails(final OrderPayment orderPayment) {
		StringBuilder creditCardDetails = new StringBuilder();
		creditCardDetails.append(SPACE_CHAR);
		creditCardDetails.append(orderPayment.getCardType());
		creditCardDetails.append(SPACE_CHAR);
		creditCardDetails.append(orderPayment.getDisplayValue());
		return creditCardDetails.toString();
	}

}
