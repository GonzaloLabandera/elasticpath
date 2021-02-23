/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.messaging.camel.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.spi.EventTypeProvider;

/**
 * Object Mapper for serializing event messages.
 */
public interface EventMessageObjectMapper {
	/**
	 * Serialize the event message object to a JSON string.
	 * @param value the event message object
	 * @return a JSON string representation of the event message object
	 * @throws JsonProcessingException if there was a problem serializing the event message
	 */
	String writeValueAsString(Object value) throws JsonProcessingException;

	/**
	 * Registers an {@link EventType} subclass with its associated EventTypeLookup. This provides Jackson with enough information to allow
	 * deserialization from JSON to the appropriate implementation of the {@link EventType} interface.
	 *
	 * @param <T> the type of {@link EventType} to register
	 * @param eventTypeProvider provides the {@link EventType} class and lookup instance
	 */
	<T extends EventType> void registerEventType(EventTypeProvider<T> eventTypeProvider);

	/**
	 * <p>Unregisters an {@link EventType}.</p>
	 * <p>This method has not been implemented as there currently is no convenient way to unregister a deserializer from the Jackson ObjectMapper
	 * .</p>
	 *
	 * @param <T> the type of {@link EventType} to unregister
	 * @param eventTypeProvider provides the {@link EventType} class and lookup instance
	 * @throws UnsupportedOperationException always
	 */
	<T extends EventType> void unregisterEventType(EventTypeProvider<T> eventTypeProvider);
}
