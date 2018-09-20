/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.persistence.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.LifecycleEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.DatabaseCreationDate;
import com.elasticpath.domain.DatabaseLastModifiedDate;
import com.elasticpath.service.misc.TimeService;

@RunWith(MockitoJUnitRunner.class)
public class DatabaseTimestampsEntityListenerTest {

	@Mock
	private BeanFactory beanFactory;
	@Mock
	private TimeService timeService;
	@Mock
	private SampleEntity entity;
	@InjectMocks
	private DatabaseTimestampsEntityListener databaseTimestampsEntityListener;

	private Date currentTime;

	@Before
	public void setUp() throws Exception {
		currentTime = new Date();
		when(beanFactory.getBean(ContextIdNames.TIME_SERVICE)).thenReturn(timeService);
		when(timeService.getCurrentTime()).thenReturn(currentTime);
	}

	@Test
	public void testBeforeAttachEventWithDirtyDetachedObjectWillSetLastModifiedDate() {
		when(entity.pcIsDirty()).thenReturn(true);

		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_ATTACH);
		databaseTimestampsEntityListener.eventOccurred(event);

		verify(entity).setLastModifiedDate(any());
	}

	@Test
	public void testBeforeAttachEventWithUnmodifiedDetachedObjectWillNotSetLastModifiedDate() {
		when(entity.pcIsDirty()).thenReturn(false);

		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_ATTACH);
		databaseTimestampsEntityListener.eventOccurred(event);

		verify(entity, never()).setLastModifiedDate(any());
	}

	/**
	 * We need to set the last modified date before store so that currently attached objects that
	 * are then modified and flushed have their last modified date set correctly.
	 */
	@Test
	public void testBeforeStoreEventWithDirtyDetachedObjectWillSetLastModifiedDate() {
		when(entity.pcIsDirty()).thenReturn(true);

		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_STORE);
		databaseTimestampsEntityListener.eventOccurred(event);

		verify(entity).setLastModifiedDate(any());
	}

	@Test
	public void testBeforeStoreEventWithUnmodifiedDetachedObjectWillNotSetLastModifiedDate() {
		when(entity.pcIsDirty()).thenReturn(false);

		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_STORE);
		databaseTimestampsEntityListener.eventOccurred(event);

		verify(entity, never()).setLastModifiedDate(any());
	}

	@Test
	public void testBeforePersistEventWillSetLastModifiedDate() {
		when(entity.pcIsDirty()).thenReturn(true);

		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_PERSIST);
		databaseTimestampsEntityListener.eventOccurred(event);

		verify(entity).setLastModifiedDate(currentTime);
	}

	@Test
	public void testBeforePersistEventWillSetCreationDate() {
		when(entity.pcIsDirty()).thenReturn(true);

		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_STORE);
		databaseTimestampsEntityListener.eventOccurred(event);

		verify(entity).setCreationDate(currentTime);
	}

	@Test
	public void testBeforePersistEventWillNotSetCreationDateIfItIsAlreadySet() {
		when(entity.pcIsDirty()).thenReturn(true);
		when(entity.getCreationDate()).thenReturn(new Date());

		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_STORE);
		databaseTimestampsEntityListener.eventOccurred(event);

		verify(entity, never()).setCreationDate(any());
	}

	/**
	 * Sample Entity interface for mocking.
	 */
	private interface SampleEntity extends PersistenceCapable, DatabaseLastModifiedDate, DatabaseCreationDate {
	}
}
