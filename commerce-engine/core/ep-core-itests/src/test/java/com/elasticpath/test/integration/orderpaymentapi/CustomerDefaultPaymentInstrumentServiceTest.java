/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */

package com.elasticpath.test.integration.orderpaymentapi;

import static com.elasticpath.commons.constants.ContextIdNames.CUSTOMER_PAYMENT_INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.orderpaymentapi.CustomerDefaultPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Test for {@link CustomerDefaultPaymentInstrumentService}.
 */
public class CustomerDefaultPaymentInstrumentServiceTest extends DbTestCase {

	@Autowired
	private CustomerDefaultPaymentInstrumentService testee;

	@Autowired
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	@Test
	public void ensureIsDefaultIsFalseWhenThereAreNoCustomerPaymentInstruments() {
		assertFalse("There must not be any default CustomerPaymentInstruments", testee.isDefault(null));
	}

	@Test
	public void ensureIsDefaultIsFalseWhenThereAreNoCustomerDefaultPaymentInstruments() {
		Customer customer = createCustomer();
		CustomerPaymentInstrument customerPaymentInstrument = createTestEntity(customer);
		customerPaymentInstrumentService.saveOrUpdate(customerPaymentInstrument);

		assertFalse("There must not be any default CustomerPaymentInstruments", testee.isDefault(customerPaymentInstrument));
	}

	@Test
	@DirtiesDatabase
	public void ensureSaveAsDefaultAssociatesCustomerPaymentInstrumentWithCustomer() {
		Customer customer = createCustomer();
		CustomerPaymentInstrument customerPaymentInstrument = createTestEntity(customer);
		customerPaymentInstrumentService.saveOrUpdate(customerPaymentInstrument);

		testee.saveAsDefault(customerPaymentInstrument);

		CustomerPaymentInstrument associatedInstrument = testee.getDefaultForCustomer(customer);
		assertEquals("Wrong CustomerPaymentInstrument associated with the customer", customerPaymentInstrument, associatedInstrument);
		assertTrue("CustomerPaymentInstrument must be set as default one", testee.isDefault(customerPaymentInstrument));
	}

	@Test
	@DirtiesDatabase
	public void ensureSaveAsDefaultRemovesPreviousAssociationInFavorOfNewOneAndDoesNotRemoveLinkedEntities() {
		Customer customer = createCustomer();
		CustomerPaymentInstrument instrument1 = createTestEntity(customer);
		customerPaymentInstrumentService.saveOrUpdate(instrument1);
		CustomerPaymentInstrument instrument2 = createTestEntity(customer);
		customerPaymentInstrumentService.saveOrUpdate(instrument2);

		testee.saveAsDefault(instrument1);
		testee.saveAsDefault(instrument2);

		CustomerPaymentInstrument associatedInstrument = testee.getDefaultForCustomer(customer);
		assertEquals("Wrong CustomerPaymentInstrument associated with the customer", instrument2, associatedInstrument);
		CustomerPaymentInstrument persistedInstrument = customerPaymentInstrumentService.findByGuid(instrument1.getGuid());
		assertEquals("CustomerPaymentInstrument was unexpectedly removed", instrument1, persistedInstrument);
	}

	private CustomerPaymentInstrument createTestEntity(final Customer customer) {
        CustomerPaymentInstrument entity = getBeanFactory().getPrototypeBean(CUSTOMER_PAYMENT_INSTRUMENT, CustomerPaymentInstrument.class);
        entity.setCustomerUid(customer.getUidPk());
        entity.setPaymentInstrumentGuid(Utils.uniqueCode("PAYMENTINSTRUMENT"));
        return entity;
    }

	private Customer createCustomer() {
		Store store = scenario.getStore();
		return persisterFactory.getStoreTestPersister().createDefaultCustomer(store);
	}

}
