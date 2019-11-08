/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.validation.validators.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import javax.validation.ConstraintViolation;

import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.commons.lang3.ArrayUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.impl.CustomerImpl;

/**
 * Test class for {@link DynamicAttributeValueValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamicAttributeValueValidatorTest {

	private static final String DATE_KEY = "date";
	private static final String DATE_VALUE = "2016-08-18";
	private static final String DATE_TIME_VALUE = "2016-08-18T10:15:30+04:00";

	@InjectMocks
	private final DynamicAttributeValueValidator dynamicAttributeValueValidator = new DynamicAttributeValueValidator();

	@Mock
	private Attribute referentAttribute;

	/**
	 * A required field should give a constraint violation when given an empty value.
	 */
	@Test
	public void shouldHaveRequiredConstraintViolation() {
		when(referentAttribute.isRequired()).thenReturn(true);

		DynamicAttributeValue valueToValidate = new DynamicAttributeValue("sender name", "", referentAttribute, Collections.emptySet());

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(valueToValidate);

		assertEquals(1, actualConstraintViolations.size());

		ConstraintViolation<DynamicAttributeValue> constraintViolation = actualConstraintViolations.iterator().next();

		assertEquals("sender name", constraintViolation.getRootBean().getAttributeKey());
		assertEquals("'sender name' value is required.", constraintViolation.getMessage());
		assertEquals("{field.required}", constraintViolation.getMessageTemplate());
	}

	/**
	 * A decimal field should give decimal constraint violation when given a non-decimal value.
	 */
	@Test
	public void shouldHaveDecimalConstraintViolation() {
		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.DECIMAL);

		String attributeKey = "id";
		String attributeValue = "12ABC";

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute, Collections.emptySet());

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(1, actualConstraintViolations.size());

		ConstraintViolation<DynamicAttributeValue> constraintViolation = actualConstraintViolations.iterator().next();

		String expectedMessage = String.format("'%s' value '%s' must be a decimal.", attributeKey, attributeValue);

		assertEquals(attributeKey, constraintViolation.getRootBean().getAttributeKey());
		assertEquals(expectedMessage, constraintViolation.getMessage());
		assertEquals("{field.invalid.decimal.format}", constraintViolation.getMessageTemplate());
	}

	/**
	 * A multi option field should give constraint violation when given an invalid value.
	 */
	@Test
	public void shouldHaveMultiOptionConstraintViolation() {

		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.SHORT_TEXT);

		String attributeKey = CustomerImpl.ATT_KEY_CP_GENDER;
		String attributeValue = "X";

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute,
				Sets.newHashSet(String.valueOf(Customer.GENDER_MALE), String.valueOf(Customer.GENDER_FEMALE)));

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(1, actualConstraintViolations.size());

		ConstraintViolation<DynamicAttributeValue> constraintViolation = actualConstraintViolations.iterator().next();

		String expectedMessage = String.format("'%s' value '%s' must match a valid option.", attributeKey, attributeValue);

		assertEquals(attributeKey, constraintViolation.getRootBean().getAttributeKey());
		assertEquals(expectedMessage, constraintViolation.getMessage());
		assertEquals("{field.invalid.option.value}", constraintViolation.getMessageTemplate());
	}

	@Test
	public void shouldHaveNoConstraintViolations() {
		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.SHORT_TEXT);

		String attributeKey = CustomerImpl.ATT_KEY_CP_GENDER;
		String attributeValue = "M";

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute,
				Sets.newHashSet(String.valueOf(Customer.GENDER_MALE), String.valueOf(Customer.GENDER_FEMALE)));

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(0, actualConstraintViolations.size());
	}

	/**
	 * An integer field should give an integer constraint violation when given a decimal value.
	 */
	@Test
	public void shouldHaveIntegerConstraintViolation() {
		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.INTEGER);

		String attributeKey = "id";
		String attributeValue = "12.00";

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute, Collections.emptySet());

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(1, actualConstraintViolations.size());

		String expectedMessage1 = String.format("'%s' value '%s' must be an integer.", attributeKey, attributeValue);

		String[] expectedConstraintMessages = {expectedMessage1};
		String[] expectedConstraintMessageTemplate = {"{field.invalid.integer.format}"};

		containsExpectedConstraints(actualConstraintViolations, fieldToValidate.getAttributeKey(),
				expectedConstraintMessages, expectedConstraintMessageTemplate);
	}

	/**
	 * A Date field should give date constraint violation when given a full DateTime.
	 */
	@Test
	public void shouldHaveDateConstraintViolation() {
		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.DATE);

		String attributeKey = DATE_KEY;
		String attributeValue = DATE_TIME_VALUE;

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute, Collections.emptySet());

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(1, actualConstraintViolations.size());

		String expectedMessage1 = String.format("'%s' value '%s' must be in ISO8601 date format (YYYY-MM-DD).", attributeKey, attributeValue);

		String[] expectedConstraintMessages = {expectedMessage1};
		String[] expectedConstraintMessageTemplate = {"{field.invalid.date.format}"};

		containsExpectedConstraints(actualConstraintViolations, fieldToValidate.getAttributeKey(),
				expectedConstraintMessages, expectedConstraintMessageTemplate);
	}

	/**
	 * A Date field should not give date constraint violation when given a Date.
	 */
	@Test
	public void shouldHaveNoDateConstraintViolation() {
		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.DATE);

		String attributeKey = DATE_KEY;
		String attributeValue = DATE_VALUE;

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute, Collections.emptySet());

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(0, actualConstraintViolations.size());
	}

	/**
	 * A DateTime field should give date constraint violation when given a Date.
	 */
	@Test
	public void shouldHaveDateTimeConstraintViolation() {
		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.DATETIME);

		String attributeKey = DATE_KEY;
		String attributeValue = DATE_VALUE;

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute, Collections.emptySet());

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(1, actualConstraintViolations.size());

		String expectedMessage1 = String.format("'%s' value '%s' must be in ISO8601 date time format (YYYY-MM-DDThh:mm:ssTZD).", attributeKey,
				attributeValue);

		String[] expectedConstraintMessages = {expectedMessage1};
		String[] expectedConstraintMessageTemplate = {"{field.invalid.datetime.format}"};

		containsExpectedConstraints(actualConstraintViolations, fieldToValidate.getAttributeKey(),
				expectedConstraintMessages, expectedConstraintMessageTemplate);
	}

	/**
	 * A DateTime field should not give date constraint violation when given a Full DateTime.
	 */
	@Test
	public void shouldHaveNoDateTimeConstraintViolation() {
		when(referentAttribute.isRequired()).thenReturn(true);
		when(referentAttribute.getAttributeType()).thenReturn(AttributeType.DATETIME);

		String attributeKey = DATE_KEY;
		String attributeValue = DATE_TIME_VALUE;

		DynamicAttributeValue fieldToValidate = new DynamicAttributeValue(attributeKey, attributeValue, referentAttribute, Collections.emptySet());

		Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations = dynamicAttributeValueValidator.validate(fieldToValidate);

		assertEquals(0, actualConstraintViolations.size());
	}

	private void containsExpectedConstraints(final Set<ConstraintViolation<DynamicAttributeValue>> actualConstraintViolations,
			final String fieldName,
			final String[] expectedConstraintMessages,
			final String[] expectedConstraintMessageTemplate) {

		for (ConstraintViolation<DynamicAttributeValue> constraintViolation : actualConstraintViolations) {
			assertEquals(fieldName, constraintViolation.getRootBean().getAttributeKey());
			assertTrue(ArrayUtils.contains(expectedConstraintMessages, constraintViolation.getMessage()));
			assertTrue(ArrayUtils.contains(expectedConstraintMessageTemplate, constraintViolation.getMessageTemplate()));
		}
	}
}
