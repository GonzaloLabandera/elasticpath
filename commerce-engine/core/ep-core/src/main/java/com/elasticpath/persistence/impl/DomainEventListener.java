/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.persistence.impl;

import static com.elasticpath.commons.exception.EventMessagePublishingException.JMS_SERVER_IS_UNAVAILABLE_MESSAGE;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.event.UpdateListener;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.FieldMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAPersistence;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.EventMessagePublishingException;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventMessagePublisher;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.persistence.openjpa.JpaPersistenceEngine;

/**
 * Lifecycle event listener which sends domain events for supported entities.
 */
public class DomainEventListener extends AbstractLifecycleListener implements UpdateListener, LifecycleEventManager.ListenerAdapter {

	private static final Logger LOGGER = Logger.getLogger(DomainEventListener.class);

	private BeanFactory beanFactory;
	private volatile JpaPersistenceEngine persistenceEngine;
	private EventMessagePublisher eventMessagePublisher;
	private EventMessageFactory eventMessageFactory;
	private EventTypeFactory eventTypeFactory;
	private  Set<Integer> eventTypes;



	@Override
	public void afterPersist(final LifecycleEvent event) {
		handleEvent(event, EventTypeFactory.EventAction.CREATED);
	}


	@Override
	public void afterDelete(final LifecycleEvent event) {
		handleEvent(event, EventTypeFactory.EventAction.DELETED);
	}


	@Override
	public void afterUpdatePerformed(final LifecycleEvent event) {
		//no-op
	}

	@Override
	public void beforeUpdate(final LifecycleEvent event) {
		handleEvent(event, EventTypeFactory.EventAction.UPDATED);
	}


	@Override
	public void afterAttach(final LifecycleEvent event) {
		handleEvent(event, EventTypeFactory.EventAction.UPDATED);
	}

	private void handleEvent(final LifecycleEvent event, final EventTypeFactory.EventAction eventAction) {
		if (eventTypeFactory.isSupported(event.getSource().getClass(), eventAction)
				&& isDirty(((PersistenceCapable) event.getSource()))) {
			EventType eventType = eventTypeFactory.getEventType(event.getSource().getClass(), eventAction);
			final String domainGuid = getGuid(event);

			sendCatalogEvent(eventType, domainGuid);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Domain event message sent: "
						+ eventAction + " " + event.getSource().getClass() + "with GUID" + domainGuid);
			}
		}
	}


	private String getGuid(final LifecycleEvent event) {
		if (event.getSource() instanceof Category) {
			return ((Category) event.getSource()).getCompoundGuid();
		} else {
			return ((Entity) event.getSource()).getGuid();
		}
	}

	@SuppressWarnings("unchecked")
	private boolean isDirty(final PersistenceCapable persistenceCapable) {
		if (persistenceCapable.pcIsDirty()) {
			return true;
		}

		EntityManager entityManager = getPersistenceEngine().getEntityManager();
		ClassMetaData metaData = JPAFacadeHelper.getMetaData(entityManager, persistenceCapable.getClass());

		Set<Class<?>> collect = (Set<Class<?>>) OpenJPAPersistence.cast(entityManager).getDirtyObjects().stream()
				.map(Object::getClass)
				.collect(Collectors.toSet());

		return Stream.of(metaData.getDeclaredFields())
				.map(FieldMetaData::getRelationType)
				.anyMatch(collect::contains);
	}

	/**
	 * Lazy loads the persistence engine to avoid spring context startup cycle failures.
	 *
	 * @return the JPA Persistence Engine
	 */
	protected JpaPersistenceEngine getPersistenceEngine() {
		if (persistenceEngine == null) {
			synchronized (this) {
				if (persistenceEngine == null) {
					persistenceEngine = beanFactory.getBean(ContextIdNames.PERSISTENCE_ENGINE);
				}
			}
		}
		return persistenceEngine;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setEventTypeFactory(final EventTypeFactory eventTypeFactory) {
		this.eventTypeFactory = eventTypeFactory;
	}

	public void setEventMessageFactory(final EventMessageFactory eventMessageFactory) {
		this.eventMessageFactory = eventMessageFactory;
	}

	public void setEventMessagePublisher(final EventMessagePublisher eventMessagePublisher) {
		this.eventMessagePublisher = eventMessagePublisher;
	}

	/**
	 * Sends a domain event message.
	 *
	 * @param eventType        the type of Catalog Event to trigger.
	 * @param domainObjectGuid the guid of the domain object associated with the event.
	 */
	private void sendCatalogEvent(final EventType eventType, final String domainObjectGuid) {
		final EventMessage domainObjectCreatedEventMessage = eventMessageFactory.createEventMessage(eventType, domainObjectGuid);

		try {
			eventMessagePublisher.publish(domainObjectCreatedEventMessage);
		} catch (Exception publishingException) {
			final EventMessagePublishingException exception = new EventMessagePublishingException(JMS_SERVER_IS_UNAVAILABLE_MESSAGE);
			exception.initCause(publishingException);

			throw exception;
		}
	}

	@Override
	public boolean respondsTo(final int eventType) {
		return eventTypes.contains(eventType);
	}

	public void setEventTypes(final Set<Integer> eventTypes) {
		this.eventTypes = eventTypes;
	}
}