/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.email.handler.order.producer.impl;

import java.util.Map;

import com.elasticpath.email.EmailDto;
import com.elasticpath.email.domain.EmailProperties;
import com.elasticpath.email.handler.order.helper.OrderEmailPropertyHelper;
import com.elasticpath.email.producer.spi.AbstractEmailProducer;
import com.elasticpath.email.producer.spi.composer.EmailComposer;

/**
 * Creates an Order Hold Notification {@link EmailDto} for a given store.
 */
public class OrderOnHoldEmailProducer extends AbstractEmailProducer {

	private static final String ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE_TEMPLATE = "The emailData must contain a non-null '%s' value.";

	private EmailComposer emailComposer;

	private OrderEmailPropertyHelper orderEmailPropertyHelper;

	@Override
	public EmailDto createEmail(final String guid, final Map<String, Object> emailData) {
		String context = getObjectFromEmailData("context", emailData);
		String heldOrderCount = getObjectFromEmailData("held_order_count", emailData);
		final EmailProperties emailProperties = getOrderEmailPropertyHelper().getHoldNotificationEmailProperties(context, heldOrderCount);

		return getEmailComposer().composeMessage(emailProperties);
	}

	/**
	 * Retrieves a String value from the given {@code Map} of email contextual data.
	 *
	 * @param key the object key
	 * @param emailData email contextual data
	 * @return the String
	 * @throws IllegalArgumentException if the Object can not be retrieved from the given parameters
	 */
	protected String getObjectFromEmailData(final String key, final Map<String, Object> emailData) {
		if (emailData == null || !emailData.containsKey(key) || emailData.get(key) == null) {
			throw new IllegalArgumentException(String.format(ILLEGAL_ARGUMENT_EXCEPTION_MESSAGE_TEMPLATE, key));
		}

		return String.valueOf(emailData.get(key));
	}

	public void setEmailComposer(final EmailComposer emailComposer) {
		this.emailComposer = emailComposer;
	}

	protected EmailComposer getEmailComposer() {
		return emailComposer;
	}

	public void setOrderEmailPropertyHelper(final OrderEmailPropertyHelper orderEmailPropertyHelper) {
		this.orderEmailPropertyHelper = orderEmailPropertyHelper;
	}

	protected OrderEmailPropertyHelper getOrderEmailPropertyHelper() {
		return orderEmailPropertyHelper;
	}

}
