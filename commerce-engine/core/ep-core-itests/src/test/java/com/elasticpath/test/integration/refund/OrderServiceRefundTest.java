/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.refund;

import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.builder.OrderBuilder;
import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.money.Money;
import com.elasticpath.persister.Persister;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.order.IncorrectRefundAmountException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.integration.checkout.OrderPaymentValidator;
import com.elasticpath.test.integration.checkout.OrderPaymentMatcherFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Tests refunds of orders made with tokenized payments.
 */
@ContextConfiguration
public class OrderServiceRefundTest extends BasicSpringContextTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Autowired
	private OrderService orderService;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private EventOriginatorHelper eventOriginatorHelper;

	@Autowired
	private OrderBuilder orderBuilder;

	@Autowired
	private CheckoutTestCartBuilder checkoutTestCartBuilder;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private Persister<ShoppingContext> shoppingContextPersister;

	@Autowired
	private OrderPaymentService orderPaymentService;

	/**
	 * Set up {@link CheckoutTestCartBuilder} and {@link OrderBuilder}.
	 */
	@Before
	public void setUp() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);
		Customer testCustomer = customerBuilder
				.withStoreCode(scenario.getStore().getCode())
				.build();
		customerService.add(testCustomer);

		final ShoppingContext shoppingContext = shoppingContextBuilder
				.withCustomer(testCustomer)
				.withStoreCode(scenario.getStore().getCode())
				.build();

		shoppingContextPersister.persist(shoppingContext);

		final CustomerSession customerSession = shoppingContext.getCustomerSession();

		checkoutTestCartBuilder.withScenario(scenario)
				.withCustomerSession(customerSession);

		orderBuilder.withCheckoutTestCartBuilder(checkoutTestCartBuilder)
				.withShoppingContext(shoppingContext);
	}

	/**
	 * Successful full refund for electronic shipment.
	 */
	@DirtiesDatabase
	@Test
	public void successfulFullRefundForElectronicShipment() {
		Order order = orderBuilder.withNonZeroElectronicShipment().checkout();
		orderService.refundOrderPayment(order,
				null,
				Money.valueOf(getAmountPaidForOrder(order), order.getCurrency()),
				createEventOriginator());

		assertEquals(OrderStatus.COMPLETED, order.getStatus());
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		assertThat("The order validation should succeed.", orderPaymentList, OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulCredit())
				.build());
	}

	/**
	 * Successful full refund for physical shipment.
	 */
	@DirtiesDatabase
	@Test
	public void successfulFullRefundForPhysicalShipment() {
		Order completedOrder = orderBuilder.withNonZeroPhysicalShipment()
				.withAllShipmentsCompleted()
				.checkout();

		orderService.refundOrderPayment(
				completedOrder,
				null,
				Money.valueOf(getAmountPaidForOrder(completedOrder), completedOrder.getCurrency()),
				createEventOriginator());

		assertEquals(OrderStatus.COMPLETED, completedOrder.getStatus());
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(completedOrder));
		assertThat("The order validation should succeed.", orderPaymentList, OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulCredit())
				.build());
	}

	/**
	 * Refunds on free electronic shipments throw exception.
	 */
	@DirtiesDatabase
	@Test
	public void refundsOnFreeElectronicShipmentsThrowException() {
		Order completedOrder = orderBuilder.withFreeElectronicShipment().checkout();

		exception.expect(IncorrectRefundAmountException.class);
		exception.expectMessage("Amount to refund must be positive.");
		orderService.refundOrderPayment(
				completedOrder,
				null,
				Money.valueOf(getAmountPaidForOrder(completedOrder), completedOrder.getCurrency()),
				createEventOriginator());

		assertThat(orderPaymentService.findByOrder(completedOrder), empty());
	}

	/**
	 * Refund on incomplete physical shipment should throw exception.
	 */
	@DirtiesDatabase
	@Test
	public void refundOnIncompletePhysicalShipmentShouldThrowException() {
		Order order = orderBuilder.withNonZeroPhysicalShipment().checkout();

		exception.expect(EpServiceException.class);
		exception.expectMessage("Order is not applicable for a refund.");
		orderService.refundOrderPayment(order,
				null,
				Money.valueOf(getAmountPaidForOrder(order), order.getCurrency()),
				createEventOriginator());
	}

	/**
	 * Refund exceeding original capture amount should throw exception.
	 */
	@DirtiesDatabase
	@Test
	public void refundExceedingOriginalCaptureAmountShouldThrowException() {
		Order order = orderBuilder.withNonZeroElectronicShipment().checkout();

		BigDecimal amountPaidForOrder = getAmountPaidForOrder(order);
		BigDecimal amountExceedingOriginalOrder = amountPaidForOrder.add(BigDecimal.ONE);
		exception.expect(IncorrectRefundAmountException.class);
		exception.expectMessage("The refund amount exceeds the total amount captured for this order.");
		orderService.refundOrderPayment(order,
				null,
				Money.valueOf(amountExceedingOriginalOrder, order.getCurrency()),
				createEventOriginator());
	}

	private BigDecimal getAmountPaidForOrder(Order order) {
		return order.getAllShipments().stream().map(OrderShipment::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private EventOriginator createEventOriginator() {
		return eventOriginatorHelper.getSystemOriginator();
	}

}
