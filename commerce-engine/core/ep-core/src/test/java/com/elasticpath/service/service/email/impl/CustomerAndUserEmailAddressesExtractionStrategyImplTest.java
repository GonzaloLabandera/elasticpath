/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.service.email.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.Order;
import com.elasticpath.service.email.impl.CustomerAndUserEmailAddressesExtractionStrategyImpl;

/**
 * Tests {@link CustomerAndUserEmailAddressesExtractionStrategyImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerAndUserEmailAddressesExtractionStrategyImplTest {

	private static final String CUSTOMER_EMAIL_ADDRESS = "customer@test.com";
	private static final String USER_EMAIL_ADDRESS = "user@test.com";

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Order order;

	private final CustomerAndUserEmailAddressesExtractionStrategyImpl target = new CustomerAndUserEmailAddressesExtractionStrategyImpl();

	@Test
	public void testExtractToList() {

		// given
		given(order.getCustomer().getEmail()).willReturn(CUSTOMER_EMAIL_ADDRESS);
		given(order.getFieldValue("user-email")).willReturn(USER_EMAIL_ADDRESS);

		// when
		List<String> result = target.extractToList(order);

		// verify
		assertThat(result).containsExactly(CUSTOMER_EMAIL_ADDRESS, USER_EMAIL_ADDRESS);

	}

	@Test
	public void testExtractToListOnlyWithCustomerEmail() {

		// given
		given(order.getCustomer().getEmail()).willReturn(CUSTOMER_EMAIL_ADDRESS);

		// when
		List<String> result = target.extractToList(order);

		// verify
		assertThat(result).containsExactly(CUSTOMER_EMAIL_ADDRESS);

	}

	@Test
	public void testExtractToListOnlyWithUserEmail() {

		// given
		given(order.getFieldValue("user-email")).willReturn(USER_EMAIL_ADDRESS);

		// when
		List<String> result = target.extractToList(order);

		// verify
		assertThat(result).containsExactly(USER_EMAIL_ADDRESS);

	}

	@Test
	public void testExtractToListWithoutAnyEmail() {

		// when
		List<String> result = target.extractToList(order);

		// verify
		assertThat(result).isEqualTo(Collections.emptyList());

	}
}
