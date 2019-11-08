/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.plugin.service;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.elasticpath.catalog.plugin.entity.ProjectionEntity;
import com.elasticpath.commons.exception.UnsupportedEventActionException;
import com.elasticpath.core.messaging.catalog.CatalogEventType;
import com.elasticpath.messaging.EventType;
import com.elasticpath.persistence.impl.EventTypeFactory;

/**
 * Catalog Event factory.
 */
public class CatalogEventTypeFactoryImpl implements EventTypeFactory {

	private final Map<String, EventType> identityTypeByEventType;

	/**
	 * Constructor.
	 *
	 * @param identityTypesByEventType map of supported classes and events.
	 */
	public CatalogEventTypeFactoryImpl(final Map<String, String> identityTypesByEventType) {
		this.identityTypeByEventType = identityTypesByEventType.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						entry -> CatalogEventType.valueOf(entry.getValue())));
	}

	@Override
	public EventType getEventType(final String identityType) {
		return Optional.ofNullable(identityTypeByEventType.get(identityType))
				.orElseThrow(() -> new UnsupportedEventActionException("Event type is not supported for identity: " + identityType));
	}

	@Override
	public <T> boolean isSupported(final Class<T> clazz,  final EventAction action) {
		return ProjectionEntity.class.isAssignableFrom(clazz);
	}
}
