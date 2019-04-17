/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.publishing;

import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;

/**
 * Publishes Change Sets.
 */
public interface ChangeSetPublisher {

	/**
	 * <p>Publishes a Change Set.</p>
	 * <p>Change Sets must be enabled in the source system, and the corresponding Change Set must have a status that permits publishing.</p>
	 *
	 * @param changeSetGuid the GUID of the change set to publish
	 * @return a summary of the publish attempt
	 */
	ChangeSetSummaryMessage publish(String changeSetGuid);

}
