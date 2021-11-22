/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.persister;

import static com.elasticpath.commons.constants.ContextIdNames.CHECKOUT_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.PRICING_SNAPSHOT_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.SHOPPING_CART;
import static com.elasticpath.commons.constants.ContextIdNames.SHOPPING_CART_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.TAX_SNAPSHOT_SERVICE;

import java.util.HashMap;
import java.util.Map;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.service.OrderConfigurationService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutService;
import com.elasticpath.service.shoppingcart.actions.impl.PostCaptureCheckoutActionContextImpl;
import com.elasticpath.shipping.connectivity.dto.ShippingOption;

/**
 * Persister allows to create and save into database catalog dependent domain objects.
 */
public class OrderTestPersister {

	private final BeanFactory beanFactory;

	private final ShoppingCartService shoppingCartService;

	private final PricingSnapshotService pricingSnapshotService;

	private final TaxSnapshotService taxSnapshotService;

	private final TestDataPersisterFactory persisterFactory;

	private final PostCaptureCheckoutService postCaptureCheckoutService;

	private final EventOriginatorHelper eventOriginatorHelper;

	private final OrderService orderService;

	public OrderTestPersister(final BeanFactory beanFactory, final TestDataPersisterFactory persisterFactory) {
		this.beanFactory = beanFactory;
		this.persisterFactory = persisterFactory;

		shoppingCartService = beanFactory.getSingletonBean(SHOPPING_CART_SERVICE, ShoppingCartService.class);
		pricingSnapshotService = beanFactory.getSingletonBean(PRICING_SNAPSHOT_SERVICE, PricingSnapshotService.class);
		taxSnapshotService = beanFactory.getSingletonBean(TAX_SNAPSHOT_SERVICE, TaxSnapshotService.class);
		postCaptureCheckoutService = beanFactory.getSingletonBean(ContextIdNames.POST_CAPTURE_CHECKOUT_SERVICE, PostCaptureCheckoutService.class);
		orderService = beanFactory.getSingletonBean(ContextIdNames.ORDER_SERVICE, OrderService.class);
		eventOriginatorHelper = beanFactory.getSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
	}

	/**
	 * Create an empty shopping cart in the DB.
	 *
	 * @param billingAddress  the billing address
	 * @param shippingAddress the shipping address
	 * @param shippingOption  the shipping option
	 * @param store           the store the cart is for
	 * @return the persisted shopping cart
	 */
	public ShoppingCart persistEmptyShoppingCart(final Shopper shopper, final Address billingAddress, final Address shippingAddress,
												 final ShippingOption shippingOption, final Store store) {
		final ShoppingCart shoppingCart = beanFactory.getPrototypeBean(SHOPPING_CART, ShoppingCart.class);
		shoppingCart.initialize();
		shoppingCart.setBillingAddress(billingAddress);
		shoppingCart.setShippingAddress(shippingAddress);
		shoppingCart.setStore(store);
		if (shippingAddress != null) {
			shoppingCart.setSelectedShippingOption(shippingOption);
		}
		shoppingCart.setShopper(shopper);
		final ShoppingCart persistedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);
		persisterFactory.getPaymentInstrumentPersister().persistPaymentInstrument(persistedShoppingCart);
		return persistedShoppingCart;
	}

	/**
	 * Create an order in the given store with the given skus.
	 *
	 * @param store the store that the order is for
	 * @param skus  the list of skus to include in the order
	 * @return the order
	 */
	public Order createOrderWithSkus(final Store store, final ProductSku... skus) {
		return createOrderWithSkusQuantity(store, 1, skus);
	}

	/**
	 * Creates an Order containing the given quantity of each of the given Skus.
	 *
	 * @return the created Order
	 */
	public Order createOrderWithSkusQuantity(final Store store, final int quantity, final ProductSku... skus) {
		final Customer customer = persisterFactory.getStoreTestPersister().createDefaultCustomer(store);
		return createOrderForCustomerWithSkusQuantity(customer, store, quantity, skus);
	}

	/**
	 * Creates the Order containing the given quantity of each of the given Skus.
	 *
	 * @param customer the customer
	 * @param store    the store
	 * @param quantity the quantity
	 * @param skus     the skus
	 * @return the order
	 */
	public Order createOrderForCustomerWithSkusQuantity(final Customer customer, final Store store, final int quantity, final ProductSku... skus) {
		final Shopper shopper = persisterFactory.getStoreTestPersister().persistShopperWithAssociatedEntities(customer);

		final Map<ProductSku, Integer> skuQuantityMap = new HashMap<>();
		for (ProductSku sku : skus) {
			skuQuantityMap.put(sku, quantity);
		}

		CustomerAddress customerAddress = customer.getPreferredBillingAddress();
		String street1 = customerAddress.getStreet1();

		ShoppingCart shoppingCart = getOrderConfigurationService().createShoppingCart(store, customer, skuQuantityMap);
		shoppingCart.setShippingAddress(customerAddress);

		final ShippingOption shippingOption = persisterFactory.getStoreTestPersister().persistDefaultShippingOption(store);

		getOrderConfigurationService().selectCustomerAddressesToShoppingCart(shopper, street1, street1);
		shoppingCart = getOrderConfigurationService().selectShippingOption(
				shoppingCart,
				shippingOption.getDisplayName(shopper.getLocale()).orElse(null));
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		persisterFactory.getPaymentInstrumentPersister().persistPaymentInstrument(shoppingCart);

		final boolean throwExceptions = false;
		CheckoutResults checkoutResults = getCheckoutService().checkout(shoppingCart, taxSnapshot, shopper.getCustomerSession(), throwExceptions);

		// only one order should have been created by the checkout service
		return postCaptureCheckout(checkoutResults.getOrder());
	}

	private Order postCaptureCheckout(Order order) {
		PostCaptureCheckoutActionContext context = new PostCaptureCheckoutActionContextImpl(order);
		postCaptureCheckoutService.completeCheckout(context);
		Order postCaptureOrder =  orderService.findOrderByOrderNumber(order.getOrderNumber());
		postCaptureOrder.setModifiedBy(eventOriginatorHelper.getCustomerOriginator(order.getCustomer()));
		return postCaptureOrder;
	}

	OrderConfigurationService getOrderConfigurationService() {
		return beanFactory.getBean("orderConfigurationService");
	}

	protected CheckoutService getCheckoutService() {
		return beanFactory.getSingletonBean(CHECKOUT_SERVICE, CheckoutService.class);
	}
}
