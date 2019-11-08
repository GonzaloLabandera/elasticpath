/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.message.handler;

import com.elasticpath.messaging.EventMessage;

/**
 * Handles Event Messages.
 */
public interface EventMessageHandler {

	/**
	 * Handles event message.
	 *
	 * @param eventMessage event message to process.
	 */
	void handleMessage(EventMessage eventMessage);

}
