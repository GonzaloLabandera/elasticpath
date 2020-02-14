/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.email;

import java.util.List;

import com.elasticpath.commons.util.EmailAddressUtil;
import com.elasticpath.domain.order.Order;

/**
 * Defines a strategy to extract email addresses from context, e.g. Order.
 */
public interface EmailAddressesExtractionStrategy {

	/**
	 * Extracts list of email addresses from order context without null value.
	 *
	 * @param order the order which contains email addresses.
	 * @return the list of email on the order.
	 */
	List<String> extractToList(Order order);

	/**
	 * Extracts inline email addresses from order context.
	 *
	 * @param order the order which contains email addresses.
	 * @return the inline email on the order.
	 */
	default String extractToInline(Order order) {
		return EmailAddressUtil.inline(extractToList(order));
	}

}
