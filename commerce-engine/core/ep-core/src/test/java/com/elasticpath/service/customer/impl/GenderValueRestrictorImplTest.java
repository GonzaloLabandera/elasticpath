/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.service.customer.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import org.assertj.core.api.Condition;
import org.assertj.core.util.Lists;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.service.customer.CustomerProfileAttributeValueRestrictorContext;

/**
 * Test class for <code>GenderValueRestrictorImpl</code>.
 */
public class GenderValueRestrictorImplTest {

	private final GenderValueRestrictorImpl restrictor = new GenderValueRestrictorImpl();

	private final List<String> genders = Lists.list(String.valueOf(Customer.GENDER_FEMALE), String.valueOf(Customer.GENDER_MALE));

	@Test
	public void testGetRestrictedValues() {
		CustomerProfileAttributeValueRestrictorContext context =
				new CustomerProfileAttributeValueRestrictorContextImpl(Lists.emptyList());

		assertThat(restrictor.getRestrictedValues(context))
				.as("Mismatched set of restricted currencies.")
				.hasSize(2)
				.have(new Condition<>(genders::contains, "gender"));
	}
}
