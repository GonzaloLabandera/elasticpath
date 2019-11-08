/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.persistence.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elasticpath.commons.exception.UnsupportedEventActionException;
import com.elasticpath.core.messaging.domain.DomainEventType;
import com.elasticpath.messaging.EventType;

/**
 * Implementation of {@link EventTypeFactory}.
 */
public class DomainEventTypeFactoryImpl implements EventTypeFactory {

	private final Map<Class<?>, List<EventType>> supportedClassByEventTypes;

	/**
	 * Constructor.
	 *
	 * @param supportedClassByEventTypes map of supported classes and events.
	 */
	public DomainEventTypeFactoryImpl(final Map<Class<?>, List<String>> supportedClassByEventTypes) {
		this.supportedClassByEventTypes = supportedClassByEventTypes.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						entry -> entry.getValue().stream().map(DomainEventType::valueOf).collect(Collectors.toList())));
	}

	@Override
	public EventType getEventType(final Class<?> clazz, final EventAction action) {
		return supportedClassByEventTypes
				.getOrDefault(clazz, Collections.emptyList())
				.stream()
				.filter(eventType -> eventType.toString().contains(action.toString()))
				.findAny()
				.orElseThrow(() -> new UnsupportedEventActionException("Event action " + action + " is not supported for class: " + clazz));
	}

	@Override
	public <T> boolean isSupported(final Class<T> clazz, final EventAction action) {
		return supportedClassByEventTypes
				.getOrDefault(clazz, Collections.emptyList())
				.stream()
				.anyMatch(eventType -> eventType.toString().contains(action.toString()));
	}
}
