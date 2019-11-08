/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler;

import com.elasticpath.messaging.EventMessage;

/**
 * Represents of helper for getting of entity from {@link org.apache.camel.Exchange}.
 *
 * @param <T> type of entity.
 */
public interface EventMessageHandlerHelper<T> {

	/**
	 * Get entity from {@link org.apache.camel.Exchange}.
	 *
	 * @param eventMessage processing event message.
	 * @return entity.
	 */
	T getExchangedEntity(EventMessage eventMessage);

}
