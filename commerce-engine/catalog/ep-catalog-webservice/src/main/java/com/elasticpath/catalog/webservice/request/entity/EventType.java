/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.request.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents of event type entity.
 */
public class EventType {

	private String eventClass;
	private String name;

	/**
	 * Get class of event.
	 *
	 * @return class of event.
	 */
	@JsonProperty("@class")
	public String getEventClass() {
		return eventClass;
	}

	/**
	 * Set class of events.
	 *
	 * @param eventClass class of events.
	 */
	public void setEventClass(final String eventClass) {
		this.eventClass = eventClass;
	}

	/**
	 * Get event type name.
	 *
	 * @return name of event type.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of event type.
	 *
	 * @param name name of event type.
	 */
	public void setName(final String name) {
		this.name = name;
	}

}
