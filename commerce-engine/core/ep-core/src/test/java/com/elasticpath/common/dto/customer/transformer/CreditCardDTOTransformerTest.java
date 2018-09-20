/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.common.dto.customer.transformer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.common.dto.customer.CreditCardDTO;
import com.elasticpath.common.dto.customer.builder.CreditCardDTOBuilder;
import com.elasticpath.commons.exception.EpInvalidValueBindException;
import com.elasticpath.commons.util.security.CreditCardEncrypter;
import com.elasticpath.domain.builder.customer.NewInstanceCustomerCreditCardBuilder;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.impl.CustomerCreditCardImpl;

/**
 * Tests the {@link CreditCardDTOTransformer}.
 */
public class CreditCardDTOTransformerTest {
	private static final String TEST_CARD_TYPE = "testCardType";
	private static final String TEST_EXPIRY_YEAR = "testExpiryYear";
	private static final String TEST_EXPIRY_MONTH = "testExpiryMonth";
	private static final String TEST_GUID = "testGuid";
	private static final String TEST_CARD_HOLDER_NAME = "testCardHolderName";
	private static final String TEST_CARD_NUMBER = "testCardNumber";
	private static final String TEST_START_YEAR = "testStartYear";
	private static final String TEST_START_MONTH = "testStartMonth";
	private static final int TEST_ISSUE_NUMBER = 0;

	private CreditCardDTOTransformer creditCardDTOTransformer;

	/** The context. */
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private CreditCardEncrypter creditCardEncrypter;

	/**
	 * Set up common test elements.
	 */
	@Before
	public void setUpCommonTestElements() {
		creditCardEncrypter = context.mock(CreditCardEncrypter.class);
		context.checking(new Expectations() {
			{
				allowing(creditCardEncrypter).encrypt(TEST_CARD_NUMBER);
				will(returnValue(TEST_CARD_NUMBER));
			}
		});

		creditCardDTOTransformer = new CreditCardDTOTransformer();
		creditCardDTOTransformer.setCustomerCreditCardBuilder(new NewInstanceCustomerCreditCardBuilder());
		creditCardDTOTransformer.setCreditCardDTOBuilder(new CreditCardDTOBuilder());
		creditCardDTOTransformer.setCreditCardEncrypter(creditCardEncrypter);
	}

	/**
	 * Ensure correct transformation to domain.
	 */
	@Test
	public void ensureCorrectTransformationToDomain() {
		CustomerCreditCard expectedCustomerCreditCard = createDefaultCreditCard();
		CustomerCreditCard actualCustomerCreditCard = creditCardDTOTransformer.transformToDomain(createDefaultCreditCardDTO());
		assertTrue("The transformed DTO fields should match those on the expected domain entity", 
				expectedCustomerCreditCard.reflectiveEquals(actualCustomerCreditCard));
	}

	/**
	 * Ensure correct transformation to dto.
	 */
	@Test
	public void ensureCorrectTransformationToDTO() {
		CreditCardDTO expectedCreditCardDTO = createDefaultCreditCardDTO();
		CreditCardDTO actualCreditCardDto = creditCardDTOTransformer.transformToDto(createDefaultCreditCard());
		assertEquals("The transformed domain entity fields should be the expected DTO", expectedCreditCardDTO, actualCreditCardDto);
	}

	/**
	 * Ensure correct transformation to dto when card fields are uninitialized.
	 */
	@Test
	public void ensureCorrectTransformationToDTOWhenCardFieldsAreMinimallyInitialized() {
		CustomerCreditCardImpl creditCard = new CustomerCreditCardImpl();
		
		CreditCardDTO actualDTO = creditCardDTOTransformer.transformToDto(creditCard);
		CreditCardDTO expectedDTO = new CreditCardDTO();
		assertThat("The transformed domain entity fields should be the expected DTO", actualDTO, equalTo(expectedDTO));
	}

	/**
	 * Ensure correct transformation to domain when dto fields are minimally initialized.
	 */
	@Test
	public void ensureCorrectTransformationToDomainWhenDTOFieldsAreMinimallyInitialized() {
		CreditCardDTO cardDTO = new CreditCardDTO();

		cardDTO.setCardNumber(TEST_CARD_NUMBER);
		
		CustomerCreditCard expectedCustomerCreditCard = new CustomerCreditCardImpl();
		expectedCustomerCreditCard.setCardNumber(TEST_CARD_NUMBER);
		
		CustomerCreditCard actualCustomerCreditCard = creditCardDTOTransformer.transformToDomain(cardDTO);
		assertTrue("The transformed DTO fields should match those on the expected domain entity", 
				expectedCustomerCreditCard.reflectiveEquals(actualCustomerCreditCard));
	}
	
	/**
	 * Ensure bind exception thrown on empty credit card number.
	 */
	@Test(expected = EpInvalidValueBindException.class)
	public void ensureTransformToDomainThrowsExceptionWhenCardNumberNull() {
		CreditCardDTO cardDTO = new CreditCardDTO();
		cardDTO.setCardNumber(null);
		creditCardDTOTransformer.transformToDomain(cardDTO);
	}
	
	private CustomerCreditCard createDefaultCreditCard() {
		return new NewInstanceCustomerCreditCardBuilder()
				.withGuid(TEST_GUID)
				.withCardHolderName(TEST_CARD_HOLDER_NAME)
				.withCardNumber(TEST_CARD_NUMBER)
				.withCardType(TEST_CARD_TYPE)
				.withExpiryMonth(TEST_EXPIRY_MONTH)
				.withExpiryYear(TEST_EXPIRY_YEAR)
				.withStartMonth(TEST_START_MONTH)
				.withStartYear(TEST_START_YEAR)
				.withIssueNumber(TEST_ISSUE_NUMBER)
				.build();
	}

	private CreditCardDTO createDefaultCreditCardDTO() {
		return new CreditCardDTOBuilder()
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
	}
}
