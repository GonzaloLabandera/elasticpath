/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.factory.TestCustomerSessionFactoryForTestApplication;
import com.elasticpath.domain.factory.TestShopperFactoryForTestApplication;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionCleanupService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.store.StoreService;
import com.elasticpath.test.db.DbTestCase;

/**
 * An integration test for the CustomerSessionServiceImpl, we are testing from a client's point of view with Spring and the Database up and running.
 */
public class CustomerSessionServiceImplTest extends DbTestCase {

	/** The main object under test. */
	@Autowired
	private CustomerSessionService customerSessionService;

	@Autowired
	private CustomerSessionCleanupService customerSessionCleanupService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private StoreService storeService;

	@Autowired
	private ShopperService shopperService;

	private CustomerSession createCustomerSessionWithTimestamp(final Date timeStamp, final Store store) {

		final Shopper shopper = TestShopperFactoryForTestApplication.getInstance().createNewShopperWithMemento();
		shopper.setStoreCode(store.getCode());
		shopperService.save(shopper);

		final CustomerSession session = TestCustomerSessionFactoryForTestApplication.getInstance().createNewCustomerSessionWithContext(shopper);
		session.setCreationDate(timeStamp);
		session.setCurrency(Currency.getInstance(Locale.CANADA));
		session.setLastAccessedDate(timeStamp);
		session.setLocale(Locale.US);

		return session;
	}

	private CustomerSession createCustomerSession(final Store store) {
		return createCustomerSessionWithTimestamp(Calendar.getInstance().getTime(), store);
	}

	/**
	 * Tests to see that {@link CustomerSessionCleanupService#checkPersistedCustomerSessionGuidExists} works.
	 */
	@Test
	public void testCheckPersistedCustomerSessionGuidExists() {

		final CustomerSession session = createCustomerSession(scenario.getStore());
		assertFalse(customerSessionCleanupService.checkPersistedCustomerSessionGuidExists(session.getGuid()));

		customerSessionService.add(session);
		assertTrue(customerSessionCleanupService.checkPersistedCustomerSessionGuidExists(session.getGuid()));
	}

	/**
	 * Tests to see if a {@link CustomerSession} can be created and then read from the database.
	 */
	@Test
	public void testBasicCreateAndReadCustomerSession() {
		final CustomerSession customerSession = createCustomerSession(scenario.getStore());
		customerSessionService.add(customerSession);

		final CustomerSession readCustomerSession = customerSessionService.findByGuid(customerSession.getGuid());
		assertEquals(customerSession.getGuid(), readCustomerSession.getGuid());
	}
}
