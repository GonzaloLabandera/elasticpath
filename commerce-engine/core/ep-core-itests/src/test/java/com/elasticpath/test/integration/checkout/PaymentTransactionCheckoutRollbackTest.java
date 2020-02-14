/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.checkout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.orderpaymentapi.OrderPayment;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.persister.Persister;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.orderpaymentapi.OrderPaymentService;
import com.elasticpath.service.search.query.CustomerSearchCriteria;
import com.elasticpath.service.search.query.OrderSearchCriteria;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Test payment transaction rollbacks on a failed checkout action.
 */
@ContextConfiguration
public class PaymentTransactionCheckoutRollbackTest extends BasicSpringContextTest {

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private CheckoutTestCartBuilder checkoutTestCartBuilder;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private Persister<ShoppingContext> shoppingContextPersister;

	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	@Autowired
	private OrderPaymentService orderPaymentService;

	private ShoppingContext shoppingContext;
	private Customer defaultCustomer;

	/**
	 * Set up common elements of the test.
	 */
	@Before
	public void setUp() {
		SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);

		defaultCustomer = customerBuilder.withStoreCode(scenario.getStore().getCode()).build();
		customerService.add(defaultCustomer);

		shoppingContext = shoppingContextBuilder
				.withCustomer(defaultCustomer)
				.withStoreCode(scenario.getStore().getCode())
				.build();

		shoppingContextPersister.persist(shoppingContext);

		checkoutTestCartBuilder.withScenario(scenario);
	}

	/**
	 * Ensure payment transactions on mixed cart are reversed on checkout rollback.
	 */
	@DirtiesDatabase
	@Test
	public void ensurePaymentTransactionsOnMixedCartAreReversedOnCheckoutRollback() {
		try {
			ShoppingCart shoppingCart = checkoutTestCartBuilder
					.withCustomerSession(shoppingContext.getCustomerSession())
					.withElectronicProduct()
					.withPhysicalProduct()
					.build();

			final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
			final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

			checkoutService.checkout(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);
			fail("The order should fail with an exception on checkout.");
		} catch (EpServiceException e) {
			// ignore exception
		}

		Order retrievedOrder = retrieveOrderForCustomer(defaultCustomer);
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(retrievedOrder));

		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulReverseCharge(),
						OrderPaymentMatcherFactory.createSuccessfulCancel())
				.build();

		assertThat("The order validation should succeed.", orderPaymentList, orderValidator);
		assertEquals(retrievedOrder.getStatus(), OrderStatus.FAILED);
	}

	/**
	 * Ensure payment transactions on mixed cart are reversed on checkout rollback.
	 */
	@DirtiesDatabase
	@Test
	public void ensurePaymentTransactionsOnCartWithPhysicalProductAreReversedOnCheckoutRollback() {
		try {
			ShoppingCart shoppingCart = checkoutTestCartBuilder
					.withCustomerSession(shoppingContext.getCustomerSession())
					.withPhysicalProduct()
					.build();

			final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
			final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

			checkoutService.checkout(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);
			fail("The order should fail with an exception on checkout.");
		} catch (EpServiceException e) {
			// ignore exception
		}

		Order retrievedOrder = retrieveOrderForCustomer(defaultCustomer);
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(retrievedOrder));

		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCancel())
				.build();

		assertThat("The order validation should succeed.", orderPaymentList, orderValidator);
		assertEquals(retrievedOrder.getStatus(), OrderStatus.FAILED);
	}

	@DirtiesDatabase
	@Test
	public void ensurePaymentTransactionsOnCartWithElectronicProductAreReversedOnCheckoutRollback() {
		try {
			ShoppingCart shoppingCart = checkoutTestCartBuilder
					.withCustomerSession(shoppingContext.getCustomerSession())
					.withElectronicProduct()
					.build();

			final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
			final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

			checkoutService.checkout(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);
			fail("The order should fail with an exception on checkout.");
		} catch (EpServiceException e) {
			// ignore exception
		}

		Order retrievedOrder = retrieveOrderForCustomer(defaultCustomer);
		ArrayList<OrderPayment> orderPaymentList = new ArrayList<>(orderPaymentService.findByOrder(retrievedOrder));

		OrderPaymentValidator orderValidator = OrderPaymentValidator.builder()
				.withPaymentMatchers(OrderPaymentMatcherFactory.createSuccessfulReserve(),
						OrderPaymentMatcherFactory.createSuccessfulCharge(),
						OrderPaymentMatcherFactory.createSuccessfulReverseCharge())
				.build();

		assertThat("The order validation should succeed.", orderPaymentList, orderValidator);
		assertEquals(retrievedOrder.getStatus(), OrderStatus.FAILED);
	}

	private Order retrieveOrderForCustomer(final Customer customer) {
		List<Order> orderList = orderService.findOrdersBySearchCriteria(getCriteriaThatFindsAllOrdersForCustomer(customer), 0, Integer.MAX_VALUE,
				null);
		return orderList.get(0);
	}

	private OrderSearchCriteria getCriteriaThatFindsAllOrdersForCustomer(final Customer customer) {
		CustomerSearchCriteria customerSearchCriteria = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER_SEARCH_CRITERIA, CustomerSearchCriteria.class);
		customerSearchCriteria.setGuid(customer.getGuid());
		customerSearchCriteria.setFuzzySearchDisabled(true);

		OrderSearchCriteria orderSearchCriteria = getBeanFactory().getPrototypeBean(ContextIdNames.ORDER_SEARCH_CRITERIA, OrderSearchCriteria.class);
		orderSearchCriteria.setCustomerSearchCriteria(customerSearchCriteria);
		return orderSearchCriteria;
	}
}
