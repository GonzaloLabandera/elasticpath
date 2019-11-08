/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.catalog.bulk;

import com.elasticpath.messaging.EventMessage;

/**
 * Handles Bulk Event Messages.
 */
public interface BulkEventHandler {

	/**
	 * Handles Bulk Event Messages.
	 *
	 * @param eventMessage eventMessage.
	 */
	void handleBulkEvent(EventMessage eventMessage);

}
