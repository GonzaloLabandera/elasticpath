/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.common.dto.customer.builder;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.elasticpath.common.dto.customer.CreditCardDTO;

/**
 * Test {@link CreditCardDTOBuilder}.
 */
public class CreditCardDTOBuilderTest {

	private static final String TEST_CARD_TYPE = "testCardType";
	private static final String TEST_EXPIRY_YEAR = "testExpiryYear";
	private static final String TEST_EXPIRY_MONTH = "testExpiryMonth";
	private static final String TEST_GUID = "testGuid";
	private static final String TEST_CARD_HOLDER_NAME = "testCardHolderName";
	private static final String TEST_CARD_NUMBER = "testCardNumber";
	private static final String TEST_START_YEAR = "testStartYear";
	private static final String TEST_START_MONTH = "testStartMonth";
	private static final int TEST_ISSUE_NUMBER = 0;

	/**
	 * Test to ensure build produces correct credit card DTO.
	 */
	@Test
	public void ensureBuildProducesCorrectCreditCardDTO() {
		CreditCardDTO expectedCreditCardDTO = new CreditCardDTO();
		expectedCreditCardDTO.setCardType(TEST_CARD_TYPE);
		expectedCreditCardDTO.setExpiryYear(TEST_EXPIRY_YEAR);
		expectedCreditCardDTO.setExpiryMonth(TEST_EXPIRY_MONTH);
		expectedCreditCardDTO.setGuid(TEST_GUID);
		expectedCreditCardDTO.setCardHolderName(TEST_CARD_HOLDER_NAME);
		expectedCreditCardDTO.setCardNumber(TEST_CARD_NUMBER);
		expectedCreditCardDTO.setStartYear(TEST_START_YEAR);
		expectedCreditCardDTO.setStartMonth(TEST_START_MONTH);
		expectedCreditCardDTO.setIssueNumber(TEST_ISSUE_NUMBER);

		CreditCardDTO creditCardDTO = new CreditCardDTOBuilder()
				.withCardType(TEST_CARD_TYPE)
				.withExpiryYear(TEST_EXPIRY_YEAR)
				.withExpiryMonth(TEST_EXPIRY_MONTH)
				.withGuid(TEST_GUID)
				.withCardHolderName(TEST_CARD_HOLDER_NAME)
				.withCardNumber(TEST_CARD_NUMBER)
				.withStartYear(TEST_START_YEAR)
				.withStartMonth(TEST_START_MONTH)
				.withIssueNumber(TEST_ISSUE_NUMBER)
				.build();

		assertEquals("The built credit card DTO should be the same as expected", expectedCreditCardDTO, creditCardDTO);
	}
}
