/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.persistence.impl;

import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;
import org.apache.openjpa.event.LifecycleEventManager;
import org.apache.openjpa.event.PostPersistListener;
import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.event.TransactionListener;
import org.apache.openjpa.event.UpdateListener;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.persistence.openjpa.JpaPersistenceEngine;
import com.elasticpath.persistence.openjpa.support.JPAUtil;
import com.elasticpath.service.messaging.OutboxMessageService;
import com.elasticpath.settings.SettingsReader;

/**
 * Lifecycle event listener which sends domain events for supported entities.
 */
public class DomainEventListener extends AbstractLifecycleListener
		implements PostPersistListener, UpdateListener, LifecycleEventManager.ListenerAdapter, TransactionListener {

	private static final Logger LOG = Logger.getLogger(DomainEventListener.class);

	private BeanFactory beanFactory;
	private volatile JpaPersistenceEngine persistenceEngine;
	private EventMessageFactory eventMessageFactory;
	private EventTypeFactory eventTypeFactory;
	private Set<Integer> eventTypes;
	private String domainEventChannelSettingPath;
	private LifecycleEventFilter lifecycleEventFilter;

	@Override
	public void afterPersist(final LifecycleEvent event) {
		handleEvent(event, EventActionEnum.CREATED);
	}

	@Override
	public void afterPersistPerformed(final LifecycleEvent lifecycleEvent) {
		//no-op
	}

	@Override
	public void afterDelete(final LifecycleEvent event) {
		handleEvent(event, EventActionEnum.DELETED);
	}

	@Override
	public void afterUpdatePerformed(final LifecycleEvent event) {
		//no-op
	}

	@Override
	public void beforeUpdate(final LifecycleEvent event) {
		handleEvent(event, EventActionEnum.UPDATED);
	}

	@Override
	public void afterAttach(final LifecycleEvent event) {
		handleEvent(event, EventActionEnum.UPDATED);
	}

	@Override
	public void afterBegin(final TransactionEvent transactionEvent) {
		lifecycleEventFilter.beginTransaction();
	}

	@Override
	public void beforeCommit(final TransactionEvent transactionEvent) {
		//no-op
	}

	@Override
	public void afterCommit(final TransactionEvent transactionEvent) {
		lifecycleEventFilter.endTransaction();
	}

	@Override
	public void afterRollback(final TransactionEvent transactionEvent) {
		lifecycleEventFilter.endTransaction();
	}

	@Override
	public void afterStateTransitions(final TransactionEvent transactionEvent) {
		//no-op
	}

	@Override
	public void afterCommitComplete(final TransactionEvent transactionEvent) {
		//no-op
	}

	@Override
	public void afterRollbackComplete(final TransactionEvent transactionEvent) {
		//no-op
	}

	@Override
	public void beforeFlush(final TransactionEvent transactionEvent) {
		//no-op
	}

	@Override
	public void afterFlush(final TransactionEvent transactionEvent) {
		//no-op
	}

	@SuppressWarnings({"unchecked"})
	private void handleEvent(final LifecycleEvent event, final EventActionEnum eventAction) {
		final String entityGuid = getGuid(event);
		if (eventTypeFactory.isSupported(event.getSource().getClass(), eventAction)
				&& !lifecycleEventFilter.isDuplicate(eventAction, (Class<PersistenceCapable>) event.getSource().getClass(), entityGuid)
				&& JPAUtil.isDirty((PersistenceCapable) event.getSource(), getPersistenceEngine())) {
			EventType eventType = eventTypeFactory.getEventType(event.getSource().getClass(), eventAction);

			recordDomainEvent(eventType, entityGuid);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Domain event message recorded: "
						+ eventAction + " " + event.getSource().getClass() + " with GUID " + entityGuid);
			}
		}
	}

	private String getGuid(final LifecycleEvent event) {
		if (event.getSource() instanceof Category) {
			return ((Category) event.getSource()).getCompoundGuid();
		} else if (event.getSource() instanceof Entity) {
			return ((Entity) event.getSource()).getGuid();
		} else {
			return "";
		}
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
					persistenceEngine = beanFactory.getSingletonBean(ContextIdNames.PERSISTENCE_ENGINE, JpaPersistenceEngine.class);
				}
			}
		}
		return persistenceEngine;
	}

	/**
	 * Records a domain event message in the Outbox table.
	 *
	 * @param eventType  the type of Catalog Event to trigger.
	 * @param entityGuid the guid of the domain object associated with the event.
	 */
	private void recordDomainEvent(final EventType eventType, final String entityGuid) {
		// OutboxMessageService and SettingsReader cannot be injected through Spring because this would create a circular dependency
		OutboxMessageService outboxMessageService = beanFactory.getSingletonBean(ContextIdNames.OUTBOX_MESSAGE_SERVICE,
				OutboxMessageService.class);
		SettingsReader settingsReader = beanFactory.getSingletonBean(ContextIdNames.SETTINGS_READER, SettingsReader.class);

		String camelUri = settingsReader.getSettingValue(domainEventChannelSettingPath).getValue();
		EventMessage eventMessage = eventMessageFactory.createEventMessage(eventType, entityGuid);
		outboxMessageService.add(camelUri, eventMessage);
	}

	@Override
	public boolean respondsTo(final int eventType) {
		return eventTypes.contains(eventType);
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

	public void setEventTypes(final Set<Integer> eventTypes) {
		this.eventTypes = eventTypes;
  }

	public void setDomainEventChannelSettingPath(final String domainEventChannelSettingPath) {
		this.domainEventChannelSettingPath = domainEventChannelSettingPath;
	}

	protected LifecycleEventFilter getLifecycleEventFilter() {
		return lifecycleEventFilter;
	}

	public void setLifecycleEventFilter(final LifecycleEventFilter lifecycleEventFilter) {
		this.lifecycleEventFilter = lifecycleEventFilter;
	}
}
