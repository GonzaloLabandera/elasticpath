package com.elasticpath.test.integration.checkout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderHoldStatus;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.persister.Persister;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.order.OrderHoldService;
import com.elasticpath.service.order.OrderService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.actions.impl.EvaluateOrderHoldStrategiesCheckoutAction;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.CheckoutHelper;

public class EvaluateOrderHoldStrategiesITest extends BasicSpringContextTest {

	@Autowired
	private EvaluateOrderHoldStrategiesCheckoutAction evaluateOrderHoldStrategiesCheckoutAction;

	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private CheckoutTestCartBuilder checkoutTestCartBuilder;

	@Autowired
	private CustomerBuilder customerBuilder;

	@Autowired
	private Persister<ShoppingContext> shoppingContextPersister;

	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	@Autowired
	private CustomerService customerService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderHoldService orderHoldService;

	private ShoppingContext shoppingContext;

	private CheckoutHelper checkoutHelper;

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

	@DirtiesDatabase
	@Test
	public void testHoldListInvokedAndHoldCreated() {
		final ShoppingCart shoppingCart = checkoutTestCartBuilder.withPhysicalProduct().build();

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults results = checkoutHelper.checkoutCartWithHold(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);

		Order order = results.getOrder();

		//reload order from db to verify that everything was properly persisted
		order = orderService.findOrderByOrderNumber(order.getOrderNumber());

		assertEquals("The order is not ON_HOLD", order.getStatus(), OrderStatus.ONHOLD);
		orderHoldService.findOrderHoldsByOrderUid(order.getUidPk()).forEach(hold -> {
			assertEquals("The order hold description does not match", hold.getHoldDescription(), "All orders are configured for hold processing");
			assertEquals("The order hold permission does not match", hold.getPermission(), "RESOLVE_GENERIC_HOLD");
			assertNotEquals("The order hold resolved date is not null", hold.getResolvedDate());
			assertEquals("The order hold is not active", hold.getStatus(), OrderHoldStatus.ACTIVE);
		});
	}

	@DirtiesDatabase
	@Test
	public void testHoldNotCreatedWhenEvaluateIsEmpty() {
		final ShoppingCart shoppingCart = checkoutTestCartBuilder.withPhysicalProduct().build();

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);
		evaluateOrderHoldStrategiesCheckoutAction.setOrderHoldStrategyXPFBridge(context -> Collections.emptyList());
		CheckoutResults results = checkoutHelper.checkoutCartWithHold(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);

		Order order = results.getOrder();

		//reload order from db to verify that everything was properly persisted
		order = orderService.findOrderByOrderNumber(order.getOrderNumber());

		assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
		assertThat(orderHoldService.findOrderHoldsByOrderUid(order.getUidPk())).hasSize(0);
	}
}
