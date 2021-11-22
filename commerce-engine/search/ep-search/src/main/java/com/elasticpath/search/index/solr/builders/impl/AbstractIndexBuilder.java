/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.builders.impl;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.solr.client.solrj.SolrClient;

import com.elasticpath.search.IndexNotificationProcessor;
import com.elasticpath.search.index.solr.IndexBuildEventListener;
import com.elasticpath.search.index.solr.builders.IndexBuilder;

/**
 * A skeleton implementation of <code>IndexBuild</code>.
 */
public abstract class AbstractIndexBuilder implements IndexBuilder {

	private IndexNotificationProcessor indexNotificationProcessor;

	/**
	 * Override in subclass to perform actions if necessary.
	 * This only occurs when method 'canPaginate' returns <code>False<code/>, which is the default behaviour of it.
	 *
	 * @return a paginated list of indexable uids whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findAllUids() {
		throw new NotImplementedException("This method has not been implemented yet");
	}

	/**
	 * Override in subclass to perform actions if necessary use of findIndexableUids 'Paginated'.
	 * This only occurs when method 'canPaginate' returns <code>True<code/>, so override its default behaviour as well.
	 *
	 * @param page the notifications
	 * @return a paginated list of indexable uids whose last modified date is later than the specified date
	 */
	@Override
	public List<Long> findIndexableUidsPaginated(final int page) {
		throw new NotImplementedException("This method has not been implemented yet");
	}

	/**
	 * Action that is performed after the index has been updated. This only occurs when there are items to process. Override in subclass to perform
	 * actions.
	 *
	 * @param server the server that was used to update the index
	 */
	@Override
	public void onIndexUpdated(final SolrClient client) {
		// subclass to perform actions
	}

	/**
	 * Action that is performed before updating the index. This only occurs when there are items to process. Override in subclass to perform actions.
	 *
	 * @param server the server that is going to be used to update the index
	 */
	@Override
	public void onIndexUpdating(final SolrClient client) {
		// subclass to perform actions
	}

	@Override
	public IndexBuildEventListener getIndexBuildEventListener() {
		return null; // default implementation
	}

	@Override
	public IndexNotificationProcessor getIndexNotificationProcessor() {
		return indexNotificationProcessor;
	}

	/**
	 * Sets the {@link IndexNotificationProcessor} instance to use.
	 *
	 * @param indexNotificationProcessor the {@link IndexNotificationProcessor} instance to use
	 */
	public void setIndexNotificationProcessor(final IndexNotificationProcessor indexNotificationProcessor) {
		this.indexNotificationProcessor = indexNotificationProcessor;
	}

}
