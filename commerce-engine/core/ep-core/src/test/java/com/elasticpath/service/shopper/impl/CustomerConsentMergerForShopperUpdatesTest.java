/**
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.service.shopper.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.service.datapolicy.CustomerConsentService;

/**
 * Tests {@link CustomerConsentMergerForShopperUpdates}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerConsentMergerForShopperUpdatesTest {

	private CustomerConsentMergerForShopperUpdates customerConsentMergerForShopperUpdates;

	@Mock
	private CustomerConsentService customerConsentService;

	@Mock
	private CustomerSession customerSession;

	private static final String MYSTERY_MAN = "MysteryMan";
	private static final String REGULAR_JOE = "RegularJoe";
	private static final String REGULAR_JANE = "RegularJane";

	private static final String CUSTOMER = "Customer";

	@Before
	public void setUp() {
		customerConsentMergerForShopperUpdates = new CustomerConsentMergerForShopperUpdates(customerConsentService);
	}

	@Test
	public void testCustomerConsentMergeWhenTransitioningFromAnonymousToRegistered() {
		final String anonymousCustomerGuid = MYSTERY_MAN + CUSTOMER;
		Shopper anonymousShopper = createShopper(anonymousCustomerGuid, CustomerType.SINGLE_SESSION_USER);

		Shopper registeredShopper = createRegisteredCustomerWithConsents();

		when(customerSession.getShopper()).thenReturn(registeredShopper);

		customerConsentMergerForShopperUpdates.invalidateShopper(customerSession, anonymousShopper);

		verify(customerConsentService).updateCustomerGuids(anonymousCustomerGuid, registeredShopper.getCustomer().getGuid());
	}

	@Test
	public void testCustomerConsentMergeWhenTransitioningFromAnonymousToRegisteredWithoutExistingConsents() {
		final String anonymousCustomerGuid = MYSTERY_MAN + CUSTOMER;
		Shopper anonymousShopper = createShopper(anonymousCustomerGuid, CustomerType.SINGLE_SESSION_USER);

		Shopper registeredShopper = createRegisteredCustomerWithoutConsents();

		when(customerSession.getShopper()).thenReturn(registeredShopper);

		customerConsentMergerForShopperUpdates.invalidateShopper(customerSession, anonymousShopper);

		verify(customerConsentService).updateCustomerGuids(anonymousCustomerGuid, registeredShopper.getCustomer().getGuid());
	}

	@Test
	public void testNoCustomerConsentMergeWhenInvalidShopperHasNoCustomerConsents() {
		final String anonymousCustomerGuid = MYSTERY_MAN + CUSTOMER;
		Shopper anonymousShopper = createShopper(anonymousCustomerGuid, CustomerType.SINGLE_SESSION_USER);

		Shopper registeredShopper = createRegisteredCustomerWithConsents();
		when(customerSession.getShopper()).thenReturn(registeredShopper);

		customerConsentMergerForShopperUpdates.invalidateShopper(customerSession, anonymousShopper);

		verify(customerConsentService).updateCustomerGuids(anonymousCustomerGuid, registeredShopper.getCustomer().getGuid());
	}

	@Test
	public void testNoCustomerConsentMergeWhenSwithingFromRegisteredToRegisteredCustomer() {
		final String originalRegisteredCustomerGuid = REGULAR_JANE + CUSTOMER;
		Shopper anonymousShopper = createShopper(originalRegisteredCustomerGuid, CustomerType.REGISTERED_USER);

		Shopper registeredShopper = createRegisteredCustomerWithConsents();

		customerConsentMergerForShopperUpdates.invalidateShopper(customerSession, anonymousShopper);

		verify(customerConsentService, never()).updateCustomerGuids(originalRegisteredCustomerGuid, registeredShopper.getCustomer().getGuid());
	}


	private Shopper createShopper(final String customerGuid, final CustomerType customerType) {
		final Shopper shopper = mock(Shopper.class, customerGuid + "Shopper");
		final Customer customer = mock(Customer.class, customerGuid + CUSTOMER);

		when(shopper.getCustomer()).thenReturn(customer);
		when(customer.getCustomerType()).thenReturn(customerType);
		when(customer.getGuid()).thenReturn(customerGuid);

		return shopper;
	}

	private Shopper createRegisteredCustomerWithConsents() {
		final String registeredCustomerGuid = REGULAR_JOE + CUSTOMER;
		return createShopper(registeredCustomerGuid, CustomerType.REGISTERED_USER);
	}

	private Shopper createRegisteredCustomerWithoutConsents() {
		final String registeredCustomerGuid = REGULAR_JOE + CUSTOMER;
		return createShopper(registeredCustomerGuid, CustomerType.REGISTERED_USER);
	}

}
