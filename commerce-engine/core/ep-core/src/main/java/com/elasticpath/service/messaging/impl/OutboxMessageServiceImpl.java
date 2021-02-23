/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.service.messaging.impl;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.messaging.OutboxMessage;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.camel.jackson.EventMessageObjectMapper;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.messaging.OutboxMessageService;

/**
 * Provide camel message outbox related business service.
 */
public class OutboxMessageServiceImpl extends AbstractEpPersistenceServiceImpl implements OutboxMessageService {
	private EventMessageObjectMapper eventMessageObjectMapper;

	@Override
	public List<OutboxMessage> list() throws EpServiceException {
		return getPersistenceEngine().retrieveByNamedQuery("SELECT_ALL_OUTBOXMESSAGE");
	}

	@Override
	public OutboxMessage get(final long outboxMessageUid) throws EpServiceException {
		OutboxMessage outboxMessage;
		if (outboxMessageUid <= 0) {
			outboxMessage = getPrototypeBean(ContextIdNames.OUTBOX_MESSAGE, OutboxMessage.class);
		} else {
			outboxMessage = getPersistentBeanFinder().get(ContextIdNames.OUTBOX_MESSAGE, outboxMessageUid);
		}
		return outboxMessage;
	}

	@Override
	public Object getObject(final long uid) throws EpServiceException {
		return get(uid);
	}

	@Override
	public void remove(final OutboxMessage outboxMessage) throws EpServiceException {
		getPersistenceEngine().delete(outboxMessage);
	}

	@Override
	public OutboxMessage add(final String camelUri, final EventMessage eventMessage) throws EpServiceException {
		OutboxMessage outboxMessage = getPrototypeBean(ContextIdNames.OUTBOX_MESSAGE, OutboxMessage.class);
		outboxMessage.setCamelUri(camelUri);
		outboxMessage.setMessageBody(getEventMessageBody(eventMessage));
		getPersistenceEngine().save(outboxMessage);
		return outboxMessage;
	}

	private String getEventMessageBody(final EventMessage eventMessage) {
		String messageBody;
		try {
			messageBody = eventMessageObjectMapper.writeValueAsString(eventMessage);
		} catch (final JsonProcessingException ex) {
			throw new EpServiceException("Unable to serialize EventMessage", ex);
		}
		return messageBody;
	}

	protected EventMessageObjectMapper getEventMessageObjectMapper() {
		return eventMessageObjectMapper;
	}

	public void setEventMessageObjectMapper(final EventMessageObjectMapper eventMessageObjectMapper) {
		this.eventMessageObjectMapper = eventMessageObjectMapper;
	}
}
