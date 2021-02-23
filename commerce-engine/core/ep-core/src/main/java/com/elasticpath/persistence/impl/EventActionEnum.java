/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.persistence.impl;

/**
 * Represents domain event action.
 */
public enum EventActionEnum {

	/**
	 * Entity created.
	 */
	CREATED(EventActionGroupEnum.CREATED_OR_UPDATED),

	/**
	 * Entity updated.
	 */
	UPDATED(EventActionGroupEnum.CREATED_OR_UPDATED),

	/**
	 * Entity deleted.
	 */
	DELETED(EventActionGroupEnum.DELETED);

	private EventActionGroupEnum eventActionGroup;

	/**
	 * Constructor.
	 * @param eventActionGroup the associated event action group
	 */
	EventActionEnum(final EventActionGroupEnum eventActionGroup) {
		this.eventActionGroup = eventActionGroup;
	}

	/**
	 * Return the event action group for this event action.
	 * @return event action group
	 */
	public EventActionGroupEnum getEventActionGroup() {
		return eventActionGroup;
	}
}
