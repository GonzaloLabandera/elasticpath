/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.orderpaymentapi;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Currency;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.factory.TestCustomerSessionFactoryForTestApplication;
import com.elasticpath.domain.factory.TestShopperFactoryForTestApplication;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.orderpaymentapi.CustomerPaymentInstrument;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentInstrument;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.orderpaymentapi.CartOrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.CustomerPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentApiCleanupService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentInstrumentService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.util.Utils;

public class OrderPaymentApiCleanupServiceImplTest extends DbTestCase {

	@Autowired
	private OrderPaymentApiCleanupService testee;

	@Autowired
	private CustomerPaymentInstrumentService customerPaymentInstrumentService;

	@Autowired
	private OrderPaymentInstrumentService orderPaymentInstrumentService;

	@Autowired
	private CartOrderPaymentInstrumentService cartOrderPaymentInstrumentService;

	@Autowired
	private OrderPaymentService orderPaymentService;

	@Autowired
	private ShopperService shopperService;

	@Autowired
	private ShoppingCartService shoppingCartService;

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

	@Test
	public void removeByOrderUidList() {
		final Order order = persistOrder();
		Collection<OrderPaymentInstrument> orderPaymentInstruments = orderPaymentInstrumentService.findByOrder(order);
		assertFalse(orderPaymentInstruments.isEmpty());
		final Collection<OrderPayment> orderPayments = orderPaymentService.findByOrder(order);
		assertFalse(orderPayments.isEmpty());

		testee.removeByOrderUidList(Collections.singletonList(order.getUidPk()));

		for (OrderPaymentInstrument orderPaymentInstrument : orderPaymentInstruments) {
			assertNull("Order payment instrument was not cleaned",
					orderPaymentInstrumentService.findByGuid(orderPaymentInstrument.getGuid()));
		}
		for (OrderPayment orderPayment : orderPayments) {
			assertNull("Order payment was not cleaned",
					orderPaymentService.findByGuid(orderPayment.getGuid()));
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

	private Shopper persistShopper() {
		Shopper shopper = TestShopperFactoryForTestApplication.getInstance().createNewShopperWithMemento();
		shopper.setCustomer(persistCustomer());
		shopper.setStoreCode(scenario.getStore().getCode());
		return shopperService.save(shopper);
	}

	private ShoppingCart persistShoppingCart(final Shopper shopper) {
		CustomerSession customerSession = TestCustomerSessionFactoryForTestApplication.getInstance().createNewCustomerSessionWithContext(shopper);
		customerSession.setCurrency(Currency.getInstance("USD"));

		ShoppingCartImpl shoppingCart = new ShoppingCartImpl();
		shoppingCart.setShopper(shopper);
		shoppingCart.setStore(scenario.getStore());
		shoppingCart.getShoppingCartMemento().setGuid(Utils.uniqueCode("CART"));
		shoppingCart.setCustomerSession(customerSession);

		shopper.setCurrentShoppingCart(shoppingCart);

		return shoppingCartService.saveOrUpdate(shoppingCart);
	}

}
