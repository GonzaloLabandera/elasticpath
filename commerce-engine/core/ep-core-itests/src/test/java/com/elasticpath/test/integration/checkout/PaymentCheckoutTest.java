/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.checkout;

import static com.elasticpath.plugin.payment.provider.dto.TransactionType.RESERVE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.event.EventOriginator;
import com.elasticpath.domain.event.impl.EventOriginatorHelperImpl;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderShipment;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.orderpaymentapi.OrderPaymentAmounts;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.money.Money;
import com.elasticpath.persister.Persister;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.charge.ChargeCapability;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.provider.payment.service.PaymentsException;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.order.CompleteShipmentFailedException;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.orderpaymentapi.impl.OrderPaymentApiServiceImpl;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.CheckoutHelper;

@ContextConfiguration
public class PaymentCheckoutTest extends BasicSpringContextTest {

	private static final String VALIDATION_SHOULD_SUCCEED = "The order validation should succeed.";
	private static final String SHOULD_HAVE_THROWN_AN_EXCEPTION = "The finalize shipment capability should have thrown an exception.";
	private static final String INTERNAL_MESSAGE = "internal message";
	private static final String EXTERNAL_MESSAGE = "external message";

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CheckoutTestCartBuilder checkoutTestCartBuilder;
	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private Persister<ShoppingContext> shoppingContextPersister;

	private ShoppingContext shoppingContext;

	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	@Autowired
	private OrderPaymentService orderPaymentService;

	@Autowired
	private OrderPaymentApiServiceImpl orderPaymentApiService;

	private EventOriginator originator;
	private CheckoutHelper checkoutHelper;

	/**
	 * Set up common elements of the test.
	 */
	@Before
	public void setUp() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);

		final String storeCode = scenario.getStore().getCode();
		Customer testCustomer = customerBuilder.withStoreCode(storeCode)
				.build();
		customerService.add(testCustomer);

		shoppingContext = shoppingContextBuilder
				.withCustomer(testCustomer)
				.withStoreCode(storeCode)
				.build();

		shoppingContextPersister.persist(shoppingContext);

		checkoutTestCartBuilder.withScenario(scenario)
				.withShopper(shoppingContext.getShopper());
		originator = new EventOriginatorHelperImpl().getSystemOriginator();
		checkoutHelper = new CheckoutHelper(getTac());
	}

	/**
	 * Test checkout is successful.
	 */
	@DirtiesDatabase
	@Test
	public void successfulCheckout() {
		ShoppingCart shoppingCart = buildShoppingCartWithElectronicProduct();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkout(shoppingCart, taxSnapshot);
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCharge())
				.build();

		assertEquals(order.getStatus(), OrderStatus.COMPLETED);
		assertThat(VALIDATION_SHOULD_SUCCEED, orderPaymentList, orderValidator);
	}

	/**
	 * Test 'ensure checkout with failing plugin for reserve throws {@link PaymentsException}.
	 */
	@DirtiesDatabase
	@Test(expected = PaymentsException.class)
	public void ensureCheckoutWithFailingPluginForReserveThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReserveCapability.class, request -> {
			throw new PaymentCapabilityRequestFailedException(INTERNAL_MESSAGE, EXTERNAL_MESSAGE, false);
		});
		ShoppingCart shoppingCart = buildShoppingCartWithElectronicProduct();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkout(shoppingCart, taxSnapshot);
	}

	/**
	 * Test 'ensure checkout with failing plugin for charge throws {@link EpServiceException}.
	 */
	@DirtiesDatabase
	@Test(expected = EpServiceException.class)
	public void ensureCheckoutWithFailingForChargePluginThrowsException() {
		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ChargeCapability.class, request -> {
			throw new PaymentCapabilityRequestFailedException(INTERNAL_MESSAGE, EXTERNAL_MESSAGE, false);
		});
		ShoppingCart shoppingCart = buildShoppingCartWithElectronicProduct();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		checkout(shoppingCart, taxSnapshot);
	}

	/**
	 * Test checkout with physical goods is successful.
	 */
	@DirtiesDatabase
	@Test
	public void checkoutWithPhysicalGoodsIsSuccessful() {
		ShoppingCart shoppingCart = buildShoppingCartWithPhysicalProduct();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkout(shoppingCart, taxSnapshot);
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve())
				.build();
		assertEquals(order.getStatus(), OrderStatus.IN_PROGRESS);
		assertThat(VALIDATION_SHOULD_SUCCEED, orderPaymentList, orderValidator);
	}

	/**
	 * Test checkout with physical goods creates a split shipment.
	 */
	@DirtiesDatabase
	@Test
	public void checkoutWithPhysicalProductCreatesSplitShipment() {
		ShoppingCart shoppingCart = buildShoppingCartWithElectronicAndPhysicalProducts();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkout(shoppingCart, taxSnapshot);
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulReserve(), OrderPaymentMatcherFactory.createSuccessfulCharge())
				.build();
		assertEquals(order.getStatus(), OrderStatus.PARTIALLY_SHIPPED);
		assertThat(VALIDATION_SHOULD_SUCCEED, orderPaymentList, orderValidator);
	}

	/**
	 * Test checkout with both physical and electronic goods is successful.
	 */
	@DirtiesDatabase
	@Test
	public void checkoutWithBothPhysicalAndElectronicProductsIsSuccessful() {
		ShoppingCart shoppingCart = buildShoppingCartWithElectronicAndPhysicalProducts();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkout(shoppingCart, taxSnapshot);
		releaseAndCompletePhysicalShipmentsForOrder(order);
		order = orderService.findOrderByOrderNumber(order.getOrderNumber());
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulCharge())
				.build();
		assertEquals(order.getStatus(), OrderStatus.COMPLETED);
		assertThat(VALIDATION_SHOULD_SUCCEED, orderPaymentList, orderValidator);
	}

	/**
	 * Ensure failed charge prevents shipment completion.
	 */
	@DirtiesDatabase
	@Test
	public void ensureFailedChargePreventsShipmentCompletion() {
		ShoppingCart shoppingCart = buildShoppingCartWithPhysicalProduct();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkout(shoppingCart, taxSnapshot);
		OrderShipment orderShipment = order.getPhysicalShipments().get(0);
		orderShipment = orderService.processReleaseShipment(orderShipment);

		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		if (!orderPaymentList.get(0).getTransactionType().equals(RESERVE) && orderPaymentList.size() == 1) {
			fail("Only payment on physical shipment should be authorization at this point");
		}

		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ChargeCapability.class, request -> {
			throw new PaymentCapabilityRequestFailedException(INTERNAL_MESSAGE, EXTERNAL_MESSAGE, false);
		});

		try {
			completeShipment(orderShipment.getShipmentNumber(), originator);
			fail("Exception should have been thrown because there was an reserve reversal");
		} catch (CompleteShipmentFailedException e) {
			// exception thrown as expected, move on
		}

		order = orderService.findOrderByOrderNumber(order.getOrderNumber());
		ArrayList<OrderPayment> newOrderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createFailedCharge(),
						OrderPaymentMatcherFactory.createSuccessfulCancel(),
						OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createFailedCharge())
				.build();
		assertEquals(order.getStatus(), OrderStatus.IN_PROGRESS);
		assertThat(VALIDATION_SHOULD_SUCCEED, newOrderPaymentList, orderValidator);
	}

	/**
	 * Ensure successful roll back of payment transactions when post completion of shipment fails with a physical shipment.
	 */
	@DirtiesDatabase
	@Test
	public void ensureSuccessfulRollbackOfPaymentsWhenPostCompletionOfShipmentFailsWithPhysicalShipment() {
		ShoppingCart shoppingCart = buildShoppingCartWithPhysicalProduct();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkout(shoppingCart, taxSnapshot);
		OrderShipment orderShipment = order.getPhysicalShipments().get(0);
		orderShipment = orderService.processReleaseShipment(orderShipment);
		try {
			completeShipment(orderShipment.getShipmentNumber(), null);
			fail(SHOULD_HAVE_THROWN_AN_EXCEPTION);
		} catch (CompleteShipmentFailedException e) {
			// exception thrown as expected, move on
		}

		order = orderService.findOrderByOrderNumber(order.getOrderNumber());
		ArrayList<OrderPayment> newOrderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulReverseCharge())
				.build();
		assertEquals(order.getStatus(), OrderStatus.IN_PROGRESS);
		assertThat(VALIDATION_SHOULD_SUCCEED, newOrderPaymentList, orderValidator);
	}

	/**
	 * Ensure successful roll back of payment transactions and successful completion of order after roll back.
	 */
	@DirtiesDatabase
	@Test
	public void ensureSuccessfulRollbackOfPaymentsAndSuccessfulCompletionOfOrder() {
		final ShoppingCart shoppingCart = buildShoppingCartWithPhysicalProduct();
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkout(shoppingCart, taxSnapshot);
		OrderShipment orderShipment = order.getPhysicalShipments().get(0);
		orderShipment = orderService.processReleaseShipment(orderShipment);
		final Money moneyBeforeChanges = order.getTotalMoney();
		try {
			completeShipment(orderShipment.getShipmentNumber(), null);
			fail(SHOULD_HAVE_THROWN_AN_EXCEPTION);
		} catch (CompleteShipmentFailedException e) {
			// exception thrown as expected, move on
		}
		//complete shipment after rollback.
		completeShipment(orderShipment.getShipmentNumber(), originator);

		order = orderService.findOrderByOrderNumber(order.getOrderNumber());
		OrderPaymentAmounts orderPaymentAmounts = orderPaymentApiService.getOrderPaymentAmounts(order);
		ArrayList<OrderPayment> newOrderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(order));
		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulReverseCharge(),
						OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulReserve())
				.build();

		assertEquals(order.getStatus(), OrderStatus.COMPLETED);
		assertEquals(order.getTotalMoney(), moneyBeforeChanges);
		assertEquals(orderPaymentAmounts.getAmountPaid(), moneyBeforeChanges);
		assertEquals(orderPaymentAmounts.getAmountDue(), Money.valueOf(BigDecimal.ZERO, moneyBeforeChanges.getCurrency()));
		assertThat(VALIDATION_SHOULD_SUCCEED, newOrderPaymentList, orderValidator);
	}

	@After
	public void tearDown() {
		PaymentProviderPluginForIntegrationTesting.resetCapabilities();
	}

	private void releaseAndCompletePhysicalShipmentsForOrder(final Order order) {
		for (OrderShipment orderShipment : order.getPhysicalShipments()) {
			orderShipment = orderService.processReleaseShipment(orderShipment);
			completeShipment(orderShipment.getShipmentNumber(), originator);
		}
	}

	private void completeShipment(final String shipmentNumber, final EventOriginator eventOriginator) {
		orderService.completeShipment(shipmentNumber,
				"trackingNumber", true, null, false, eventOriginator);
	}

	private ShoppingCart buildShoppingCartWithPhysicalProduct() {
		return checkoutTestCartBuilder
				.withPhysicalProduct()
				.build();
	}

	private ShoppingCart buildShoppingCartWithElectronicProduct() {
		return checkoutTestCartBuilder
				.withElectronicProduct()
				.build();
	}

	private ShoppingCart buildShoppingCartWithElectronicAndPhysicalProducts() {
		return checkoutTestCartBuilder
				.withElectronicProduct()
				.withPhysicalProduct()
				.build();
	}

	private Order checkout(final ShoppingCart shoppingCart, final ShoppingCartTaxSnapshot taxSnapshot) {
		return checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart, taxSnapshot, true);
	}
}