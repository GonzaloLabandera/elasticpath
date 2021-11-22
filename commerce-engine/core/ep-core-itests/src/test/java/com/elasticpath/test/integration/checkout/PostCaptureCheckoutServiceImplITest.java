package com.elasticpath.test.integration.checkout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.persister.Persister;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutActionContext;
import com.elasticpath.service.shoppingcart.actions.PostCaptureCheckoutService;
import com.elasticpath.service.shoppingcart.actions.impl.PostCaptureCheckoutActionContextImpl;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.CheckoutHelper;

/**
 * Tests post capture checkout service.
 */
@ContextConfiguration(inheritLocations = true)
public class PostCaptureCheckoutServiceImplITest extends BasicSpringContextTest {

	@Autowired
	private PostCaptureCheckoutService postCaptureCheckoutService;

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

	private CheckoutHelper checkoutHelper;

	private PostCaptureCheckoutActionContext context;

	private ShoppingContext shoppingContext;

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
	public void testCheckout() {
		final ShoppingCart shoppingCart = checkoutTestCartBuilder.withPhysicalProduct().build();

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults results = checkoutHelper.checkoutCartWithoutHolds(
				shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);

		assertTrue("Shopping Cart should be empty", shoppingCart.isEmpty());

		context = new PostCaptureCheckoutActionContextImpl(results.getOrder());
		postCaptureCheckoutService.completeCheckout(context);

		assertEquals("Order should be IN_PROGRESS", context.getOrder().getStatus(), OrderStatus.IN_PROGRESS);

	}

}
