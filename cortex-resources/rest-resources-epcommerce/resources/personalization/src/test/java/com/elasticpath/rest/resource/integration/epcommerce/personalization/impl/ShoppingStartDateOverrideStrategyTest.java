package com.elasticpath.rest.resource.integration.epcommerce.personalization.impl;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.tags.Tag;
import com.elasticpath.tags.TagSet;

/**
 * Created by sleslie on 2019-03-21.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingStartDateOverrideStrategyTest {

	/** Tag name. */
	private static final String SHOPPING_DATE_OVERRIDE_KEY = "SHOPPING_CONTEXT_DATE_OVERRIDE";

	/** Shopping context date tag name. */
	private static final String SHOPPING_START_TIME_KEY = "SHOPPING_START_TIME";

	private static final String OVERRIDE_DATE_STRING = "2012-01-01T00:00:00";

	ShoppingStartDateOverrideTagStrategy strategyUnderTest = new ShoppingStartDateOverrideTagStrategy();

	private Date currentTime;

	/** Override date in milliseconds since epoch taking local time zone into account. */
	private final Date overrideDate = Date.from(Instant.from(
			DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()).parse(OVERRIDE_DATE_STRING)));

	@Mock
	Customer customer;

	@Before
	public void setUp() {
		currentTime = new Date();
	}

	@Test
	public void testStartDateIsOverriden() {
		TagSet tags = givenTagSetWithOverride();

		strategyUnderTest.populate(customer, tags);

		assertEquals(tags.getTagValue(SHOPPING_START_TIME_KEY).getValue(), overrideDate.getTime());
	}

	@Test
	public void testInvalidDateDoesNotOverride() {
		TagSet tags = givenTagSetWithInvalidDate();

		strategyUnderTest.populate(customer, tags);

		assertTrue(tags.getTags().containsKey(SHOPPING_DATE_OVERRIDE_KEY));
		assertEquals(tags.getTagValue(SHOPPING_START_TIME_KEY).getValue(), currentTime.getTime());
	}

	private TagSet givenTagSetWithOverride() {
		TagSet tagSet = new TagSet();

		tagSet.addTag(SHOPPING_START_TIME_KEY, new Tag(currentTime.getTime()));
		tagSet.addTag(SHOPPING_DATE_OVERRIDE_KEY, new Tag(OVERRIDE_DATE_STRING));

		return tagSet;
	}

	private TagSet givenTagSetWithInvalidDate() {
		TagSet tagSet = new TagSet();

		tagSet.addTag(SHOPPING_START_TIME_KEY, new Tag(currentTime.getTime()));
		tagSet.addTag(SHOPPING_DATE_OVERRIDE_KEY, new Tag("INVALID DATE"));

		return tagSet;
	}
}
