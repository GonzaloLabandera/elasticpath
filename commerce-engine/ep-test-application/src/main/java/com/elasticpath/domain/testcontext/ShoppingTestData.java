/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.testcontext;

import com.elasticpath.commons.constants.ContextIdNames;
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

	private TestShopper testShopper;

	/* Wish there was another way, but time is short and some of our tests need a second CustomerSession. */
	private TestShopper secondaryTestShopper;

	private Store store;
	private Order order;

	private ShoppingTestData() {
	}

	public static ShoppingTestData getInstance() {
		return instance;
	}

	public Shopper getShopper() {
		return testShopper.getShopper();
	}

	public void setShopper(Shopper shopper) {
		this.testShopper = new TestShopperImpl(shopper, shoppingCartService);
	}

	public Shopper getSecondaryShopper() {
		return secondaryTestShopper.getShopper();
	}
	public void setSecondaryShopper(final Shopper secondaryShopper) {
		this.secondaryTestShopper = new TestShopperImpl(secondaryShopper, shoppingCartService);
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
		return getCompletedOrder(testShopper);
	}

	public Order getSecondaryCompletedOrder() {
		return getCompletedOrder(secondaryTestShopper);
	}

	//fetching completed order via OrderService, instead relying on volatile shopping cart, prevents intermittent failures
	// (shipment item subtotal can be null due to cached/non-initialized value)
	private Order getCompletedOrder(final TestShopper testShopper) {
		return orderService.findOrderByOrderNumber(testShopper.getCompletedOrderNumber());
	}

	private interface TestShopper {
		String getCompletedOrderNumber();
		void setCompletedOrderNumber(String completedOrderNumber);
		Shopper getShopper();
	}

	private static class TestShopperImpl implements TestShopper {
		private final Shopper shopper;
		private final ShoppingCartService shoppingCartService;

		private String completedOrderNumber;

		TestShopperImpl(final Shopper shopper, final ShoppingCartService shoppingCartService) {
			this.shopper = shopper;
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
		public Shopper getShopper() {
			refreshCartIfRequired();
			return shopper;
		}

		/**
		  The cart must be refreshed upon checkout. Also, completed order's order number must be preserved as well
		  so the fresh completed order is always retrieved.
		 **/
		private void refreshCartIfRequired() {
			ShoppingCart shoppingCart = shopper.getCurrentShoppingCart();

			if (!shoppingCart.isActive()) {
				//get the completed order's order number before taking a fresh cart
				String completedOrderNumber = shoppingCart.getCompletedOrder().getOrderNumber();

				setCompletedOrderNumber(completedOrderNumber);

				shoppingCart = shoppingCartService.findOrCreateDefaultCartByShopper(shopper);
				shoppingCartService.saveIfNotPersisted(shoppingCart);
				shopper.setCurrentShoppingCart(shoppingCart);
			}
		}
	}

}
