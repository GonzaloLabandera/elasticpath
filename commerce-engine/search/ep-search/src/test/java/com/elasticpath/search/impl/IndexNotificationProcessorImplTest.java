/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.search.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.base.exception.EpSystemException;
import com.elasticpath.domain.search.IndexNotification;
import com.elasticpath.domain.search.UpdateType;
import com.elasticpath.service.search.IndexNotificationService;
import com.elasticpath.service.search.IndexType;

/**
 * Test case for {@link IndexNotificationProcessorImpl}.
 */
public class IndexNotificationProcessorImplTest {

	private static final String NEVER_BE_EMPTY = "This should never be empty";

	private IndexNotificationProcessorImpl indexNotificationProcessorImpl;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private IndexNotificationService mockIndexNotificationService;

	private final IndexType indexType = IndexType.PRODUCT;

	private IndexNotification mockUpdateNotification;

	private IndexNotification mockRebuildNotification;

	private IndexNotification mockDeleteAllNotification;

	private IndexNotification mockDeleteNotification;

	@Before
	public void setUp() throws Exception {
		mockUpdateNotification = context.mock(IndexNotification.class, "update notification");
		mockRebuildNotification = context.mock(IndexNotification.class, "rebuild notification");
		mockDeleteAllNotification = context.mock(IndexNotification.class, "delete all notification");
		mockDeleteNotification = context.mock(IndexNotification.class, "delete notification");
		context.checking(new Expectations() {
			{
				allowing(mockUpdateNotification).getUpdateType();
				will(returnValue(UpdateType.UPDATE));
				allowing(mockRebuildNotification).getUpdateType();
				will(returnValue(UpdateType.REBUILD));
				allowing(mockDeleteAllNotification).getUpdateType();
				will(returnValue(UpdateType.DELETE_ALL));
				allowing(mockDeleteNotification).getUpdateType();
				will(returnValue(UpdateType.DELETE));
			}
		});

		indexNotificationProcessorImpl = new IndexNotificationProcessorImpl();
		mockIndexNotificationService = context.mock(IndexNotificationService.class);
		indexNotificationProcessorImpl.setIndexNotificationService(mockIndexNotificationService);
	}

	@Test
	public void testNoOverridesHere() {
		context.checking(new Expectations() {
			{
				oneOf(mockIndexNotificationService).findByIndexType(indexType);
				will(returnValue(Arrays.asList(mockDeleteNotification, mockUpdateNotification)));
			}
		});
		assertEquals(indexNotificationProcessorImpl.getNotifications(indexType),
				Arrays.asList(mockDeleteNotification, mockUpdateNotification));
	}

	/**
	 * Test method for {@link IndexNotificationProcessorImpl#getNotifications(IndexType)}
	 * with a {@code null}.
	 */
	@Test(expected = EpSystemException.class)
	public void testFindAllNewNotificationsWithNull() {
		indexNotificationProcessorImpl.getNotifications(null);
	}

	/**
	 * Test method for {@link IndexNotificationProcessorImpl#getNotifications(IndexType)}.
	 */
	@Test
	public void testFindAllNotifications() {
		context.checking(new Expectations() {
			{
				oneOf(mockIndexNotificationService).findByIndexType(indexType);
				will(returnValue(Arrays.asList(mockUpdateNotification, mockDeleteNotification)));
			}
		});

		assertEquals(indexNotificationProcessorImpl.getNotifications(indexType), Arrays.asList(mockUpdateNotification, mockDeleteNotification));
	}

	/**
	 * Test method for {@link IndexNotificationProcessorImpl#getNotifications()} when
	 * {@link IndexNotificationProcessorImpl#getNotifications(IndexType)} is called.
	 */
	@Test
	public void testGetNotificationsWithFindAllNewNotifications() {
		// first this should be empty
		assertNotNull(NEVER_BE_EMPTY, indexNotificationProcessorImpl.getNotifications());
		assertTrue(indexNotificationProcessorImpl.getNotifications().isEmpty());

		context.checking(new Expectations() {
			{
				oneOf(mockIndexNotificationService).findByIndexType(indexType);
				will(returnValue(Arrays.asList(mockRebuildNotification)));
			}
		});

		// now after we run through, this should different
		final List<IndexNotification> notificationList = Arrays.asList(mockRebuildNotification);
		assertEquals(indexNotificationProcessorImpl.getNotifications(indexType), notificationList);
		assertEquals(indexNotificationProcessorImpl.getNotifications(), notificationList);
	}
}
