/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.messaging.predicate.impl;

import java.util.Map;

import com.elasticpath.messaging.EventMessage;

/**
 * The predicate based on custom property and compatible event types.
 *
 * The {@link EventMessage} is valid if the event type is compatible with expected ones and
 * data property, if found, matches the expected value.
 */
public class PropertyAndCompatibleEventTypePredicate extends CompatibleEventTypePredicate {

	//property name and value to compare
	private String propertyName;
	private String propertyValue;

	@Override
	public boolean apply(final EventMessage eventMessage) {
		boolean isApplicable = super.apply(eventMessage);

		if (isApplicable) {
			Map<String, Object> msgData = eventMessage.getData();

			boolean dataFieldExists = msgData != null && msgData.containsKey(propertyName);

			if (dataFieldExists) {
				return propertyValue.equals(String.valueOf(msgData.get(propertyName)));
			}

			return false;
		}

		return false;
	}

	public void setPropertyName(final String propertyName) {
		this.propertyName = propertyName;
	}

	public void setPropertyValue(final String propertyValue) {
		this.propertyValue = propertyValue;
	}
}
