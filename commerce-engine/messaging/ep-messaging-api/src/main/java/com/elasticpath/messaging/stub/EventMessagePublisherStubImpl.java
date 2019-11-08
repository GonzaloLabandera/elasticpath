/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.messaging.stub;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;

/**
 * Represents a stub for EventMessagePublisher.
 */
public class EventMessagePublisherStubImpl implements EventMessagePublisher {

	/**
	 * Do nothing.
	 *
	 * @param eventMessage the message to publish
	 */
	@Override
	public void publish(final EventMessage eventMessage) {
		// it is just a stub

	}
}
