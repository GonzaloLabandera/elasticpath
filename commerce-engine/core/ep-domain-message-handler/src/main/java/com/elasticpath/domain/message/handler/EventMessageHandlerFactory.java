/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler;

import java.util.Optional;

/**
 * The factory to get event message handler by event type.
 */
public interface EventMessageHandlerFactory {

	/**
	 * Gets event message handler by event type.
	 *
	 * @param eventType event type to get handler for.
	 * @return an {@link Optional} of handler.
	 */
	Optional<EventMessageHandler> getHandler(String eventType);

}