/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.test.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Currency;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.sellingchannel.ShoppingItemDtoFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.impl.CustomerAddressImpl;
import com.elasticpath.domain.event.EventOriginatorHelper;
import com.elasticpath.domain.factory.TestShoppingCartFactoryForTestApplication;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.plugin.payment.provider.capabilities.PaymentCapabilityRequestFailedException;
import com.elasticpath.plugin.payment.provider.capabilities.reservation.ReserveCapability;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.payment.provider.PaymentProviderPluginForIntegrationTesting;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.impl.OrderFactoryImpl;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.util.CheckoutHelper;
import com.elasticpath.test.util.Utils;

/**
 * Test {@link OrderFactoryImpl} functionality.
 */
public class OrderFactoryImplTest extends DbTestCase {

	@Autowired
	private CheckoutService checkoutService;

	@Autowired
	private ShopperService shopperService;

	@Autowired
	private CartDirector cartDirector;

	@Autowired
	private ShoppingItemDtoFactory shoppingItemDtoFactory;

	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@Autowired
	private CartOrderService cartOrderService;

	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	private Customer customer;

	private ShoppingContext shoppingContext;

	private CheckoutHelper checkoutHelper;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 *
	 * @throws Exception when exception.
	 */
	@Before
	public void setUp() throws Exception {
		customer = persisterFactory.getStoreTestPersister().createDefaultCustomer(scenario.getStore());
		shoppingContext = shoppingContextBuilder
				.withCustomer(customer)
				.withStoreCode(scenario.getStore().getCode())
				.build();
		shopperService.save(shoppingContext.getShopper());
		checkoutHelper = new CheckoutHelper(getTac());
	}

	@After
	public void tearDown() {
		PaymentProviderPluginForIntegrationTesting.resetCapabilities();
	}

	/**
	 * Integration test to check that the order factory populates the cart order GUID in a created order that has been placed in a FAILED state.
	 */
	@DirtiesDatabase
	@Test
	public void testOrderFactoryPopulatesCartOrderGuidOnOrderFailure() {
		final ShoppingCart shoppingCart = createShoppingCartWithScenarioStore();
		cartDirector.addItemToCart(shoppingCart, shoppingItemDtoFactory.createDto(createPhysicalProduct(), 1));

		PaymentProviderPluginForIntegrationTesting.addCapability(getClass(), ReserveCapability.class, request -> {
			throw new PaymentCapabilityRequestFailedException("internal message", "external message", false);
		});

		CartOrder cartOrder = cartOrderService.findByShoppingCartGuid(shoppingCart.getGuid());

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults results = checkoutHelper.checkoutCartWithoutHolds(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(),
				false);
		assertTrue("order should fail", results.isOrderFailed());

		Order order = results.getOrder();
		assertNotNull("There should have been an order on the checkout results", order);
		assertTrue("The order should be persistent", order.isPersisted());
		assertNull("The order cart order GUID should be null to allow the order to be resubmitted since there is a unique constraint on the "
				+ "CART_ORDER_GUID", order.getCartOrderGuid());

	}

	/**
	 * Integration test to check that the order factory populates the cart order GUID in a created order that was successful.
	 */
	@DirtiesDatabase
	@Test
	public void testOrderFactoryPopulatesCartOrderGuidOnOrderSuccess() {
		final ShoppingCart shoppingCart = createShoppingCartWithScenarioStore();
		cartDirector.addItemToCart(shoppingCart, shoppingItemDtoFactory.createDto(createPhysicalProduct(), 1));

		CartOrder cartOrder = cartOrderService.findByShoppingCartGuid(shoppingCart.getGuid());

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults results = checkoutHelper.checkoutCartWithoutHolds(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(),
				true);
		Order order = results.getOrder();

		assertNotNull("There should have been an order on the checkout results", order);

		order = checkoutHelper.finalizeOrder(order);

		assertEquals("The order should be in progress", OrderStatus.IN_PROGRESS, order.getStatus());
		assertTrue("The order should be persistent", order.isPersisted());
		assertEquals("The order cart order GUID should be set to the corresponding cart order GUID", cartOrder.getGuid(), order.getCartOrderGuid());
	}

	/**
	 * Creates the shopping cart with scenario store.
	 *
	 * @return the shopping cart
	 */
	private ShoppingCart createShoppingCartWithScenarioStore() {
		final ShoppingCart shoppingCart = TestShoppingCartFactoryForTestApplication.getInstance().createNewCartWithMemento(
				shoppingContext.getShopper(),
				scenario.getStore());

		final CustomerSession customerSession = shoppingContext.getCustomerSession();
		customerSession.setCurrency(Currency.getInstance(Locale.US));

		shoppingCart.initialize();
		shoppingCart.setBillingAddress(getBillingAddress());
		shoppingCart.setShippingAddress(getBillingAddress());
		shoppingCart.setSelectedShippingOption(scenario.getShippingOption());

		final ShoppingCartService shoppingCartService =
				getBeanFactory().getSingletonBean(ContextIdNames.SHOPPING_CART_SERVICE, ShoppingCartService.class);
		shoppingCartService.saveOrUpdate(shoppingCart);

		cartOrderService.createOrderIfPossible(shoppingCart);

		persisterFactory.getPaymentInstrumentPersister().persistPaymentInstrument(shoppingCart);

		return shoppingCart;
	}

	/**
	 * Initializes a mock billing address.
	 *
	 * @return the Address
	 */
	private Address getBillingAddress() {
		final Address billingAddress = new CustomerAddressImpl();
		billingAddress.setFirstName("Billy");
		billingAddress.setLastName("Bob");
		billingAddress.setCountry("CA");
		billingAddress.setStreet1("1295 Charleston Road");
		billingAddress.setCity("Vancouver");
		billingAddress.setSubCountry("BC");
		billingAddress.setZipOrPostalCode("94043");
		billingAddress.setGuid(Utils.uniqueCode("address"));
		return billingAddress;
	}

	private Product createPhysicalProduct() {
		return persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
				scenario.getCategory(),
				scenario.getWarehouse());
	}

	public EventOriginatorHelper getEventOriginatorHelper() {
		return getBeanFactory().getSingletonBean(ContextIdNames.EVENT_ORIGINATOR_HELPER, EventOriginatorHelper.class);
	}
}
