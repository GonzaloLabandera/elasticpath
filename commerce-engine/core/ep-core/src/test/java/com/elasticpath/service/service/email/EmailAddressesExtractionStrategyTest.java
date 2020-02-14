/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.service.service.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.order.Order;
import com.elasticpath.service.email.EmailAddressesExtractionStrategy;

/**
 * Tests {@link EmailAddressesExtractionStrategy}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailAddressesExtractionStrategyTest {

	@Spy
	private EmailAddressesExtractionStrategy target;

	@Mock
	private Order order;

	@Before
	public void setUp() {
		given(target.extractToList(order)).willReturn(Arrays.asList("test1@test.com", "test2@test.com"));
	}

	@Test
	public void testExtractToInline() {

		// when
		final String result = target.extractToInline(order);

		// verify
		assertThat(result).isEqualTo("test1@test.com,test2@test.com");
	}

}
