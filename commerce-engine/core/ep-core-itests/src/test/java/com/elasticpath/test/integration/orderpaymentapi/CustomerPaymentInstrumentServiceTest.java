/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.test.integration.orderpaymentapi;

import static com.elasticpath.commons.constants.ContextIdNames.CUSTOMER_PAYMENT_INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.orderpaymentapi.CustomerDefaultPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.util.Utils;

/**
 * Test for {@link CustomerPaymentInstrumentService}.
 */
public class CustomerPaymentInstrumentServiceTest extends DbTestCase {

	private static String paymentInstrumentGuid;

	@Autowired
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	@Autowired
	private CustomerDefaultPaymentInstrumentService customerDefaultPaymentInstrumentService;

	@Autowired
	private CustomerService customerService;

	@Before
	public void setUp() {
		paymentInstrumentGuid = Utils.uniqueCode("PAYMENTINSTRUMENT");
	}

	@Test
	@DirtiesDatabase
	public void ensureFindByGuidFindsCustomerPaymentInstrument() {
        CustomerPaymentInstrument entity = createTestEntity();
        Customer customer = createCustomer();
        entity.setCustomerUid(customer.getUidPk());

        customerPaymentInstrumentService.saveOrUpdate(entity);

        CustomerPaymentInstrument persistedInstrument = customerPaymentInstrumentService.findByGuid(entity.getGuid());
        assertEquals("Wrong CustomerPaymentInstrument found by GUID", entity, persistedInstrument);
    }

	@Test
	@DirtiesDatabase
	public void ensureFindByCustomerFindsCustomerPaymentInstruments() {
        CustomerPaymentInstrument entity = createTestEntity();
        Customer customer = createCustomer();
        entity.setCustomerUid(customer.getUidPk());

        customerPaymentInstrumentService.saveOrUpdate(entity);

        Collection<CustomerPaymentInstrument> instruments = customerPaymentInstrumentService.findByCustomer(customer);
        Iterator<CustomerPaymentInstrument> iterator = instruments.iterator();
        assertTrue("No CustomerPaymentInstrument entities were found for this Customer", iterator.hasNext());
        assertEquals("Wrong CustomerPaymentInstrument associated with the Customer", entity.getUidPk(), iterator.next().getUidPk());
    }

	@Test
	@DirtiesDatabase
	public void removingCustomerPaymentInstrumentDoesNotRemoveCustomer() {
        Customer customer = createCustomer();
        CustomerPaymentInstrument entity = createTestEntity();
        entity.setCustomerUid(customer.getUidPk());
        customerPaymentInstrumentService.saveOrUpdate(entity);

        customerPaymentInstrumentService.remove(entity);

        final CustomerPaymentInstrument removedInstrument = customerPaymentInstrumentService.findByGuid(entity.getGuid());
        assertNull("CustomerPaymentInstrument was not removed", removedInstrument);
        final Customer persistedCustomer = customerService.findByGuid(customer.getGuid());
        assertNotNull("Customer was unexpectedly removed", persistedCustomer);
    }

	@Test
	@DirtiesDatabase
	public void removingCustomerPaymentInstrumentRemovesCustomerDefaultPaymentInstrumentAsWell() {
        Customer customer = createCustomer();
        CustomerPaymentInstrument entity = createTestEntity();
        entity.setCustomerUid(customer.getUidPk());
        customerPaymentInstrumentService.saveOrUpdate(entity);

        customerDefaultPaymentInstrumentService.saveAsDefault(entity);
        assertTrue("CustomerDefaultPaymentInstrument was not attached to customer",
                customerDefaultPaymentInstrumentService.hasDefaultPaymentInstrument(customer));

        customerPaymentInstrumentService.remove(entity);

        final CustomerPaymentInstrument removedInstrument = customerPaymentInstrumentService.findByGuid(entity.getGuid());
        assertNull("CustomerPaymentInstrument was not removed", removedInstrument);
		final CustomerPaymentInstrument defaultInstrument = customerDefaultPaymentInstrumentService.getDefaultForCustomer(customer);
		assertNull("CustomerDefaultPaymentInstrument was not removed", defaultInstrument);
		assertFalse("CustomerDefaultPaymentInstrument is still attached to customer",
				customerDefaultPaymentInstrumentService.hasDefaultPaymentInstrument(customer));
	}

	private CustomerPaymentInstrument createTestEntity() {
		CustomerPaymentInstrument entity = getBeanFactory().getPrototypeBean(CUSTOMER_PAYMENT_INSTRUMENT, CustomerPaymentInstrument.class);
		entity.setPaymentInstrumentGuid(paymentInstrumentGuid);
		return entity;
	}

	private Customer createCustomer() {
		Store store = scenario.getStore();
		return persisterFactory.getStoreTestPersister().createDefaultCustomer(store);
	}

}
