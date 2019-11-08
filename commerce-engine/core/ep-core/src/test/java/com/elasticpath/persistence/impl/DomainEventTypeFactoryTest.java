package com.elasticpath.persistence.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.exception.UnsupportedEventActionException;
import com.elasticpath.core.messaging.domain.DomainEventType;
import com.elasticpath.domain.catalog.impl.LinkedCategoryImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;

@RunWith(MockitoJUnitRunner.class)
public class DomainEventTypeFactoryTest {

	private Map<Class<?>, List<String>> supportedClassByEventTypes;

	@Before
	public void setUp() {
		final List<String> events = Arrays.asList("CATEGORY_LINK_CREATED", "CATEGORY_LINK_DELETED");
		supportedClassByEventTypes = new HashMap<>();
		supportedClassByEventTypes.put(LinkedCategoryImpl.class, events);
	}

	@Test
	public void testThatDomainEventTypeFactoryImplIsSupportedEventWithValidClassAndValidEventType() {
		EventTypeFactory factory = new DomainEventTypeFactoryImpl(supportedClassByEventTypes);
		assertThat(factory.isSupported(LinkedCategoryImpl.class, EventTypeFactory.EventAction.DELETED)).isTrue();
	}

	@Test
	public void testThatDomainEventTypeFactoryImplIsNotSupportedEventWithNotValidClassAndValidEventType() {
		EventTypeFactory factory = new DomainEventTypeFactoryImpl(supportedClassByEventTypes);
		assertThat(factory.isSupported(SkuOption.class, EventTypeFactory.EventAction.DELETED)).isFalse();
	}

	@Test
	public void testThatDomainEventTypeFactoryImplIsNotSupportedEventWithValidClassAndNotValidEventType() {
		EventTypeFactory factory = new DomainEventTypeFactoryImpl(supportedClassByEventTypes);
		assertThat(factory.isSupported(LinkedCategoryImpl.class, EventTypeFactory.EventAction.UPDATED)).isFalse();
	}

	@Test
	public void testThatDomainEventTypeFactoryImplGetValidEventType() {
		EventTypeFactory factory = new DomainEventTypeFactoryImpl(supportedClassByEventTypes);
		assertThat(factory.getEventType(LinkedCategoryImpl.class, EventTypeFactory.EventAction.CREATED))
				.isEqualTo(DomainEventType.CATEGORY_LINK_CREATED);
	}

	@Test(expected = UnsupportedEventActionException.class)
	public void testThatDomainEventTypeFactoryThrowsExceptionInCaseInvalidEventType() {
		EventTypeFactory factory = new DomainEventTypeFactoryImpl(supportedClassByEventTypes);
		factory.getEventType(LinkedCategoryImpl.class, EventTypeFactory.EventAction.UPDATED);
	}
}
