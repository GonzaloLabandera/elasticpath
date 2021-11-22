/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.search.IndexNotificationProcessor;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;

/**
 * Default implementation of {@link IndexNotificationProcessor}.
 */
public class IndexNotificationProcessorImpl implements IndexNotificationProcessor {

	private IndexNotificationService indexNotificationService;

	private List<IndexNotification> indexNotificationList = Collections.emptyList();

	@Override
	public List<IndexNotification> getNotifications(final IndexType indexType) {
		if (indexType == null) {
			throw new EpSystemException("indexType must not be null");
		}

		indexNotificationList = indexNotificationService.findByIndexType(indexType);
		return indexNotificationList;
	}

	@Override
	public List<IndexNotification> getNotifications() {
		return indexNotificationList;
	}

	@Override
	public void removeStoredNotifications() {
		List<Long> notificationUids = new ArrayList<>(indexNotificationList.size());
		for (IndexNotification notification : indexNotificationList) {
			notificationUids.add(notification.getUidPk());
		}

		indexNotificationService.removeNotificationsByUid(notificationUids);
	}

	@Override
	public List<IndexNotification> findLastDeleteAllOrRebuildIndexType(final IndexType indexType) {
		return indexNotificationService.findLastDeleteAllOrRebuildIndexType(indexType);
	}

	@Override
	public void removeNotificationByMaxUidAndIndexType(final Long maxUid, final IndexType indexType) {
		indexNotificationService.removeNotificationByMaxUid(maxUid, indexType);
	}

	public void setIndexNotificationService(final IndexNotificationService indexNotificationService) {
		this.indexNotificationService = indexNotificationService;
	}
}
