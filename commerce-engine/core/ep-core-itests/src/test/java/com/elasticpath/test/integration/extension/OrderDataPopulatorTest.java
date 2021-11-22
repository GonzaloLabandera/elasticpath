/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.test.integration.extension;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.SHOPPING_CART_SERVICE;
import static org.junit.Assert.assertEquals;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.sellingchannel.ShoppingItemDtoFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.Address;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerAddress;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.factory.TestShoppingCartFactoryForTestApplication;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderStatus;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.shoppingcart.extension.impl.CartModifierOrderDataPopulator;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.CheckoutHelper;
import com.elasticpath.test.util.Utils;
import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.impl.XPFExtensionSelectorAny;
import com.elasticpath.xpf.impl.XPFInMemoryExtensionResolverImpl;

/**
 * Test failing payments on checkout.
 */
public class OrderDataPopulatorTest extends BasicSpringContextTest {

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
	private PricingSnapshotService pricingSnapshotService;

	@Autowired
	private TaxSnapshotService taxSnapshotService;

	@Autowired
	private XPFInMemoryExtensionResolverImpl resolver;

	private SimpleStoreScenario scenario;
	private CheckoutHelper checkoutHelper;

	private ShoppingCart shoppingCart;

	private Customer anonymousCustomer;
	private ShoppingContext shoppingContext;

	private static String EXTENSION = CartModifierOrderDataPopulator.class.getName();

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 *
	 * @throws Exception when exception
	 */
	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SimpleStoreScenario.class);

		final Store store = scenario.getStore();
		anonymousCustomer = getTac().getPersistersFactory().getStoreTestPersister().createDefaultCustomer(store);
		shoppingContext = shoppingContextBuilder
				.withCustomer(anonymousCustomer)
				.withStoreCode(store.getCode())
				.build();
		shopperService.save(shoppingContext.getShopper());
		shoppingCart = createEmptyShoppingCartWithScenarioStore();

		checkoutHelper = new CheckoutHelper(getTac());
	}

	/**
	 * Integration test to check cart data is copied to order data.
	 */
	@DirtiesDatabase
	@Test
	public void testCheckoutOrderDataPopulatorWithDefaultExtensions() {
		//Given a cart with modifier fields
		shoppingCart.getModifierFields().putIfAbsent("cartData", "cartValue");
		Map<String, String> expectedMap = shoppingCart.getModifierFields().getMap();

		//When a order is placed
		Order order = createOrder();

		//Then the cart data is copied to order data
		Map<String, String> actualMap = order.getModifierFields().getMap();
		assertEquals("Order data map not as expected:", expectedMap, actualMap);
	}

	/**
	 * Integration test to check cart data is copied to order data.
	 */
	@DirtiesDatabase
	@Test
	public void testCheckoutOrderDataPopulatorWithMinimalExtensions() {
		//Given a cart with modifier fields
		shoppingCart.getModifierFields().putIfAbsent("cartData", "cartValue");

		//When the CartModifierOrderDataPopulator is unloaded and a order is placed
		resolver.removeExtensionFromSelector(EXTENSION, null, XPFExtensionPointEnum.ORDER_DATA_POPULATOR,
				new XPFExtensionSelectorAny());
		Order order = createOrder();

		//Then the order data has not been populated
		Map<String, String> map = order.getModifierFields().getMap();
		assertEquals("Order data map not as expected:", 0, map.size());
	}

	private Order createOrder() {
		setupValidShippingOptionOnCart(shoppingCart);
		cartDirector.addItemToCart(shoppingCart, shoppingItemDtoFactory.createDto(createPhysicalProduct(), 1));

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		Order order = checkoutHelper.checkoutCartAndFinalizeOrderWithoutHolds(shoppingCart,
				taxSnapshot,
				false);

		assertEquals("Order should succeed.", order.getStatus(), OrderStatus.IN_PROGRESS);
		return order;
	}

	private ShoppingCart createEmptyShoppingCartWithScenarioStore() {
		final ShoppingCart shoppingCart = TestShoppingCartFactoryForTestApplication.getInstance().createNewCartWithMemento(
				shoppingContext.getShopper(), scenario.getStore());

		final CustomerSession customerSession = shoppingContext.getCustomerSession();
		customerSession.setCurrency(Currency.getInstance(Locale.US));

		shoppingCart.setBillingAddress(getAddress());
		shoppingCart.setShippingAddress(getAddress());

		final ShoppingCartService shoppingCartService = getBeanFactory().getSingletonBean(SHOPPING_CART_SERVICE, ShoppingCartService.class);
		shoppingCartService.saveOrUpdate(shoppingCart);

		final CartOrderService cartOrderService = getBeanFactory().getSingletonBean(CART_ORDER_SERVICE, CartOrderService.class);
		cartOrderService.createOrderIfPossible(shoppingCart);

		getTac().getPersistersFactory().getPaymentInstrumentPersister().persistPaymentInstrument(shoppingCart);

		return shoppingCart;
	}

	private void setupValidShippingOptionOnCart(final ShoppingCart shoppingCart) {
		shoppingCart.setSelectedShippingOption(scenario.getShippingOption());
	}

	private Address getAddress() {
		final Address address = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER_ADDRESS, CustomerAddress.class);
		address.setFirstName("Billy");
		address.setLastName("Bob");
		address.setCountry("CA");
		address.setStreet1("1295 Charleston Road");
		address.setCity("Vancouver");
		address.setSubCountry("BC");
		address.setZipOrPostalCode("V5N1T8");
		address.setGuid(Utils.uniqueCode("address"));
		return address;
	}

	private Product createPhysicalProduct() {
		return getTac().getPersistersFactory().getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());
	}
}
