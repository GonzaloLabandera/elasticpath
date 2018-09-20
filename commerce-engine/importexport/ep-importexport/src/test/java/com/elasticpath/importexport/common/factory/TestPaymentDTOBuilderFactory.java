/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.importexport.common.factory;

import com.elasticpath.common.dto.customer.CreditCardDTO;
import com.elasticpath.common.dto.customer.LegacyCreditCardDTO;
import com.elasticpath.common.dto.customer.builder.AbstractCreditCardDTOBuilder;
import com.elasticpath.common.dto.customer.builder.CreditCardDTOBuilder;
import com.elasticpath.common.dto.customer.builder.LegacyCreditCardDTOBuilder;
import com.elasticpath.common.dto.customer.builder.PaymentTokenDTOBuilder;

/**
 * A factory for producing {@link AbstractCreditCardDTOBuilder}s.
 */
public class TestPaymentDTOBuilderFactory {
	private static final String TEST_CARD_HOLDER_NAME = "testCardHolderName";
	private static final String TEST_CARD_TYPE = "testCardType";
	private static final String TEST_EXPIRY_YEAR = "testExpiryYear";
	private static final String TEST_EXPIRY_MONTH = "testExpiryMonth";

	private static final String TEST_START_MONTH = "testStartMonth";
	private static final String TEST_START_YEAR = "testStartYear";
	private static final String TEST_PAYMENT_TOKEN_DISPLAY_VALUE = "testPaymentTokenDisplayValue";

	/**
	 * Create test {@link LegacyCreditCardDTOBuilder} pre-populated with default test values for everything except card number and guid, which are
	 * required to be unique.
	 *
	 * @param identity the identity to use
	 *
	 * @return the {@link LegacyCreditCardDTOBuilder}
	 */
	public AbstractCreditCardDTOBuilder<LegacyCreditCardDTO> createLegacyWithIdentity(final String identity) {
		return new LegacyCreditCardDTOBuilder()
				.withCardNumber(identity)
				.withGuid(identity)
				.withCardHolderName(TEST_CARD_HOLDER_NAME)
				.withCardType(TEST_CARD_TYPE)
				.withExpiryYear(TEST_EXPIRY_YEAR)
				.withExpiryMonth(TEST_EXPIRY_MONTH)
				.withIssueNumber(1)
				.withStartMonth(TEST_START_MONTH)
				.withStartYear(TEST_START_YEAR);
	}

	/**
	 * Create {@link CreditCardDTOBuilder} pre-populated with default test values.
	 *
	 * @param identity the identity to use
	 *
	 * @return the {@link CreditCardDTOBuilder}
	 */
	public AbstractCreditCardDTOBuilder<CreditCardDTO> createWithIdentity(final String identity) {
		return new CreditCardDTOBuilder()
				.withCardNumber(identity)
				.withGuid(identity)
				.withCardHolderName(TEST_CARD_HOLDER_NAME)
				.withCardType(TEST_CARD_TYPE)
				.withExpiryYear(TEST_EXPIRY_YEAR)
				.withExpiryMonth(TEST_EXPIRY_MONTH)
				.withIssueNumber(1)
				.withStartMonth(TEST_START_MONTH)
				.withStartYear(TEST_START_YEAR);
	}

	/**
	 * Create {@link PaymentTokenDTOBuilder} pre-populated with default test values.
	 *
	 * @param value the value to use
	 *
	 * @return the {@link PaymentTokenDTOBuilder}
	 */
	public PaymentTokenDTOBuilder createPaymentTokenWithValue(final String value) {
		return new PaymentTokenDTOBuilder()
				.withPaymentTokenDisplayValue(TEST_PAYMENT_TOKEN_DISPLAY_VALUE)
				.withPaymentTokenValue(value);
	}
}
