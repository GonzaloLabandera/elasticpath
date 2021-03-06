/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.tax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Integration test for TaxOperationService.
 */
public class TaxOperationOrderCancellationTest extends AbstractBasicTaxOperationTest {

	/**
	 * Test cancelling an order.
	 */
	@DirtiesDatabase
	@Test
	public void testCancelOrder() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());

		ShoppingItemDto physicalDto = new ShoppingItemDto(shippableProducts.get(0).getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, physicalDto);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		// only one order should have been created by the checkout service
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		Order order = ordersList.iterator().next();

		// one shipments should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertEquals(1, shipments.size());

		// check payments
		final Collection<OrderPayment> orderPayments = orderPaymentService.findByOrder(order);
		assertEquals(1, orderPayments.size());
		OrderPayment authPayment = orderPayments.iterator().next();
		assertEquals(order.getTotal().doubleValue(), authPayment.getAmount().doubleValue(), 0);

		assertTrue(order.isCancellable());
		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		order = orderService.cancelOrder(order);

		assertEquals(OrderStatus.CANCELLED, order.getStatus());

		for (OrderShipment orderShipment : order.getAllShipments()) {
			verifyTaxDocumentReversal(orderShipment.getTaxDocumentId());
		}
	}

	/**
	 * Test canceling a shipment.
	 */
	@DirtiesDatabase
	@Test
	public void testCancelShipment() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());

		ShoppingItemDto physicalDto = new ShoppingItemDto(shippableProducts.get(0).getDefaultSku().getSkuCode(), 1);
		ShoppingItemDto electronicDto = new ShoppingItemDto(nonShippableProducts.get(0).getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, physicalDto);
		cartDirector.addItemToCart(shoppingCart, electronicDto);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		// only one order should have been created by the checkout service
		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertEquals(1, ordersList.size());
		Order order = ordersList.iterator().next();

		// two shipment should have been created
		List<OrderShipment> shipments = order.getAllShipments();
		assertEquals(2, shipments.size());

		PhysicalOrderShipment phShipment = order.getPhysicalShipments().iterator().next();
		assertTrue(phShipment.isCancellable());

		phShipment = orderService.cancelOrderShipment(phShipment);

		assertEquals(OrderShipmentStatus.CANCELLED, phShipment.getShipmentStatus());
		assertEquals(OrderStatus.COMPLETED, phShipment.getOrder().getStatus()); // the order has one electornic shipment, so its status is COMPLETE

		verifyTaxDocumentReversal(phShipment.getTaxDocumentId());
	}
}
