/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.index.solr.builders;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;

import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.search.IndexNotificationProcessor;
import com.elasticpath.search.index.solr.IndexBuildEventListener;
import com.elasticpath.service.search.IndexType;

/**
 * An interface for Index Builders.
 */
public interface IndexBuilder {

	/**
	 * Return the index type the subclass builds.
	 * 
	 * @return the index type the subclass builds.
	 */
	IndexType getIndexType();
	
	/**
	 * Returns index build service name.
	 * 
	 * @return index build service name
	 */
	String getName();

	/**
	 * Retrieve all UIDs.
	 *
	 * @return all UIDs
	 */
	List<Long> findAllUids();

	/**
	 * Retrieves a paginated list of all searchable uids.
	 *
	 * @param page the current page of the list to retrieve
	 * @return a paginated list of indexable uids whose last modified date is later than the specified date
	 */
	List<Long> findIndexableUidsPaginated(int page);

	/**
	 * To be used with findIndexableUidsPaginated method and inform use of pagination.
	 *
	 * @return false as default, otherwise override it to use pagination.
	 */
	default boolean canPaginate() {
		return false;
	}

	/**
	 * Retrieves a set of all UIDs that represent the given notification.
	 *
	 * @param notifications the notifications
	 * @return a set of all UIDs that represent the given notification
	 */
	Collection<Long> findUidsByNotification(IndexNotification notifications);
		
	/**
	 * Retrieve added or modified UIDs since last build.
	 * 
	 * @param lastBuildDate the last build date
	 * @return added or modified UIDs
	 */
	List<Long> findAddedOrModifiedUids(Date lastBuildDate);
	
	/**
	 * Retrieve deleted UIDs.
	 * 
	 * @param lastBuildDate the last build date
	 * @return deleted UIDs.
	 */
	List<Long> findDeletedUids(Date lastBuildDate);
	
	/**
	 * Action that is performed after the index has been updated. This only occurs when there are
	 * items to process. Override in subclass to perform actions.
	 * 
	 * @param client the client that was used to update the index
	 */
	void onIndexUpdated(SolrClient client);

	/**
	 * Action that is performed before updating the index. This only occurs when there are items
	 * to process. Override in subclass to perform actions.
	 * 
	 * @param client the client that is going to be used to update the index
	 */
	void onIndexUpdating(SolrClient client);
	
	/**
	 * Gets the index builder event listener.
	 * 
	 * @return the index builder event listener
	 */
	IndexBuildEventListener getIndexBuildEventListener();
	
	/**
	 * Gets the index notification processor.
	 * 
	 * @return the index notification processor
	 */
	IndexNotificationProcessor getIndexNotificationProcessor();
	
	
	/**
	 * Request that the {@code IndexBuilder} build the given uids. An index builder is for one specific {@code IndexType} so the type is implicit.
	 * Note that with the introduction of {@code IndexingPipeline} this call will return after the work has been sent to the pipeline.
	 * 
	 * @param uids a {@code Collection} of uids; preferably a {@code Set}, but we're flexible for backwards compatability.
	 */
	void submit(Collection<Long> uids);
}
