/*
 * Copyright © 2013 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentmethods.integration.epcommerce.transform;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.PaymentToken;
import com.elasticpath.domain.customer.impl.PaymentTokenImpl;
import com.elasticpath.rest.ResourceTypeFactory;
import com.elasticpath.rest.definition.paymentmethods.CreditCardEntity;
import com.elasticpath.rest.definition.paymentmethods.PaymentMethodEntity;
import com.elasticpath.rest.definition.paymenttokens.PaymentTokenEntity;
import com.elasticpath.rest.resource.integration.epcommerce.repository.customer.paymentmethods.PaymentMethodIdentifierResolver;

/**
 * The test of {@link PaymentMethodTransformer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PaymentMethodTransformerTest {

	private static final Integer EXPECTED_ISSUE_NUMBER = 7;
	private static final String EXPECTED_START_YEAR = "start year";
	private static final String EXPECTED_START_MONTH = "start month";
	private static final String EXPECTED_EXPIRY_YEAR = "next year";
	private static final String EXPECTED_EXPIRY_MONTH = "next month";
	private static final String EXPECTED_TYPE = "Discover";
	private static final String EXPECTED_NUMBER = "314159";
	private static final String EXPECTED_NAME = "Joe Schmoe";
	private static final String EXPECTED_IDENTIFIER = "expectedIdentifier";
	private static final String TEST_DISPLAY_VALUE = "testDisplayValue";
	private static final String TEST_VALUE = "testValue";

	@Mock
	private PaymentMethodIdentifierResolver paymentMethodIdentifierResolver;
	@InjectMocks
	private PaymentMethodTransformer transformer;

	@Test(expected = UnsupportedOperationException.class)
	public void ensureTransformToDomainIsUnsupported() {
		transformer.transformToDomain(null);
	}

	@Test
	public void ensureCorrectTransformOfCustomerCreditCardToEntity() {
		final CustomerCreditCard mockCustomerCreditCard = Mockito.mock(CustomerCreditCard.class);
		when(mockCustomerCreditCard.getCardHolderName()).thenReturn(EXPECTED_NAME);
		when(mockCustomerCreditCard.getMaskedCardNumber()).thenReturn(EXPECTED_NUMBER);
		when(mockCustomerCreditCard.getCardType()).thenReturn(EXPECTED_TYPE);
		when(mockCustomerCreditCard.getExpiryMonth()).thenReturn(EXPECTED_EXPIRY_MONTH);
		when(mockCustomerCreditCard.getExpiryYear()).thenReturn(EXPECTED_EXPIRY_YEAR);
		when(mockCustomerCreditCard.getStartMonth()).thenReturn(EXPECTED_START_MONTH);
		when(mockCustomerCreditCard.getStartYear()).thenReturn(EXPECTED_START_YEAR);
		when(mockCustomerCreditCard.getIssueNumber()).thenReturn(EXPECTED_ISSUE_NUMBER);
		when(paymentMethodIdentifierResolver.getIdentifierForPaymentMethod(mockCustomerCreditCard)).thenReturn(EXPECTED_IDENTIFIER);

		PaymentMethodEntity paymentMethodEntity = transformer.transformToEntity(mockCustomerCreditCard);
		CreditCardEntity creditCardDto = ResourceTypeFactory.adaptResourceEntity(paymentMethodEntity, CreditCardEntity.class);

		assertEquals("Card Guid does not match", EXPECTED_IDENTIFIER, creditCardDto.getPaymentMethodId());
		assertEquals("Card name doesn't match", EXPECTED_NAME, creditCardDto.getCardholderName());
		assertEquals("Card number doesn't match", EXPECTED_NUMBER, creditCardDto.getCardNumber());
		assertEquals("Card type doesn't match", EXPECTED_TYPE, creditCardDto.getCardType());
		assertEquals("Card expiry month doesn't match", EXPECTED_EXPIRY_MONTH, creditCardDto.getExpiryMonth());
		assertEquals("Card expiry year doesn't match", EXPECTED_EXPIRY_YEAR, creditCardDto.getExpiryYear());
		assertEquals("Card start month doesn't match", EXPECTED_START_MONTH, creditCardDto.getStartMonth());
		assertEquals("Card start year doesn't match", EXPECTED_START_YEAR, creditCardDto.getStartYear());
		assertEquals("Card issue number doesn't match", EXPECTED_ISSUE_NUMBER, creditCardDto.getIssueNumber());
	}

	@Test
	public void ensureCorrectTransformOfPaymentTokenToEntity() {
		final PaymentToken paymentToken = new PaymentTokenImpl.TokenBuilder()
					.withDisplayValue(TEST_DISPLAY_VALUE)
					.withValue(TEST_VALUE)
					.build();

		when(paymentMethodIdentifierResolver.getIdentifierForPaymentMethod(paymentToken)).thenReturn(EXPECTED_IDENTIFIER);
		PaymentMethodEntity paymentMethodEntity = transformer.transformToEntity(paymentToken);

		PaymentTokenEntity paymentTokenEntity = ResourceTypeFactory.adaptResourceEntity(paymentMethodEntity, PaymentTokenEntity.class);

		assertEquals("The payment token display value should be the same as expected", TEST_DISPLAY_VALUE, paymentTokenEntity.getDisplayName());
		assertEquals("The payment token value should be the same as expected", TEST_VALUE, paymentTokenEntity.getToken());
		assertEquals("The payment token id should be the same as expected", EXPECTED_IDENTIFIER,
				paymentTokenEntity.getPaymentMethodId());
	}
}
