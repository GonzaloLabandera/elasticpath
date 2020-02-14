/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.email.impl;

import java.util.List;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.service.email.EmailAddressesExtractionStrategy;

/**
 * Implements {@link EmailAddressesExtractionStrategy}.
 * <p>
 * Extracts the customer email from customer profile and the user email from order meta data.
 * Expects both email addresses will be sent in notification process.
 */
public class CustomerAndUserEmailAddressesExtractionStrategyImpl implements EmailAddressesExtractionStrategy {

	/**
	 * The key of user email in the order data.
	 */
	private static final String USER_EMAIL = "user-email";

	@Override
	public List<String> extractToList(final Order order) {
		final List<String> emailAddressList = Lists.newArrayList(getEmailOfCustomer(order.getCustomer()), getEmailFromOrderData(order));
		CollectionUtils.filter(emailAddressList, PredicateUtils.notNullPredicate());
		return emailAddressList;
	}

	private String getEmailOfCustomer(final Customer customer) {
		return customer.getEmail();
	}

	private String getEmailFromOrderData(final Order order) {
		return order.getFieldValue(USER_EMAIL);
	}
}
