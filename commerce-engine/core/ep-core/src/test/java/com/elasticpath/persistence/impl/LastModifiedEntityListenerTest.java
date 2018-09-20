/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.persistence.impl;

import java.util.Date;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.LifecycleEvent;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.DatabaseLastModifiedDate;
import com.elasticpath.service.misc.TimeService;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

public class LastModifiedEntityListenerTest {
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private final BeanFactory beanFactory = context.mock(BeanFactory.class);
	private final TimeService timeService = context.mock(TimeService.class);
	private final SampleEntity entity = context.mock(SampleEntity.class);
	private Date currentTime;
	private LastModifiedEntityListener lastModifiedEntityListener;

	@Before
	public void setUp() throws Exception {
		BeanFactoryExpectationsFactory bfef = new BeanFactoryExpectationsFactory(context, beanFactory);
		bfef.allowingBeanFactoryGetBean(ContextIdNames.TIME_SERVICE, timeService);

		lastModifiedEntityListener = new LastModifiedEntityListener();
		lastModifiedEntityListener.setBeanFactory(beanFactory);

		currentTime = new Date();
		context.checking(new Expectations() {
			{
				allowing(timeService).getCurrentTime();
				will(returnValue(currentTime));
			}
		});
	}

	@Test
	public void testBeforeAttachEventWithDirtyDetachedObjectWillSetLastModifiedDate() throws Exception {
		givenEntityIsDirty();

		// Then
		expectThatLastModifiedDateWillBeSet();

		// When
		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_ATTACH);
		lastModifiedEntityListener.eventOccurred(event);
	}

	@Test
	public void testBeforeAttachEventWithUnmodifiedDetachedObjectWillNotSetLastModifiedDate() throws Exception {
		givenEntityIsNotDirty();

		// When
		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_ATTACH);
		lastModifiedEntityListener.eventOccurred(event);

		// Then nothing should happen
	}


	/**
	 * We need to set the last modified date before store so that currently attached objects that
	 * are then modified and flushed have their last modified date set correctly.
	 */
	@Test
	public void testBeforeStoreEventWithDirtyDetachedObjectWillSetLastModifiedDate() {
		givenEntityIsDirty();

		// Then
		expectThatLastModifiedDateWillBeSet();

		// When
		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_STORE);
		lastModifiedEntityListener.eventOccurred(event);
	}

	@Test
	public void testBeforeStoreEventWithUnmodifiedDetachedObjectWillNotSetLastModifiedDate() throws Exception {
		givenEntityIsNotDirty();

		// When
		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_STORE);
		lastModifiedEntityListener.eventOccurred(event);

		// Then nothing should happen
	}

	@Test
	public void testBeforePersistEventWillSetLastModifiedDate() throws Exception {
		// Then
		expectThatLastModifiedDateWillBeSet();

		// When
		LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.BEFORE_PERSIST);
		lastModifiedEntityListener.eventOccurred(event);
	}

	private void givenEntityIsDirty() {
		context.checking(new Expectations() {
			{
				allowing(entity).pcIsDirty();
				will(returnValue(true));
			}
		});
	}

	private void givenEntityIsNotDirty() {
		context.checking(new Expectations() {
			{
				allowing(entity).pcIsDirty();
				will(returnValue(false));
			}
		});
	}


	private void expectThatLastModifiedDateWillBeSet() {
		context.checking(new Expectations() {
			{
				oneOf(entity).setLastModifiedDate(currentTime);
			}
		});
	}

	/**
	 * Sample Entity interface for mocking.
	 */
	private interface SampleEntity extends PersistenceCapable, DatabaseLastModifiedDate {
	}
}
