/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.builder;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.impl.EventOriginatorHelperImpl;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.OrderFactory;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.persister.TestApplicationContext;
import com.elasticpath.test.util.CheckoutHelper;

public class OrderBuilder implements DomainObjectBuilder<Order> {

	private CheckoutTestCartBuilder checkoutTestCartBuilder;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private OrderFactory orderFactory;

	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	@Autowired
    private TestApplicationContext testApplicationContext;

	private boolean allShipmentsCompleted;

	private boolean sendShipmentConfirmationEmail = false;

	private ShoppingContext shoppingContext;

	public OrderBuilder withCheckoutTestCartBuilder(final CheckoutTestCartBuilder checkoutTestCartBuilder) {
		this.checkoutTestCartBuilder = checkoutTestCartBuilder;
		return this;
	}

	public OrderBuilder withNonZeroPhysicalShipment() {
		checkoutTestCartBuilder.withPhysicalProduct();
		return this;
	}

	public OrderBuilder withNonZeroElectronicShipment() {
		checkoutTestCartBuilder.withElectronicProduct();
		return this;
	}

	public OrderBuilder withFreeElectronicShipment() {
		checkoutTestCartBuilder.withFreeElectronicProduct();
		return this;
	}

	public OrderBuilder withGiftCertificateProduct() {
		checkoutTestCartBuilder.withGiftCertificateProduct();
		return this;
	}

	public OrderBuilder withAllShipmentsCompleted() {
		this.allShipmentsCompleted  = true;
		return this;
	}

	public OrderBuilder withShoppingContext(final ShoppingContext shoppingContext) {
		this.shoppingContext = shoppingContext;
		return this;
	}

	public OrderBuilder withShipmentConfirmationEmail() {
		this.sendShipmentConfirmationEmail  = true;
		return this;
	}

	public Order checkout() {
		if (checkoutTestCartBuilder == null) {
			throw new IllegalStateException("Cannot call checkout() prior to calling withCheckoutTestCartBuilder()");
		}

		if (shoppingContext == null) {
			throw new IllegalStateException("Cannot call checkout() prior to calling withShoppingContext()");
		}

		ShoppingCart shoppingCart = checkoutTestCartBuilder.build();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);
		CheckoutHelper checkoutHelper = new CheckoutHelper(testApplicationContext);
		Order order = checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart,
														   taxSnapshot,
				true);

		if (allShipmentsCompleted) {
			order = completePhysicalShipmentsForOrder(order, sendShipmentConfirmationEmail);
		}

		return order;
	}

	/**
	 * Enables the setting to hold the order and calls checkout.
	 * @return the order returned from checkout
	 */
	public Order checkoutWithHold() {
		if (checkoutTestCartBuilder == null) {
			throw new IllegalStateException("Cannot call checkout() prior to calling withCheckoutTestCartBuilder()");
		}

		if (shoppingContext == null) {
			throw new IllegalStateException("Cannot call checkout() prior to calling withShoppingContext()");
		}

		ShoppingCart shoppingCart = checkoutTestCartBuilder.build();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);
		CheckoutHelper checkoutHelper = new CheckoutHelper(testApplicationContext);
		CheckoutResults results =  checkoutHelper.checkoutCartWithHold(shoppingCart,
				taxSnapshot,
				shoppingContext.getCustomerSession(),
				true);

		return results.getOrder();
	}

	@Override
	public Order build() {
		if (checkoutTestCartBuilder == null) {
			throw new IllegalStateException("Cannot call build() prior to calling withCheckoutTestCartBuilder()");
		}

		if (shoppingContext == null) {
			throw new IllegalStateException("Cannot call build() prior to calling withShoppingContext()");
		}

		final ShoppingCart shoppingCart = checkoutTestCartBuilder.build();
		final Customer customer = shoppingContext.getShopper().getCustomer();
		final CustomerSession customerSession = shoppingContext.getCustomerSession();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = orderFactory.createAndPersistNewEmptyOrder(
				customer,
				customerSession,
				shoppingCart,
				false,
				false);

		order = orderFactory.fillInNewOrderFromShoppingCart(order,
															customer,
															customerSession,
															shoppingCart,
															taxSnapshot);

		order.getModifierFields().putAll(shoppingCart.getModifierFields().getMap());

		order.releaseOrder();

		if (allShipmentsCompleted) {
			order = completePhysicalShipmentsForOrder(order, sendShipmentConfirmationEmail);
		}

		return orderService.update(order);
	}

	private Order completePhysicalShipmentsForOrder(final Order order, final boolean sendShipmentConfirmationEmail) {
		Order completedOrder = order;
		for (OrderShipment orderShipment : order.getPhysicalShipments()) {
			orderShipment = orderService.processReleaseShipment(orderShipment);
			String shipmentNumber = orderShipment.getShipmentNumber();
			EventOriginator systemOriginator = new EventOriginatorHelperImpl().getSystemOriginator();
			completedOrder = orderService.completeShipment(shipmentNumber, "trackingNumber", true, null,
				sendShipmentConfirmationEmail, systemOriginator);
		}

		return completedOrder;
	}
}
