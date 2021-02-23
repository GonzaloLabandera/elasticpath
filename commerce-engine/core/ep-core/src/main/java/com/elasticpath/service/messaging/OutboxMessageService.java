/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.messaging;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.messaging.OutboxMessage;
import com.elasticpath.messaging.EventMessage;

/**
 * Provide outbox message related business service.
 */
public interface OutboxMessageService {

	/**
	 * List all camel message outbox records stored in the database.
	 *
	 * @return a list of camel message outbox records
	 * @throws EpServiceException in case of any errors
	 */
	List<OutboxMessage> list() throws EpServiceException;

	/**
	 * Get the outbox message record with the given UID. Return a new instance if the given UID is less than 0.
	 * Return <code>null</code> the given UID is bigger than 0 and no matching record exists.
	 *
	 * @param outboxMessageUid the OutboxMessage UID.
	 * @return a OutboxMessage if the given UID is less than 0 or exists, otherwise <code>null</code>
	 * @throws EpServiceException in case of any errors
	 */
	OutboxMessage get(long outboxMessageUid) throws EpServiceException;

	/**
	 * Insert outbox message record into the database.
	 *
	 * @param camelUri the destination camel URI for the message
	 * @param eventMessage the event message
	 * @return the OutboxMessage record that was created
	 * @throws EpServiceException in case of any errors
	 */
	OutboxMessage add(String camelUri, EventMessage eventMessage) throws EpServiceException;

	/**
	 * Delete outbox message record from the database.
	 *
	 * @param outboxMessage the outbox message object
	 * @throws EpServiceException in case of any errors
	 */
	void remove(OutboxMessage outboxMessage) throws EpServiceException;
}
