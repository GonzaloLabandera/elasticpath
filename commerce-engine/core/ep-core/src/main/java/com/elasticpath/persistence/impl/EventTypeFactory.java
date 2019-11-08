/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.persistence.impl;

import com.elasticpath.commons.exception.UnsupportedEventActionException;
import com.elasticpath.messaging.EventType;

/**
 * Event type factory is responsible for building an appropriate {@link EventType event type} based on class and domain event action.
 */
public interface EventTypeFactory {

	/**
	 * Factory method to get an event type for given class and domain event action.
	 *
	 * @param clazz  class.
	 * @param action event action.
	 * @return event type.
	 */
	default EventType getEventType(Class<?> clazz, EventAction action) {
		throw new UnsupportedEventActionException("Unsupported action");
	}

	/**
	 * Factory method to get an event type for given identity.
	 * Identity is a guid or any other unique identifier.
	 *
	 * @param identity identity to get event type for.
	 * @return event type.
	 */
	default EventType getEventType(String identity) {
		throw new UnsupportedEventActionException("Unsupported action");
	}

	/**
	 * Is the class is supported and event type can be built.
	 *
	 * @param <T>   type.
	 * @param clazz class to check.
	 * @param action action to check.
	 * @return true, if type is supported and event type can be built, false otherwise.
	 */
	<T> boolean isSupported(Class<T> clazz, EventAction action);

	/**
	 * Represents domain event action.
	 */
	enum EventAction {
		/**
		 * Created event.
		 */
		CREATED,
		/**
		 * Created event.
		 */
		UPDATED,
		/**
		 * Created event.
		 */
		DELETED
	}
}