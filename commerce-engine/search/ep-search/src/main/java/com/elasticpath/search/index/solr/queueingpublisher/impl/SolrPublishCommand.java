/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.search.index.solr.queueingpublisher.impl;

import org.apache.solr.client.solrj.SolrClient;

/**
 * A {@link SolrPublishCommand} implements the Command Pattern for the {@link QueueingSolrDocumentPublisher}.
 */
public interface SolrPublishCommand {

	/**
	 * Apply can throw any exception it wants, with the exception of {@code InterruptedException} they will simply be logged. Throwing
	 * {@code InterruptedException} tells the working thread to shutdown.
	 * 
	 * @param client the Solr client.
	 * @throws Exception will be logged but the further commands will continue to be processed, unless it's a {@code InterruptedException}.
	 */
	void apply(SolrClient client) throws Exception;

}
