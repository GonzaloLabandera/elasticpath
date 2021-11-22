/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.checkout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.persister.Persister;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.order.CompleteShipmentFailedException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.CheckoutHelper;

/**
 * Test payment transaction rollbacks on a failed cancellation shipment.
 */
@ContextConfiguration
public class PaymentRollbackOnCancellationShipmentTest extends BasicSpringContextTest {

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private CheckoutTestCartBuilder checkoutTestCartBuilder;

	@Autowired
	private CustomerBuilder customerBuilder;

	private ShoppingContext shoppingContext;

	@Autowired
	private Persister<ShoppingContext> shoppingContextPersister;

	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	@Autowired
	private OrderPaymentService orderPaymentService;

	private CheckoutHelper checkoutHelper;

	/**
	 * Set up common elements of the test.
	 */
	@Before
	public void setUp() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);

		final String storeCode = scenario.getStore().getCode();
		Customer testCustomer = customerBuilder.withStoreCode(storeCode).build();
		customerService.add(testCustomer);

		shoppingContext = shoppingContextBuilder.withCustomer(testCustomer)
				.withStoreCode(storeCode)
				.build();

		shoppingContextPersister.persist(shoppingContext);

		checkoutTestCartBuilder.withScenario(scenario)
				.withShopper(shoppingContext.getShopper());

		checkoutHelper = new CheckoutHelper(getTac());
	}

	/**
	 * Ensure successful roll back of payment transactions when post completion of shipment fails with a physical shipment.
	 */
	@DirtiesDatabase
	@Test
	public void ensureSuccessfullRollbackOfPaymentsWhenPostCompletionOfShipmentFailsWithPhysicalShipment() {
		final ShoppingCart shoppingCart = checkoutTestCartBuilder.withPhysicalProduct().build();

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(
				shoppingCart, taxSnapshot, true);

		try {
			completePhysicalShipmentsForOrder(order);
			fail("The finalize shipment capability should have thrown an exception.");
		} catch (CompleteShipmentFailedException e) {
			// exception thrown as expected, move on
		}

		order = orderService.findOrderByOrderNumber(order.getOrderNumber());
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulReverseCharge())
				.build();

		assertEquals(order.getStatus(), OrderStatus.IN_PROGRESS);
		assertThat("The order validation should succeed.", orderPaymentList, orderValidator);
	}

	private void completePhysicalShipmentsForOrder(final Order order) {
		for (OrderShipment orderShipment : order.getPhysicalShipments()) {
			orderShipment = orderService.processReleaseShipment(orderShipment);
			orderService.completeShipment(orderShipment.getShipmentNumber(),
					"trackingNumber", true, null, false, null);
		}
	}
}
