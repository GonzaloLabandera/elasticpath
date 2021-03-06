/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.service.search.impl;

import static com.elasticpath.persistence.api.PersistenceConstants.LIST_PARAMETER_NAME;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.search.Query;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.EpUnsupportedOperationException;
import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.domain.search.UpdateType;
import com.elasticpath.service.impl.AbstractEpPersistenceServiceImpl;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;
import com.elasticpath.service.search.SearchConfigFactory;
import com.elasticpath.service.search.index.QueryComposer;
import com.elasticpath.service.search.index.QueryComposerFactory;
import com.elasticpath.service.search.query.SearchCriteria;

/**
 * Default implementation of {@link IndexNotificationService}.
 */
public class IndexNotificationServiceImpl extends AbstractEpPersistenceServiceImpl implements IndexNotificationService {
	
	private QueryComposerFactory queryComposerFactory;
	
	private SearchConfigFactory searchConfigFactory;

	private int defaultBatchSize;

	/**
	 * Add the given {@link IndexNotification} to the notification queue.
	 * 
	 * @param notification the {@link IndexNotification} to save or update
	 * @return the saved notification
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public IndexNotification add(final IndexNotification notification) {
		sanityCheck();
		return getPersistenceEngine().saveOrUpdate(notification);
	}
	
	/**
	 * Removes the given {@link IndexNotification}.
	 *
	 * @param notification the {@link IndexNotification} to remove
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public void remove(final IndexNotification notification) {
		sanityCheck();
		getPersistenceEngine().delete(notification);
	}
	
	/**
	 * Gets a list of {@link IndexNotification}s which are for the given {@link IndexType}.
	 * 
	 * @param indexType the type of index to
	 * @return a list of {@link IndexNotification}s which are for the given {@link IndexType}
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public List<IndexNotification> findByIndexType(final IndexType indexType) {
		sanityCheck();
		if (indexType == null) {
			throw new EpServiceException("indexType cannot be null");
		}

		return getPersistenceEngine().retrieveByNamedQuery(
				"INDEXNOTIFY_FIND_BY_INDEX_TYPE", new Object[] { indexType.getIndexName() }, 0, getDefaultBatchSize()
		);
	}

	@Override
	public List<IndexNotification> findLastDeleteAllOrRebuildIndexType(final IndexType indexType) {
		sanityCheck();

		if (indexType == null) {
			throw new IllegalArgumentException("indexType cannot be null");
		}

		return getPersistenceEngine().retrieveByNamedQuery(
				"INDEXNOTIFY_FIND_BY_INDEX_TYPE_AND_DELETE_ALL_OR_REBUILD",
				new Object[] { indexType.getIndexName(), UpdateType.REBUILD, UpdateType.DELETE_ALL},
				0,
				1
		);
	}

	@Override
	public void removeNotificationByMaxUid(final Long maxUid, final IndexType indexType) {
		getPersistenceEngine().executeNamedQuery("DELETE_INDEXNOTIFY_BY_MAX_UID_AND_INDEXTYPE", maxUid, indexType.getIndexName());
	}

	/**
	 * Finds a set of index notifications by index and update types.
	 * 
	 * @param indexType the index type
	 * @param updateType the update type
	 * @return a set of index notifications
	 */
	@Override
	public Collection<IndexNotification> findByIndexAndUpdateType(final IndexType indexType, final UpdateType updateType) {
		sanityCheck();
		if (indexType == null || updateType == null) {
			throw new IllegalArgumentException("indexType and updateType cannot be null");
		}
		return getPersistenceEngine().retrieveByNamedQuery("INDEXNOTIFY_FIND_BY_INDEX_AND_UPDATE_TYPE", indexType.getIndexName(), updateType);
	}

	@Override
	public void removeNotificationsByUid(final List<Long> indexNotificationUidList) {
		if (!indexNotificationUidList.isEmpty()) {
			getPersistenceEngine().executeNamedQueryWithList("DELETE_INDEXNOTIFY_BY_UIDS", LIST_PARAMETER_NAME, indexNotificationUidList);
		}
	}

	/**
	 * Convenience method to send a notification about the given {@link UpdateType updateType} of
	 * the UIDs affected by the given {@link SearchCriteria searchCriteria}. The update type
	 * should generally be either {@link UpdateType#UPDATE UPDATE} or
	 * {@link UpdateType#DELETE DELETE}.
	 * <p>
	 * Note: The search isn't run by this method - the client dealing with the
	 * notification will execute the query at an undefined point in the future 
	 * when processing its notifications.
	 * </p>
	 * 
	 * @param updateType update type to apply
	 * @param searchCriteria the search criteria used to find the affected UIDs
	 * @param isFuzzy whether the composed query should be fuzzy
	 * @throws EpServiceException in case of any errors
	 */
	@Override
	public void addViaQuery(final UpdateType updateType, final SearchCriteria searchCriteria, final boolean isFuzzy) {
		sanityCheck();
		final IndexNotification notification = getPrototypeBean(ContextIdNames.INDEX_NOTIFICATION, IndexNotification.class);
		notification.setIndexType(searchCriteria.getIndexType());
		notification.setUpdateType(updateType);

		final QueryComposer queryComposer = queryComposerFactory.getComposerForCriteria(searchCriteria);
		final Query query;
		if (isFuzzy) {
			query = queryComposer.composeFuzzyQuery(searchCriteria, getSearchConfigFactory().getSearchConfig(
					searchCriteria.getIndexType().getIndexName()));
		} else {
			query = queryComposer.composeQuery(searchCriteria, getSearchConfigFactory().getSearchConfig(
					searchCriteria.getIndexType().getIndexName()));
		}

		notification.setQueryString(query.toString());
		add(notification);
	}
	
	/**
	 * Not used.
	 *
	 * @param uid the uid
	 * @return nothing
	 * @throws EpServiceException never
	 * @throws EpUnsupportedOperationException always
	 */
	@Override
	public Object getObject(final long uid) throws EpServiceException {
		throw new EpUnsupportedOperationException("not supported");
	}
	
	/**
	 * Sets the {@link QueryComposerFactory} instance to use.
	 *
	 * @param queryComposerFactory the {@link QueryComposerFactory} instance to use
	 */
	public void setQueryComposerFactory(final QueryComposerFactory queryComposerFactory) {
		this.queryComposerFactory = queryComposerFactory;
	}

	/**
	 * Get the search config factory used to get a search configuration.
	 * 
	 * @return the <code>SearchConfigFactory</code>
	 */
	protected SearchConfigFactory getSearchConfigFactory() {
		return searchConfigFactory;
	}

	/**
	 * Set the search config factory used to get a search configuration.
	 * 
	 * @param searchConfigFactory the <code>SearchConfigFactory</code> to set
	 */
	public void setSearchConfigFactory(final SearchConfigFactory searchConfigFactory) {
		this.searchConfigFactory = searchConfigFactory;
	}

	@Override
	public void addNotificationForEntityIndexUpdate(final IndexType indexType,
			final Long affectedUid) {
		sanityCheck();
		final IndexNotification notification = createNotification(indexType, affectedUid);
		add(notification);
	}
	/**
	 * Creates an update index notification object.
	 *   
	 * @param indexType index type
	 * @param affectedUid affected uid
	 * @return populated {@link IndexNotification}
	 */
	IndexNotification createNotification(final IndexType indexType, final Long affectedUid) {
		final IndexNotification notification = getPrototypeBean(ContextIdNames.INDEX_NOTIFICATION, IndexNotification.class);
		notification.setIndexType(indexType);
		notification.setAffectedUid(affectedUid);
		notification.setAffectedEntityType("singleUnit");
		notification.setUpdateType(UpdateType.UPDATE);
		return notification;
	}

	public void setDefaultBatchSize(final int defaultBatchSize) {
		this.defaultBatchSize = defaultBatchSize;
	}

	protected int getDefaultBatchSize() {
		return defaultBatchSize;
	}

}
