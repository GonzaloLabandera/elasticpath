/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.messaging.predicate.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.messaging.EventMessage;
import com.elasticpath.messaging.EventType;
import com.elasticpath.messaging.impl.EventMessageImpl;

/**
 * Test class for {@link PropertyAndCompatibleEventTypePredicate}.
 */
public class PropertyAndCompatibleEventTypePredicateTest {

	private static final String IS_FLYING = "isFlying";

	private PropertyAndCompatibleEventTypePredicate predicate;

	@Before
	public void setUp() {
		predicate = new PropertyAndCompatibleEventTypePredicate();
	}

	@Test
	public void shouldReturnFalseWhenEventMessageHasCompatibleEventTypeAndNoAdditionalProperty()  {
		predicate.setCompatibleEventTypes(new BasicEventType("one"), new BasicEventType("two"), new BasicEventType("three"));

		final EventMessage eventMessage = new EventMessageImpl(new BasicEventType("two"), "guid");

		assertFalse("Predicate did not return false for message with compatible Event Type and missing data structure",
				predicate.apply(eventMessage));
	}

	@Test
	public void shouldReturnTrueWhenEventMessageHasCompatibleEventTypeAndMatchesAdditionalProperty()  {
		predicate.setCompatibleEventTypes(new BasicEventType("uno"), new BasicEventType("dos"), new BasicEventType("tres"));
		predicate.setPropertyName(IS_FLYING);
		predicate.setPropertyValue("superman");

		Map<String, Object> data  = new HashMap<>();
		data.put(IS_FLYING, "superman");

		final EventMessage eventMessage = new EventMessageImpl(new BasicEventType("dos"), "guid1", data);

		assertTrue("Predicate did not return true for message with compatible Event Type and matching additional property",
				predicate.apply(eventMessage));
	}

	@Test
	public void shouldReturnFalseWhenEventMessageHasCompatibleEventTypeAndDoesNotMatchAdditionalProperty()  {
		predicate.setCompatibleEventTypes(new BasicEventType("un"), new BasicEventType("deux"), new BasicEventType("trois"));
		predicate.setPropertyName(IS_FLYING);
		predicate.setPropertyValue("superman");

		Map<String, Object> data  = new HashMap<>();
		data.put(IS_FLYING, "hawkeye");

		final EventMessage eventMessage = new EventMessageImpl(new BasicEventType("trois"), "guid2", data);

		assertFalse("Predicate did not return false for message with compatible Event Type and non-matching additional property",
				predicate.apply(eventMessage));
	}

	@Test
	public void shouldReturnFalseWhenEventMessageHasCompatibleEventTypeAndDoesNotMatchAdditionalPropertyWithNullValue()  {
		predicate.setCompatibleEventTypes(new BasicEventType("ena"), new BasicEventType("dyo"), new BasicEventType("tria"));
		predicate.setPropertyName(IS_FLYING);
		predicate.setPropertyValue("batman");

		Map<String, Object> data  = new HashMap<>();
		data.put(IS_FLYING, null);

		final EventMessage eventMessage = new EventMessageImpl(new BasicEventType("tria"), "guid3", data);

		assertFalse("Predicate did not return false for message with compatible Event Type and additional property with null value",
				predicate.apply(eventMessage));
	}

	@Test
	public void shouldReturnTrueWhenEventMessageHasCompatibleEventTypeAndMultipleDataFieldsAndOneIsMatching()  {
		predicate.setCompatibleEventTypes(new BasicEventType("ena"), new BasicEventType("dyo"), new BasicEventType("tria"));
		predicate.setPropertyName(IS_FLYING);
		predicate.setPropertyValue("batman");

		Map<String, Object> data  = new HashMap<>();
		data.put(IS_FLYING, "batman");
		data.put("black", "widow");

		final EventMessage eventMessage = new EventMessageImpl(new BasicEventType("dyo"), "guid4", data);

		assertTrue("Predicate did not return true for message with compatible Event Type, multiple data fields and one matching",
				predicate.apply(eventMessage));
	}

	@Test
	public void shouldReturnFalseWhenEventMessageHasIncompatibleEventType() {
		predicate.setCompatibleEventTypes(new BasicEventType("alpha"), new BasicEventType("beta"), new BasicEventType("gamma"));

		final EventMessage eventMessage = new EventMessageImpl(new BasicEventType("omega"), "guid");

		assertFalse("Predicate did not return false for message with incompatible Event Type", predicate.apply(eventMessage));
	}

	private final class BasicEventType implements EventType {
		private static final long serialVersionUID = -5294655962068485522L;
		private final String name;

		BasicEventType(final String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean equals(final Object that) {
			return EqualsBuilder.reflectionEquals(this, that);
		}

		@Override
		public int hashCode() {
			return name.hashCode();
		}
	}

}
