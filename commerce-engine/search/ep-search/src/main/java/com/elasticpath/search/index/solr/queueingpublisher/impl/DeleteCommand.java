/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.search.index.solr.queueingpublisher.impl;

import org.apache.solr.client.solrj.SolrClient;

/**
 * Delete command for a Solr server.
 */
public class DeleteCommand implements SolrPublishCommand {

	private final String uid;

	/**
	 * Constructor, sets the domain object identifier to delete.
	 * 
	 * @param uid the domain object identifier to delete.
	 */
	public DeleteCommand(final long uid) {
		this.uid = String.valueOf(uid);
	}

	/**
	 * Applies the deletion to the specified SolrClient client.
	 * 
	 * @param client the specified {@link SolrClient}.
	 * @throws Exception on failure.
	 */
	@Override
	public void apply(final SolrClient client) throws Exception {
		client.deleteById(uid);
	}

}
