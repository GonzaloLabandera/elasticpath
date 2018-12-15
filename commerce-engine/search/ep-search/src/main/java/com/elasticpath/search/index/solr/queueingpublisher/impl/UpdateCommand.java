/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.search.index.solr.queueingpublisher.impl;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

/**
 * Update document command for the {@code QueueingSolrDocumentPublisher}.
 */
public class UpdateCommand implements SolrPublishCommand {

	private final SolrInputDocument document;

	/**
	 * Constructor, sets the document to update.
	 * 
	 * @param document the document to update.
	 */
	public UpdateCommand(final SolrInputDocument document) {
		this.document = document;
	}

	/**
	 * Applies the update to the specified SolrClient client.
	 * 
	 * @param client the specified {@link SolrClient}.
	 * @throws SolrServerException on failure.
	 * @throws IOException on failure.
	 */
	@Override
	public void apply(final SolrClient client) throws SolrServerException, IOException {
		client.add(document);
	}
}