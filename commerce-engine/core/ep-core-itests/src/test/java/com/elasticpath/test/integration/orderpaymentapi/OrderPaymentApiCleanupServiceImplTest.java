/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.orderpaymentapi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Collection;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.test.db.DbTestCase;

public class OrderPaymentApiCleanupServiceImplTest extends DbTestCase {

	@Autowired
	private OrderPaymentApiCleanupService testee;

	@Autowired
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	@Autowired
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Test
	public void removeByCustomer() {
		final Customer customer = persistCustomer();
		final CustomerPaymentInstrument customerPaymentInstrument =
				persisterFactory.getPaymentInstrumentPersister().persistPaymentInstrument(customer, customer.getPreferredBillingAddress());

		testee.removeByCustomer(customer);

		assertNull("Customer payment instrument was not cleaned",
				customerPaymentInstrumentService.findByGuid(customerPaymentInstrument.getGuid()));
	}

	@Test
	public void removeByOrder() {
		final Order order = persistOrder();
		Collection<OrderPaymentInstrument> orderPaymentInstruments = orderPaymentInstrumentService.findByOrder(order);
		assertFalse(orderPaymentInstruments.isEmpty());

		testee.removeByOrder(order);

		for (OrderPaymentInstrument orderPaymentInstrument : orderPaymentInstruments) {
			assertNull("Order payment instrument was not cleaned",
					orderPaymentInstrumentService.findByGuid(orderPaymentInstrument.getGuid()));
		}
	}

	private Customer persistCustomer() {
		Store store = scenario.getStore();
		return persisterFactory.getStoreTestPersister().createDefaultCustomer(store);
	}

	private Order persistOrder() {
		Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(
				scenario.getCatalog(), scenario.getCategory(), scenario.getWarehouse());

		return persisterFactory.getOrderTestPersister().createOrderWithSkus(
				scenario.getStore(), product.getDefaultSku());
	}
}
