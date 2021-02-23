/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.domain.messaging;

import com.elasticpath.domain.DatabaseCreationDate;
import com.elasticpath.persistence.api.Persistable;

/**
 * A record for storing messages that are pending to be sent (usually to JMS) via Camel.
 * We use the Outbox Pattern to ensure that the message will only be sent if the transaction is committed.
 */
public interface OutboxMessage extends Persistable, DatabaseCreationDate {
	/**
	 * Retrieve the Camel URI to use when the message is eventually sent.
	 *
	 * @return the Camel URI
	 */
	String getCamelUri();

	/**
	 * Set the Camel URI to use when the message is eventually sent.
	 *
	 * @param camelUri the Camel URI
	 */
	void setCamelUri(String camelUri);

	/**
	 * Retrieve the message body.
	 *
	 * @return the message body
	 */
	String getMessageBody();

	/**
	 * Set the message body.
	 *
	 * @param messageBody the message body
	 */
	void setMessageBody(String messageBody);
}
