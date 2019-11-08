/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.webservice.request.entity;

import java.util.Map;

/**
 * Represents the POST request body.
 */
public class RequestBody {

	private EventType eventType;
	private String guid;
	private Map<String, Object> data;

	/**
	 * Get eventType.
	 *
	 * @return eventType.
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * Set eventType.
	 *
	 * @param eventType of request.
	 */
	public void setEventType(final EventType eventType) {
		this.eventType = eventType;
	}

	/**
	 * Get guid.
	 *
	 * @return guid.
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * Set guid.
	 *
	 * @param guid of request.
	 */
	public void setGuid(final String guid) {
		this.guid = guid;
	}

	/**
	 * Get request data.
	 *
	 * @return request data.
	 */
	public Map<String, Object> getData() {
		return data;
	}

	/**
	 * Set request date.
	 *
	 * @param data of request.
	 */
	public void setData(final Map<String, Object> data) {
		this.data = data;
	}

}
