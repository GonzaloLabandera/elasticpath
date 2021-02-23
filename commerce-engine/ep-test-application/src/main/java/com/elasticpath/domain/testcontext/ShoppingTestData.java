/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.testcontext;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.impl.CustomerSessionImpl;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.persister.TestApplicationContext;

/**
 * This class holds Shopping specific data required to persist 
 * between fixtures for the duration of a test.
 * 
 * Do not add elements to this class for completely different test
 * flows, create a new one that is appropriate to the set of tests.
 * 
 * NOTE: The previous approach to this was for each fixture to hold its own
 * references which allowed for them to get out of sync very easily and
 * encouraged the coupling of fixtures to other fixtures.  Any changes
 * to the way certain elements were constructed caused large ripple effects
 * throughout the fixtures.
 */
public class ShoppingTestData {

	private static ShoppingTestData instance = new ShoppingTestData();

	private ShoppingCartService shoppingCartService;
	private OrderService orderService;

	private TestCustomerSession customerSession;

	/* Wish there was another way, but time is short and some of our tests need a second CustomerSession. */
	private TestCustomerSession secondaryCustomerSession;

	private Store store;
	private Order order;

	private ShoppingTestData() {
	}

	public static ShoppingTestData getInstance() {
		return instance;
	}

	public CustomerSession getCustomerSession() {
		return customerSession.getRealCustomerSession();
	}

	public void setCustomerSession(CustomerSession customerSession) {
		this.customerSession = new TestCustomerSessionImpl(customerSession, shoppingCartService);
	}

	public Store getStore() {
		return store;
	}

	public void setStore(Store store) {
		this.store = store;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Order getOrder() {
		return order;
	}

	/**
	 * Reset the data.
	 */
	public static void reset() {
		instance = new ShoppingTestData();
	}

    /**
     * @return the secondaryCustomerSession
     */
    public CustomerSession getSecondaryCustomerSession() {
        return secondaryCustomerSession.getRealCustomerSession();
    }

    /**
     * @param secondaryCustomerSession the secondaryCustomerSession to set
     */
    public void setSecondaryCustomerSession(final CustomerSession secondaryCustomerSession) {
        this.secondaryCustomerSession = new TestCustomerSessionImpl(secondaryCustomerSession, shoppingCartService);
    }

	/**
	 * Initialize {@link ShoppingCartService} and {@link OrderService} services.
	 *
	 * @param testApplicationContext the test application context
	 */
	public void initServices(final TestApplicationContext testApplicationContext) {

    	if (this.shoppingCartService == null) {
			this.shoppingCartService = testApplicationContext.getBeanFactory().getSingletonBean(ContextIdNames.SHOPPING_CART_SERVICE,
					ShoppingCartService.class);
		}

    	if (this.orderService == null) {
			this.orderService = testApplicationContext.getBeanFactory().getSingletonBean(ContextIdNames.ORDER_SERVICE,
					OrderService.class);
		}
	}

	public Order getCompletedOrder() {
		return getCompletedOrder(customerSession);
	}

	public Order getSecondaryCompletedOrder() {
		return getCompletedOrder(secondaryCustomerSession);
	}

	//fetching completed order via OrderService, instead relying on volatile shopping cart, prevents intermittent failures
	// (shipment item subtotal can be null due to cached/non-initialized value)
	private Order getCompletedOrder(final TestCustomerSession customerSession) {
		return orderService.findOrderByOrderNumber(customerSession.getCompletedOrderNumber());
	}

	private interface TestCustomerSession extends CustomerSession {
		String getCompletedOrderNumber();
		void setCompletedOrderNumber(String completedOrderNumber);

		CustomerSession getRealCustomerSession();
	}

	private static class TestCustomerSessionImpl extends CustomerSessionImpl implements TestCustomerSession {
		/**
		 * Serial version id.
		 */
		private static final long serialVersionUID = 6000000001L;

		private final CustomerSession realCustomerSession;
		private final ShoppingCartService shoppingCartService;

		private String completedOrderNumber;

		public TestCustomerSessionImpl(final CustomerSession customerSession, final ShoppingCartService shoppingCartService) {
			this.realCustomerSession = customerSession;
			this.shoppingCartService = shoppingCartService;
		}

		@Override
		public String getCompletedOrderNumber() {
			refreshCartIfRequired();
			return completedOrderNumber;
		}

		@Override
		public void setCompletedOrderNumber(final String completedOrderNumber) {
			this.completedOrderNumber = completedOrderNumber;
		}

		@Override
		public CustomerSession getRealCustomerSession() {
			return refreshCartIfRequired();
		}

		/*
	  The cart must be refreshed upon checkout. Also, completed order's order number must be preserved as well
	  so the fresh completed order is always retrieved.
	 */
		private CustomerSession refreshCartIfRequired() {
			Shopper shopper = this.realCustomerSession.getShopper();
			ShoppingCart shoppingCart = shopper.getCurrentShoppingCart();

			if (!shoppingCart.isActive()) {
				//get the completed order's order number before taking a fresh cart
				String completedOrderNumber = shoppingCart.getCompletedOrder().getOrderNumber();

				setCompletedOrderNumber(completedOrderNumber);

				shoppingCart = shoppingCartService.findOrCreateDefaultCartByCustomerSession(realCustomerSession);
				shoppingCartService.saveIfNotPersisted(shoppingCart);
				shopper.setCurrentShoppingCart(shoppingCart);
			}

			//calling getCompletedOrder(CustomerSession) will always return a non-null value
			//the intention is to avoid additional global fields so returning null, when order number is not required, is fine
			return realCustomerSession;
		}
	}

}
