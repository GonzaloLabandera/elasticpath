/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.search.index.solr.queueingpublisher.impl;

import org.apache.solr.client.solrj.SolrClient;

/**
 * Shut down command for the {@code QueueingSolrDocumentPublisher}.
 */
public class ShutdownCommand implements SolrPublishCommand {

	/**
	 * Applies the command to the specified {@link SolrClient}.
	 * 
	 * @param server the specified {@link SolrClient}.
	 * @throws InterruptedException which the will shutdown the thread.
	 * @see QueueingSolrDocumentPublisher
	 */
	@Override
	public void apply(final SolrClient client) throws InterruptedException {
		throw new InterruptedException();
	}

}
