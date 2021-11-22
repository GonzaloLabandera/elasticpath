/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.domain.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.CustomerProfileValue;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.attribute.impl.CustomerProfileValueImpl;
import com.elasticpath.domain.customer.CustomerProfile;

/**
 * Test of the public API of <code>CustomerProfileImpl</code>.
 */
public class CustomerProfileImplTest {

	private static final String NEW_NULL_VALUE = null;
	private static final String OLD_EMPTY_VALUE = StringUtils.EMPTY;
	private static final String TEST_KEY = "testKey";

	@Test
	public void setStringProfileValueDoesNotChangeEmptyAttributeToNullTest() {
		CustomerProfileValue customerProfileValue = getCustomerProfileValue(OLD_EMPTY_VALUE);
		CustomerProfile customerProfile = createCustomerProfile(customerProfileValue);

		customerProfile.setStringProfileValue(TEST_KEY, NEW_NULL_VALUE);

		assertThat(customerProfileValue.getStringValue()).isEqualTo(OLD_EMPTY_VALUE);
	}

	private CustomerProfile createCustomerProfile(final CustomerProfileValue customerProfileValue) {
		CustomerProfileImpl customerProfile = new CustomerProfileImpl();
		Attribute attribute = new AttributeImpl();
		attribute.setKey(TEST_KEY);
		attribute.setAttributeType(AttributeType.SHORT_TEXT);

		customerProfile.setCustomerProfileAttributeMap(Collections.singletonMap(TEST_KEY, attribute));
		Map<String, CustomerProfileValue> profileValueMap = Collections.singletonMap(TEST_KEY, customerProfileValue);
		customerProfile.setProfileValueMap(profileValueMap);
		return customerProfile;
	}

	private CustomerProfileValue getCustomerProfileValue(final String value) {
		Attribute attribute = new AttributeImpl();
		attribute.setKey(TEST_KEY);
		attribute.setAttributeType(AttributeType.SHORT_TEXT);
		CustomerProfileValue customerProfileValue = new CustomerProfileValueImpl();
		customerProfileValue.setAttribute(attribute);
		customerProfileValue.setAttributeType(AttributeType.SHORT_TEXT);
		customerProfileValue.setStringValue(value);
		return customerProfileValue;
	}
}
