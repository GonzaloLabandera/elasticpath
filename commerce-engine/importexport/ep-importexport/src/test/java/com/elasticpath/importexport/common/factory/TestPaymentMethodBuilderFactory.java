/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.factory;

import com.elasticpath.domain.customer.impl.AbstractCustomerCreditCardBuilder;
import com.elasticpath.domain.customer.impl.PaymentTokenImpl;

/**
 * Creates builders for instances of {@link com.elasticpath.plugin.payment.dto.PaymentMethod} populated with sensible default values to be used for
 * testing.
 */
public class TestPaymentMethodBuilderFactory {
	private static final String TEST_EXPIRY_YEAR = "testExpiryYear";
	private static final String TEST_EXPIRY_MONTH = "testExpiryMonth";
	private static final String TEST_CARD_TYPE = "visa";
	private static final String TEST_CARD_HOLDER_NAME = "testCardHolderName";
	private static final String TEST_TOKEN_DISPLAY_VALUE = "testTokenDisplayValue";

	private AbstractCustomerCreditCardBuilder customerCreditCardBuilder;

	/**
	 * Creates a {@link AbstractCustomerCreditCardBuilder} initialized with card number and guid set to the specified identity, and defaults for all
	 * other fields.
	 * @param identity the identity
	 * @return the builder
	 */
	public AbstractCustomerCreditCardBuilder createCreditCardBuilderWithIdentity(final String identity) {
		return getCustomerCreditCardBuilder().withCardNumber(identity)
				.withGuid(identity)
				.withCardHolderName(TEST_CARD_HOLDER_NAME)
				.withCardType(TEST_CARD_TYPE)
				.withExpiryMonth(TEST_EXPIRY_MONTH)
				.withExpiryYear(TEST_EXPIRY_YEAR)
				.withIssueNumber(0);
	}

	/**
	 * Creates a {@link PaymentTokenImpl.TokenBuilder} initialized with value set to identity, and a default display value.
	 * @param identity the identity
	 * @return the builder
	 */
	public PaymentTokenImpl.TokenBuilder createPaymentTokenBuilderWithIdentity(final String identity) {
		return new PaymentTokenImpl.TokenBuilder()
				.withDisplayValue(TEST_TOKEN_DISPLAY_VALUE)
				.withValue(identity);
	}

	protected AbstractCustomerCreditCardBuilder getCustomerCreditCardBuilder() {
		return customerCreditCardBuilder;
	}

	public void setCustomerCreditCardBuilder(final AbstractCustomerCreditCardBuilder customerCreditCardBuilder) {
		this.customerCreditCardBuilder = customerCreditCardBuilder;
	}
}