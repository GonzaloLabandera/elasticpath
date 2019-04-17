/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.email.converters;

import com.elasticpath.email.EmailDto;
import com.elasticpath.messaging.EventMessage;

/**
 * Converter bean responsible for converting into <code>{@link EmailDto}</code> objects.
 */
public interface EmailDtoConverter {

	/**
	 * Converts a <code>EventMessage</code> into a <code>EmailDto</code>
	 * appropriate for notification of the outcome of the referred publish.
	 * @param eventMessage Authoring data.
	 * @return Notification e-mail DTO.
	 */
	EmailDto fromEventMessage(EventMessage eventMessage);

	/**
	 * Set the recipient e-mail address to be included in case of an error during Change Set processing.
	 * @param errorEmailAddress the recipient address e-mail.
	 */
	void setErrorEmailAddress(String errorEmailAddress);

	/**
	 * Set the from e-mail address.
	 * @param fromEmailAddress the from e-mail address
	 */
	void setFromEmailAddress(String fromEmailAddress);
}
