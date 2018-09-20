/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.domain.customer.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.builder.customer.NewInstanceCustomerCreditCardBuilder;
import com.elasticpath.domain.customer.CustomerCreditCard;
import com.elasticpath.domain.customer.CustomerPaymentMethods;
import com.elasticpath.plugin.payment.dto.PaymentMethod;

/**
 * Tests {@link CustomerPaymentMethodsImpl}.
 */
public class CustomerPaymentMethodsImplTest {
	private static final Long TEST_UID_PK = 1L;
	private CustomerPaymentMethods customerPaymentMethods;
	private PaymentMethod testCustomerCreditCard;
	private PaymentMethod testCustomerCreditCard2;
	private PaymentMethod testCustomerCreditCard2Equal;

	/**
	 * Setup common test components.
	 */
	@Before
	public void setupTestComponents() {
		testCustomerCreditCard = new NewInstanceCustomerCreditCardBuilder().withCardHolderName("testCardholderName")
				.withGuid("testGuid")
				.build();
		testCustomerCreditCard2 = new NewInstanceCustomerCreditCardBuilder().withCardHolderName("testCardHolderName2")
				.withGuid("testGuid2")
				.build();
		testCustomerCreditCard2Equal = new NewInstanceCustomerCreditCardBuilder().withCardHolderName("testCardHolderName2")
				.withGuid("testGuid2")
				.build();
		CustomerImpl customer = new CustomerImpl();
		customerPaymentMethods = new CustomerPaymentMethodsImpl(customer);
	}

	/**
	 * Test that when adding a list of payment methods, if no default is set, the first payment method of the list is set as the default.
	 */
	@Test
	public void testAddAllEnsuresDefaultIsSet() {
		customerPaymentMethods.addAll(Arrays.<PaymentMethod>asList(testCustomerCreditCard, testCustomerCreditCard2));

		assertEquals("The default payment method should be the first credit card in the list of credit cards added",
				testCustomerCreditCard, customerPaymentMethods.getDefault());
	}

	/**
	 * Test that when adding a payment method, if no default is set, the first payment method added is set as the default.
	 */
	@Test
	public void testAddEnsuresDefaultIsSet() {
		customerPaymentMethods.add(testCustomerCreditCard);

		assertEquals("The default payment method should be the first and only credit card added", testCustomerCreditCard,
				customerPaymentMethods.getDefault());
	}

	/**
	 * Test that when removing a default payment method, the next payment in the list of payment methods is set as the default.
	 */
	@Test
	public void testRemoveEnsuresDefaultIsSet() {
		customerPaymentMethods.addAll(Arrays.<PaymentMethod>asList(testCustomerCreditCard, testCustomerCreditCard2));
		customerPaymentMethods.setDefault(testCustomerCreditCard);
		customerPaymentMethods.remove(testCustomerCreditCard2);

		assertEquals("The default payment method should be the second credit card in the list given the original default was removed",
				testCustomerCreditCard, customerPaymentMethods.getDefault());
	}

	/**
	 * Test removing default payment method removes the default payment method set.
	 */
	@Test
	public void testRemovingDefaultPaymentMethod() {
		customerPaymentMethods.add(testCustomerCreditCard);
		customerPaymentMethods.setDefault(testCustomerCreditCard);
		customerPaymentMethods.remove(testCustomerCreditCard);

		assertNull("There should be no default payment method set", customerPaymentMethods.getDefault());
	}

	/**
	 * Test adding existing default payment method does not duplicate payment method in customer payment methods collection.
	 */
	@Test
	public void testAddingExistingDefaultPaymentMethodDoesNotDuplicatePaymentMethod() {
		customerPaymentMethods.setDefault(testCustomerCreditCard);
		customerPaymentMethods.add(testCustomerCreditCard);

		assertThat("The customer payment methods should only contain the test customer credit card",
				customerPaymentMethods.all(), contains(testCustomerCreditCard));
		assertEquals("The default customer payment method should be the test customer credit card", testCustomerCreditCard,
				customerPaymentMethods.getDefault());
	}

	/**
	 * Test that call to add all does not duplicate payment methods.
	 */
	@Test
	public void ensureAddAllDoesNotAddDuplicatePaymentMethods() {
		List<PaymentMethod> paymentMethods = Arrays.asList(testCustomerCreditCard, testCustomerCreditCard2);
		customerPaymentMethods.addAll(paymentMethods);
		customerPaymentMethods.add(testCustomerCreditCard);

		assertThat("The customer payment methods should only contain the test customer credit card 1 and 2",
				customerPaymentMethods.all(), contains(testCustomerCreditCard, testCustomerCreditCard2));
	}

	/**
	 * Test setting default payment to null throws {@link IllegalArgumentException}.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSetDefaultPaymentMethodCanNotBeSetToNull() {
		customerPaymentMethods.setDefault(null);
	}

	/**
	 * Test calling remove all with a collection of payment methods containing the default and
	 * ensure that the default is set to the first in the list.
	 */
	@Test
	public void testRemoveAllWithDefaultRemovedResetsDefaultToFirstAvailableInList() {
		customerPaymentMethods.addAll(Arrays.<PaymentMethod>asList(testCustomerCreditCard, testCustomerCreditCard2));
		customerPaymentMethods.setDefault(testCustomerCreditCard);
		customerPaymentMethods.removeAll(Arrays.<PaymentMethod>asList(testCustomerCreditCard));

		assertThat("The customer payment methods should only contain the test customer credit card 2",
				customerPaymentMethods.all(), contains(testCustomerCreditCard2));
		assertEquals("The default customer payment method should be the test customer credit card 2", testCustomerCreditCard2,
				customerPaymentMethods.getDefault());
	}

	/**
	 * Test that calling remove all with all the payment methods for a customer will remove the payment methods and set the default payment
	 * method to null.
	 */
	@Test
	public void ensureRemoveAllWithAllElementsRemovedAlsoRemovesDefaultPaymentMethod() {
		List<PaymentMethod> paymentMethods = Arrays.asList(testCustomerCreditCard, testCustomerCreditCard2);
		customerPaymentMethods.addAll(paymentMethods);
		customerPaymentMethods.setDefault(testCustomerCreditCard);
		customerPaymentMethods.removeAll(paymentMethods);

		assertThat("The customer payment methods should be empty",
				customerPaymentMethods.all(), empty());
		assertNull("The customer's default payment method should be null", customerPaymentMethods.getDefault());
	}

	/**
	 * Test that calling clear removes all payment methods including the default payment method.
	 */
	@Test
	public void ensureClearRemovesAllPaymentMethodsIncludingDefaultPaymentMethod() {
		List<PaymentMethod> paymentMethods = Arrays.asList(testCustomerCreditCard, testCustomerCreditCard2);
		customerPaymentMethods.addAll(paymentMethods);
		customerPaymentMethods.clear();

		assertThat("The customer payment methods should be empty",
				customerPaymentMethods.all(), empty());
		assertNull("The customer's default payment method should be null", customerPaymentMethods.getDefault());
	}

	/**
	 * Ensures that the default cannot be set to a reference that doesn't exist in the payment method collection.
	 */
	@Test
	public void ensureSetDefaultCoalescesReference() {
		customerPaymentMethods.add(testCustomerCreditCard2);
		customerPaymentMethods.setDefault(testCustomerCreditCard2Equal);
		assertThat("Default references is same as the one in the collection", customerPaymentMethods.getDefault(),
				sameInstance(customerPaymentMethods.all().iterator().next()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void ensureResolveThrowsExceptionForNullPaymentMethod() {
		customerPaymentMethods.resolve(null);
	}

	@Test
	public void ensureResolveReturnsNullForPaymentMethodWhichDoesNotMatch() {
		customerPaymentMethods.add(new PaymentMethod() { });
		assertNull("resolved payment method was not null", customerPaymentMethods.resolve(new PaymentMethod() { }));
	}

	@Test
	public void ensureResolveReturnsMatchingPaymentMethod() {
		PaymentMethod paymentMethod = new EqualToEverythingPaymentMethod();
		customerPaymentMethods.add(paymentMethod);

		PaymentMethod equalCreditCard = new EqualToEverythingPaymentMethod();

		assertSame("resolved payment method was not the instance added to customer payment methods",
				paymentMethod, customerPaymentMethods.resolve(equalCreditCard));
	}

	@Test
	public void ensureGetByUidPkForValidUidPkReturnsPaymentMethod() {
		CustomerCreditCard paymentMethod = new CustomerCreditCardImpl();
		paymentMethod.setUidPk(TEST_UID_PK);

		customerPaymentMethods.add(paymentMethod);

		PaymentMethod retrievedPaymentMethod = customerPaymentMethods.getByUidPk(TEST_UID_PK);

		assertSame("The payment method found for the uid pk should be the same as the one added", paymentMethod, retrievedPaymentMethod);
	}

	@Test
	public void ensureGetByUidPkReturnsNullIfPaymentMethodNotFound() {
		PaymentMethod retrievedPaymentMethod = customerPaymentMethods.getByUidPk(TEST_UID_PK);

		assertNull("No payment method should have been found", retrievedPaymentMethod);
	}

	private static class EqualToEverythingPaymentMethod implements PaymentMethod {
		@Override
		public boolean equals(final Object obj) {
			return obj instanceof EqualToEverythingPaymentMethod;
		}

		@Override
		public int hashCode() {
			return 0;
		}
	}
}
