/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.importexport.common.marshalling;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.elasticpath.common.dto.customer.CustomerDTO;
import com.elasticpath.common.dto.customer.LegacyCreditCardDTO;
import com.elasticpath.importexport.common.factory.TestCustomerDTOBuilderFactory;
import com.elasticpath.importexport.common.factory.TestPaymentDTOBuilderFactory;

/**
 * Test XML unmarshalling of a customer.
 */
public class CustomerUnmarshallingTest {
	private static final String TEST_PAYMENT_TOKEN_VALUE = "testPaymentTokenValue";
	private static final String TEST_IDENTITY = "testIdentity";

	private final XMLUnmarshaller xmlUnmarshaller = new XMLUnmarshaller(CustomerDTO.class);
	private final TestCustomerDTOBuilderFactory testCustomerDTOBuilderFactory = new TestCustomerDTOBuilderFactory();
	private final TestPaymentDTOBuilderFactory testPaymentDTOBuilderFactory = new TestPaymentDTOBuilderFactory();

	/**
	 * Ensures that all currently supported XML elements are unmarshalled.
	 */
	@Test
	public void ensureCompleteXmlIsUnmarshalledIntoDto() {
		Object outputDto = xmlUnmarshaller.unmarshall(
				getClass().getClassLoader().getResourceAsStream("customers/testCompleteCustomerXmlRepresentation.xml"));

		assertTrue("The unmarshalled dto should be an instance of a CustomerDto", outputDto instanceof CustomerDTO);
		CustomerDTO customerDto = (CustomerDTO) outputDto;

		LegacyCreditCardDTO creditCard = testPaymentDTOBuilderFactory.createLegacyWithIdentity(TEST_IDENTITY).build();
		creditCard.setDefaultCard(true);
		CustomerDTO expectedCustomerDto = testCustomerDTOBuilderFactory
				.createWithPaymentMethods()
				.withCreditCards(creditCard)
				.build();

		assertEquals("The unmarshalled customer dto should be the same as expected",
				expectedCustomerDto,
				customerDto);

		/* testCompleteCustomerXmlRepresentation.xml doesn't contain <first_time_buyer> element,
		   so the unmarshalled value for CustomerDto.firstTimeBuyer field must be false
		 */
		assertFalse("First time buyer field must be false when xml element is missing", customerDto.isFirstTimeBuyer());
	}

	/**
	 * Ensures that if extra default payment methods are provided, they are ignored by the unmarshaller.
	 */
	@Test
	public void ensureUnmarshallingIgnoresExtraDefaultPaymentMethods() {
		Object outputDto = xmlUnmarshaller.unmarshall(getClass().getClassLoader()
				.getResourceAsStream("customers/testCustomerXmlRepresentationWithMultipleDefaultPaymentMethods.xml"));

		assertTrue("The unmarshalled dto should be an instance of a CustomerDto", outputDto instanceof CustomerDTO);
		CustomerDTO customerDto = (CustomerDTO) outputDto;

		CustomerDTO expectedCustomerDto = testCustomerDTOBuilderFactory
				.createWithPaymentMethods()
				.withDefaultPaymentMethod(testPaymentDTOBuilderFactory.createPaymentTokenWithValue(TEST_PAYMENT_TOKEN_VALUE)
						.build())
				.build();
		assertEquals("The unmarshalled customer dto should be the same as expected",
				expectedCustomerDto,
				customerDto);
	}
}
