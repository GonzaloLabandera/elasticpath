/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.tax;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.order.ElectronicOrderShipment;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderAddress;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderShipmentStatus;
import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.order.PhysicalOrderShipment;
import com.elasticpath.domain.order.impl.PhysicalOrderShipmentImpl;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.plugin.tax.domain.impl.StringTaxDocumentId;
import com.elasticpath.service.tax.TaxDocumentModificationContext;
import com.elasticpath.service.tax.TaxDocumentModificationItem;
import com.elasticpath.service.tax.TaxDocumentModificationType;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Integration test for TaxOperationService.
 */
public class TaxOperationOrderModificationTest extends AbstractBasicTaxOperationTest {

	/**
	 * Test augmenting the shipment total.
	 */
	@DirtiesDatabase
	@Test
	public void testAddItemToPhysicalOrderShipment() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());

		ShoppingItemDto physicalDto = new ShoppingItemDto(shippableProducts.get(0).getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, physicalDto);

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		// checkout
		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertEquals("only one order should have been created by the checkout service", 1, ordersList.size());
		Order order = ordersList.iterator().next();

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		assertNotNull("Order should have a modified by value", order.getModifiedBy());

		List<OrderShipment> shipments = order.getAllShipments();
		assertEquals("one shipment should have been created", 1, shipments.size());

		final Collection<OrderPayment> orderPayments = orderPaymentService.findByOrder(order);
		assertEquals(1, orderPayments.size());
		OrderPayment authPayment = orderPayments.iterator().next();
		assertEquals("payment total should be for full amount of order", order.getTotal().doubleValue(), authPayment.getAmount().doubleValue(), 0);

		PhysicalOrderShipment phShipment = order.getPhysicalShipments().iterator().next();

		// the new one is recorded in Tax journal as purchas
		verifyTaxDocumentForOrderShipment(phShipment, store);

		// Add item to the existing order shipment
		OrderSku newProductOrderSku = getNewProductOrderSku(scenario, "newSku-1", order.getUidPk());
		BigDecimal previousTotal = phShipment.getTotal();
		phShipment.addShipmentOrderSku(newProductOrderSku);
		assertTrue("the previous total amount should be less than the new one", previousTotal.compareTo(phShipment.getTotal()) < 0);

		TaxDocumentModificationContext taxDocumentModificationContext = new TaxDocumentModificationContext();
		taxDocumentModificationContext.add(phShipment,
				buildOrderShipmentAddress(order).get(phShipment.getShipmentNumber()),
				TaxDocumentModificationType.UPDATE);
		// the order shipment has new item added
		order = orderService.update(order, taxDocumentModificationContext);

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		phShipment = order.getPhysicalShipments().iterator().next();

		// the old one is recorded in Tax journal as return
		verifyUpdateTaxDocuments(order, taxDocumentModificationContext);

		// add new item again
		// Add item to the existing order shipment
		OrderSku newProductOrderSku2 = getNewProductOrderSku(scenario, "newSku-2", order.getUidPk());
		BigDecimal previousTotal2 = phShipment.getTotal();
		phShipment.addShipmentOrderSku(newProductOrderSku2);
		assertTrue("the previous total amount should be less than the new one", previousTotal2.compareTo(phShipment.getTotal()) < 0);

		taxDocumentModificationContext.add(phShipment,
				buildOrderShipmentAddress(order).get(phShipment.getShipmentNumber()),
				TaxDocumentModificationType.UPDATE);
		order = orderService.update(order, taxDocumentModificationContext);
		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());

		// the old one is recorded in Tax journal as return
		verifyUpdateTaxDocuments(order, taxDocumentModificationContext);
	}


	/**
	 * Test creating a new shipment.
	 */
	@DirtiesDatabase
	@Test
	public void testCreateNewShipment() {
		// construct new shopping cart
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());

		ShoppingItemDto physicalDto = new ShoppingItemDto(shippableProducts.get(1).getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, physicalDto);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertEquals("Only one order should have been created by the checkout service", 1, ordersList.size());
		Order order = ordersList.iterator().next();

		List<OrderShipment> shipments = order.getAllShipments();
		assertEquals("one shipment should have been created", 1, shipments.size());

		final Collection<OrderPayment> orderPayments = orderPaymentService.findByOrder(order);
		assertEquals(1, orderPayments.size());
		OrderPayment authPayment = orderPayments.iterator().next();
		BigDecimal originalTotal = order.getTotal();
		assertEquals("Payment should be for the full amount of the order", originalTotal.doubleValue(), authPayment.getAmount().doubleValue(), 0);

		PhysicalOrderShipment phShipment = order.getPhysicalShipments().iterator().next();

		// create new order shipment
		PhysicalOrderShipmentImpl newPhysicalShipment = new PhysicalOrderShipmentImpl();
		newPhysicalShipment.setCreatedDate(new Date());
		newPhysicalShipment.setLastModifiedDate(new Date());
		newPhysicalShipment.setOrder(order);
		newPhysicalShipment.setStatus(OrderShipmentStatus.INVENTORY_ASSIGNED);
		newPhysicalShipment.initialize();

		OrderSku newProductOrderSku = getNewProductOrderSku(scenario, "newSku-2", order.getUidPk());
		newPhysicalShipment.addShipmentOrderSku(newProductOrderSku);
		newPhysicalShipment.setShippingOptionCode(scenario.getShippingOption().getCode());
		newPhysicalShipment.setShippingCost(BigDecimal.ONE);
		newPhysicalShipment.setShipmentAddress(phShipment.getShipmentAddress());

		// add new order shipment to the order
		order.addShipment(newPhysicalShipment);
		assertTrue("the new shipment total should be > 0", BigDecimal.ZERO.compareTo(newPhysicalShipment.getTotal()) < 0);
		assertEquals("the order should now have 2 physical shipments", 2, order.getPhysicalShipments().size());
		assertEquals("Order shipment should have a total of the original order", originalTotal, phShipment.getTotal());

		TaxDocumentModificationContext taxDocumentModificationContext = new TaxDocumentModificationContext();
		taxDocumentModificationContext.add(phShipment,
				buildOrderShipmentAddress(order).get(phShipment.getShipmentNumber()),
				TaxDocumentModificationType.UPDATE);

		taxDocumentModificationContext.add(newPhysicalShipment,
				null,
				TaxDocumentModificationType.NEW);

		// the order shipment has new item added
		order = orderService.update(order, taxDocumentModificationContext);

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());

		verifyUpdateTaxDocuments(order, taxDocumentModificationContext);
	}

	/**
	 * Test augmenting the shipment total.
	 */
	@DirtiesDatabase
	@Test
	public void testRemoveItemToPhysicalOrderShipment() {
		// construct and save new shopping cart
		ShoppingCart shoppingCart = persisterFactory.getOrderTestPersister().persistEmptyShoppingCart(shopper, address, address,
				scenario.getShippingOption(), scenario.getStore());

		ShoppingItemDto physicalDto = new ShoppingItemDto(shippableProducts.get(0).getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(shoppingCart, physicalDto);

		// checkout
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);

		List<Order> ordersList = orderService.findOrderByCustomerGuid(shopper.getCustomer().getGuid(), true);
		assertEquals("only one order should have been created by the checkout service", 1, ordersList.size());
		Order order = ordersList.iterator().next();

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		assertNotNull("Order should have a modified by value", order.getModifiedBy());

		List<OrderShipment> shipments = order.getAllShipments();
		assertEquals("one shipment should have been created", 1, shipments.size());

		final Collection<OrderPayment> orderPayments = orderPaymentService.findByOrder(order);
		assertEquals(1, orderPayments.size());
		OrderPayment authPayment = orderPayments.iterator().next();
		assertEquals("payment total should be for full amount of order", order.getTotal().doubleValue(), authPayment.getAmount().doubleValue(), 0);

		PhysicalOrderShipment phShipment = order.getPhysicalShipments().iterator().next();

		// Add item to the existing order shipment
		OrderSku newProductOrderSku = getNewProductOrderSku(scenario, "newSku-3", order.getUidPk());
		BigDecimal previousTotal = phShipment.getTotal();
		phShipment.addShipmentOrderSku(newProductOrderSku);
		assertTrue("the previous total amount should be less than the new one", previousTotal.compareTo(phShipment.getTotal()) < 0);

		// the existing shipment is modified by adding a new item
		TaxDocumentModificationContext taxDocumentModificationContext = new TaxDocumentModificationContext();
		taxDocumentModificationContext.add(phShipment,
				buildOrderShipmentAddress(order).get(phShipment.getShipmentNumber()),
				TaxDocumentModificationType.UPDATE);
		order = orderService.update(order, taxDocumentModificationContext);

		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());
		phShipment = order.getPhysicalShipments().iterator().next();

		verifyUpdateTaxDocuments(order, taxDocumentModificationContext);
		taxDocumentModificationContext.clear();

		// remove the new added order sku
		phShipment.removeShipmentOrderSku(newProductOrderSku, productSkuLookup);

		taxDocumentModificationContext.add(phShipment,
				buildOrderShipmentAddress(order).get(phShipment.getShipmentNumber()),
				TaxDocumentModificationType.UPDATE);
		order = orderService.update(order, taxDocumentModificationContext);
		order.setModifiedBy(getEventOriginatorHelper().getSystemOriginator());

		order.getPhysicalShipments().iterator().next();

		verifyUpdateTaxDocuments(order, taxDocumentModificationContext);
	}

	private void verifyUpdateTaxDocuments(final Order order, final TaxDocumentModificationContext taxDocumentModificationContext) {

		// the new one is recorded in Tax journal as purchase
		for (TaxDocumentModificationItem item : taxDocumentModificationContext.get(TaxDocumentModificationType.NEW)) {
			OrderShipment orderShipment = order.getShipment(item.getTaxDocumentReferenceId());
			verifyTaxDocumentForOrderShipment(orderShipment, store);
		}

		// the updated one has one return for its previous tax document, and one purchase for the updated tax document
		for (TaxDocumentModificationItem item : taxDocumentModificationContext.get(TaxDocumentModificationType.UPDATE)) {
			OrderShipment orderShipment = order.getShipment(item.getTaxDocumentReferenceId());
			verifyTaxDocumentReversal(StringTaxDocumentId.fromString(item.getPreviousTaxDocumentId()));
			verifyTaxDocumentForOrderShipment(orderShipment, store);
		}

		// the cancelled one has one return for it
		for (TaxDocumentModificationItem item : taxDocumentModificationContext.get(TaxDocumentModificationType.CANCEL)) {
			verifyTaxDocumentReversal(StringTaxDocumentId.fromString(item.getPreviousTaxDocumentId()));

		}
	}

	/**
	 * Remembers the original order shipment tax address, in case any ordershipment changes, the tax can be recorded correctly.
	 */
	private Map<String, OrderAddress> buildOrderShipmentAddress(final Order order) {

		Map<String, OrderAddress> addresses = new HashMap<>();

		for (PhysicalOrderShipment orderShipment : order.getPhysicalShipments()) {
			addresses.put(orderShipment.getShipmentNumber(), orderShipment.getShipmentAddress());
		}

		for (ElectronicOrderShipment orderShipment : order.getElectronicShipments()) {
			addresses.put(orderShipment.getShipmentNumber(), order.getBillingAddress());
		}

		return addresses;
	}
}
