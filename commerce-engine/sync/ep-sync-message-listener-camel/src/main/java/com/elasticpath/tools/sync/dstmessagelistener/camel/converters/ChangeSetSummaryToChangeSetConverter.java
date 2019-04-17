/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.tools.sync.dstmessagelistener.camel.converters;

import org.apache.camel.Converter;

import com.elasticpath.domain.changeset.ChangeSet;
import com.elasticpath.tools.sync.dstmessagelistener.changesets.ChangeSetLoader;
import com.elasticpath.tools.sync.dstmessagelistener.messages.ChangeSetSummaryMessage;

/**
 * Apache Camel Type Converter that converts {@link ChangeSetSummaryMessage} instances to {@link ChangeSet} instances.
 */
@Converter
public class ChangeSetSummaryToChangeSetConverter {

	private ChangeSetLoader changeSetLoader;

	/**
	 * converts {@link ChangeSetSummaryMessage} instances to {@link ChangeSet} instances.
	 *
	 * @param changeSetSummaryMessage the change set summary message to convert
	 * @return the corresponding change set
	 */
	@Converter
	public ChangeSet convert(final ChangeSetSummaryMessage changeSetSummaryMessage) {
		return getChangeSetLoader().load(changeSetSummaryMessage.getChangeSetGuid());
	}

	protected ChangeSetLoader getChangeSetLoader() {
		return changeSetLoader;
	}

	public void setChangeSetLoader(final ChangeSetLoader changeSetLoader) {
		this.changeSetLoader = changeSetLoader;
	}

}
