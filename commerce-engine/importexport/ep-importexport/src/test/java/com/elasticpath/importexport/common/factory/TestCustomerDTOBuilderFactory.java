/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.factory;

import com.elasticpath.common.dto.customer.PaymentMethodDto;
import com.elasticpath.common.dto.customer.builder.CustomerDTOBuilder;

/**
 * A factory for producing test {@link CustomerDTOBuilder}s.
 */
public class TestCustomerDTOBuilderFactory {
	private static final String TEST_GUID = "testGuid";
	private static final String TEST_IDENTITY = "testIdentity";
	private static final String TEST_PAYMENT_TOKEN_VALUE = "testPaymentTokenValue";
	private final TestPaymentDTOBuilderFactory testPaymentDTOBuilderFactory = new TestPaymentDTOBuilderFactory();

	/**
	 * Create {@link CustomerDTOBuilder} pre-populated with default test values.
	 *
	 * @return the {@link CustomerDTOBuilder}
	 */
	public CustomerDTOBuilder create() {
		return new CustomerDTOBuilder()
				.withGuid(TEST_GUID);
	}

	/**
	 * Create {@link CustomerDTOBuilder} pre-populated with default test payment methods and values.
	 *
	 * @return the {@link CustomerDTOBuilder}
	 */
	public CustomerDTOBuilder createWithPaymentMethods() {
		PaymentMethodDto testCreditCard = testPaymentDTOBuilderFactory.createWithIdentity(TEST_IDENTITY)
				.build();
		PaymentMethodDto testPaymentToken = testPaymentDTOBuilderFactory.createPaymentTokenWithValue(TEST_PAYMENT_TOKEN_VALUE)
				.build();
		return create()
				.withPaymentMethods(testCreditCard, testPaymentToken)
				.withDefaultPaymentMethod(testCreditCard);
	}
}
