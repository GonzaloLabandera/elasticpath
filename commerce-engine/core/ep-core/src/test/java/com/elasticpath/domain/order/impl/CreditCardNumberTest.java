/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.order.impl;

import static org.junit.Assert.assertEquals;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.util.security.CreditCardEncrypter;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

/**
 * Test cases for <code>CreditCardNumber</code>.
 */
public class CreditCardNumberTest {
	private static final String UNENCRYPTED_CARD_NUMBER = "4012888888881881";
	private static final String ENCRYPTED_CARD_NUMBER = "encrypted number";
	private static final String MASKED_CARD_NUMBER = "************1881";

	private CreditCardNumber creditCardNumber;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private BeanFactory beanFactory;
	private BeanFactoryExpectationsFactory expectationsFactory;

	/**
	 * Prepare for the tests.
	 */
	@Before
	public void setUp() {
		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);

		// Mock out the underlying encrypter
		expectationsFactory.allowingBeanFactoryGetBean("creditCardEncrypter",
				new CreditCardEncrypter() {

					@Override
					public String mask(final String objectToMask) {
						return MASKED_CARD_NUMBER;
					}

					@Override
					public String encrypt(final String unencryptedString) {
						return ENCRYPTED_CARD_NUMBER;
					}

					@Override
					public String decrypt(final String encryptedString) {
						return UNENCRYPTED_CARD_NUMBER;
					}

					@Override
					public String decryptAndMask(final String encryptedCreditCardNumber) {
						return null;
					}
				});

		creditCardNumber = new CreditCardNumber();
	}

	@After
	public void tearDown() {
		expectationsFactory.close();
	}

	@Test
	public void testGetEncryptedBySettingUnencrypted() {
		creditCardNumber.setFullCardNumber(UNENCRYPTED_CARD_NUMBER);
		assertEquals(ENCRYPTED_CARD_NUMBER, creditCardNumber.getEncryptedCardNumber());
	}

	@Test
	public void testGetMaskedCardNumberWithEncrypted() {
		// First, set the encrypted card number
		creditCardNumber.setFullCardNumber(UNENCRYPTED_CARD_NUMBER);
		assertEquals(MASKED_CARD_NUMBER, creditCardNumber.getMaskedCardNumber());
	}
}
