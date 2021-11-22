package com.elasticpath.persistence.impl;

import static com.elasticpath.core.messaging.domain.DomainEventType.CATEGORY_DELETED;
import static com.elasticpath.core.messaging.domain.DomainEventType.SKU_OPTION_CREATED;
import static com.elasticpath.core.messaging.domain.DomainEventType.SKU_OPTION_DELETED;
import static com.elasticpath.core.messaging.domain.DomainEventType.SKU_OPTION_UPDATED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.LifecycleEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.messaging.factory.EventMessageFactory;
import com.elasticpath.messaging.impl.EventMessageImpl;
import com.elasticpath.persistence.api.Entity;
import com.elasticpath.service.messaging.OutboxMessageService;
import com.elasticpath.settings.SettingsReader;
import com.elasticpath.settings.domain.SettingValue;

@RunWith(MockitoJUnitRunner.class)
public class DomainEventListenerTest {

	private static final String SOME_GUID = "someGuid";
	private static final String DOMAIN_EVENT_CHANNEL_SETTING_PATH = "SETTING_PATH";
	private static final String DOMAIN_EVENT_CHANNEL_URI = "jms:whatever";

	@Mock
	private EventMessageFactory eventMessageFactory;

	@Mock
	private EventTypeFactory eventTypeFactory;

	@Mock
	private SampleEntity entity;

	@Mock
	private BeanFactory beanFactory;

	@Mock
	private OutboxMessageService outboxMessageService;

	@Mock
	private SettingsReader settingsReader;

	@Mock
	private SettingValue domainEventChannelSettingPath;

	@Mock
	private LifecycleEventFilter lifecycleEventFilter;

	@InjectMocks
	private DomainEventListener domainEventListener;

	@Before
	public void setUp() throws Exception {
		when(eventTypeFactory.isSupported(any(), any())).thenReturn(true);
		when(lifecycleEventFilter.wasPreviouslyProcessed(any(), any(), any())).thenReturn(false);
		when(entity.pcIsDirty()).thenReturn(true);
		when(entity.getGuid()).thenReturn(SOME_GUID);
		when(beanFactory.getSingletonBean(ContextIdNames.OUTBOX_MESSAGE_SERVICE, OutboxMessageService.class))
				.thenReturn(outboxMessageService);
		when(beanFactory.getSingletonBean(ContextIdNames.SETTINGS_READER, SettingsReader.class))
				.thenReturn(settingsReader);
		domainEventListener.setDomainEventChannelSettingPath(DOMAIN_EVENT_CHANNEL_SETTING_PATH);
		when(settingsReader.getSettingValue(DOMAIN_EVENT_CHANNEL_SETTING_PATH)).thenReturn(domainEventChannelSettingPath);
		when(domainEventChannelSettingPath.getValue()).thenReturn(DOMAIN_EVENT_CHANNEL_URI);
	}

	/**
	 * Ensure that eventMessageFactory call createEventMessage method and eventMessagePublisher call publish method after domain object was
	 * persisted.
	 */
	@Test
	public void testThatEventMessageFactoryCallCreateEventMessageAfterPersistEvent() {
		final LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.AFTER_PERSIST);

		when(eventTypeFactory.getEventType(any(), any())).thenReturn(SKU_OPTION_CREATED);
		when(eventMessageFactory.createEventMessage(SKU_OPTION_CREATED, SOME_GUID)).thenReturn(new EventMessageImpl(SKU_OPTION_CREATED, SOME_GUID));

		domainEventListener.afterPersist(event);

		verify(eventMessageFactory).createEventMessage(SKU_OPTION_CREATED, SOME_GUID);
	}

	/**
	 * Ensure that eventMessageFactory call createEventMessage method and eventMessagePublisher call publish method when domain object was updated
	 * Note, this is attached to beforeUpdate as opposed to afterAttach due to afterAttach not being hit for DST.
	 */
	@Test
	public void testThatEventMessageFactoryCallCreateEventMessageBeforeUpdatePerformed() {
		final LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.AFTER_ATTACH);

		when(eventTypeFactory.getEventType(any(), any())).thenReturn(SKU_OPTION_UPDATED);
		when(eventMessageFactory.createEventMessage(SKU_OPTION_UPDATED, SOME_GUID)).thenReturn(new EventMessageImpl(SKU_OPTION_UPDATED, SOME_GUID));

		domainEventListener.beforeUpdate(event);

		verify(eventMessageFactory).createEventMessage(SKU_OPTION_UPDATED, SOME_GUID);
	}

	/**
	 * Ensure that eventMessageFactory call createEventMessage method and eventMessagePublisher call publish method after domain object was deleted.
	 */
	@Test
	public void testThatEventMessageFactoryCallCreateEventMessageAfterDeleteEvent() {
		final LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.AFTER_DELETE);

		when(eventTypeFactory.getEventType(any(), any())).thenReturn(SKU_OPTION_DELETED);
		when(eventMessageFactory.createEventMessage(SKU_OPTION_DELETED, SOME_GUID)).thenReturn(new EventMessageImpl(SKU_OPTION_DELETED, SOME_GUID));

		domainEventListener.afterDelete(event);

		verify(eventMessageFactory).createEventMessage(SKU_OPTION_DELETED, SOME_GUID);
	}

	@Test
	public void testThatDomainEventListenerGetsCodeAsGuidFromCategory() {
		final CategoryEntity entity = mock(CategoryEntity.class);
		when(entity.getCompoundGuid()).thenReturn(SOME_GUID);
		when(entity.pcIsDirty()).thenReturn(true);

		final LifecycleEvent event = new LifecycleEvent(entity, LifecycleEvent.AFTER_DELETE);

		when(eventTypeFactory.getEventType(any(), any())).thenReturn(CATEGORY_DELETED);
		when(eventMessageFactory.createEventMessage(CATEGORY_DELETED, SOME_GUID)).thenReturn(new EventMessageImpl(CATEGORY_DELETED, SOME_GUID));

		domainEventListener.afterDelete(event);

		verify(eventMessageFactory).createEventMessage(CATEGORY_DELETED, SOME_GUID);
	}

	/**
	 * Sample Entity interface for mocking.
	 */
	private interface SampleEntity extends PersistenceCapable, Entity {
	}

	/**
	 * Sample Category Entity interface for mocking.
	 */
	private interface CategoryEntity extends SampleEntity, Category {
	}
}
