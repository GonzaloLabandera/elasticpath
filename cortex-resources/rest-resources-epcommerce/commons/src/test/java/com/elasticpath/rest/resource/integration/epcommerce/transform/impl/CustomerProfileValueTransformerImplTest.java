/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.integration.epcommerce.transform.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.rest.resource.integration.epcommerce.transform.CustomerProfileValueTransformer;
import com.elasticpath.rest.resource.integration.epcommerce.transform.DateForEditTransformer;

/**
 * Test class for {@link DateForEditTransformerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerProfileValueTransformerImplTest {

	private static final String VALUE = "VALUE";
	@Mock
	private DateForEditTransformer dateForEditTransformer;

	@Mock
	private CustomerProfileValue customerProfileValue;

	@InjectMocks
	private final CustomerProfileValueTransformer transformer = new CustomerProfileValueTransformerImpl();

	/**
	 * Test transform with null value.
	 */
	@Test(expected = NullPointerException.class)
	public void testTransformWithNullValue() {

		transformer.transformToString(null);
	}

	/**
	 * Test date transform.
	 */
	@Test
	public void testTransformToDate() {
		Date date = new Date();

		when(customerProfileValue.getAttributeType())
				.thenReturn(AttributeType.DATETIME);
		when(customerProfileValue.getDateValue())
				.thenReturn(date);
		transformer.transformToString(customerProfileValue);
		verify(dateForEditTransformer).transformToString(AttributeType.DATETIME, date);
	}

	/**
	 * Test string transform.
	 */
	@Test
	public void testTransformToString() {
		when(customerProfileValue.getAttributeType())
				.thenReturn(AttributeType.SHORT_TEXT);
		when(customerProfileValue.toString())
				.thenReturn(VALUE);
		assertThat(transformer.transformToString(customerProfileValue))
				.as("transformed value does not match")
				.isEqualTo(VALUE);
	}
}
