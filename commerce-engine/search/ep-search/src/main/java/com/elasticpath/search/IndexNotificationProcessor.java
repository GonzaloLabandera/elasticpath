/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search;

import java.util.List;

import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.service.search.IndexType;

/**
 * Helper service which handles retrieval and removal of {@link IndexNotification}s.
 */
public interface IndexNotificationProcessor {

	/**
	 * Finds all new notifications to process. Notifications are collapsed so that only the
	 * necessary notifications are returned. Use {@link #getRawNotifications()} to get the list of
	 * pre-processed {@link IndexNotification}s.
	 *
	 * @param indexType the type of notifications to retrieve
	 * @return new notifications to process
	 * @see #getRawNotifications()
	 */
	List<IndexNotification> getNotifications(IndexType indexType);

	/**
	 * Retrieves the list of {@link IndexNotification}s. Notifications are collapsed so that
	 * only the necessary notifications are returned. This list is a cached version of what is
	 * stored.
	 *
	 * @return the list of {@link IndexNotification}s
	 * @see #getNotifications(IndexType)
	 */
	List<IndexNotification> getNotifications();

	/**
	 * Removes all of the stored notifications.
	 */
	void removeStoredNotifications();

	/**
	 * Find the last indexnotification with rebuild/delete all.
	 * @param indexType the index type.
	 * @return list of notification.
	 */
	List<IndexNotification> findLastDeleteAllOrRebuildIndexType(IndexType indexType);

	/**
	 * Remove notifications based on the max uid and index type.
	 *
	 * @param maxUid the max uid
	 * @param indexType the index type
	 */
	void removeNotificationByMaxUidAndIndexType(Long maxUid, IndexType indexType);
}