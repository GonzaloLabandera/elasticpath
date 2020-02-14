/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.checkoutaction;

import static com.elasticpath.commons.constants.ContextIdNames.CART_ORDER_SERVICE;
import static com.elasticpath.commons.constants.ContextIdNames.SHOPPING_CART_SERVICE;
import static org.junit.Assert.assertFalse;

import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
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
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.catalog.ProductService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.service.shoppingcart.actions.exception.CheckoutValidationException;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * Test failing payments on checkout.
 */
public class MissingShippingInformationCheckoutTest extends BasicSpringContextTest {

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

	private SimpleStoreScenario scenario;

	private ShoppingCart shoppingCart;

	private Customer anonymousCustomer;
	private ShoppingContext shoppingContext;

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
	}

	/**
	 * Integration test to check the happy path for check out of a valid cart with physical goods.
	 */
	@DirtiesDatabase
	@Test
	public void checkoutValidCartWithPhysicalGoods() {
		setupValidShippingOptionOnCart(shoppingCart);
		cartDirector.addItemToCart(shoppingCart, shoppingItemDtoFactory.createDto(createPhysicalProduct(), 1));

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults checkoutResult = checkoutService.checkout(shoppingCart,
				taxSnapshot,
				shoppingContext.getCustomerSession(),
				false);

		assertFalse("Order should succeed.", checkoutResult.isOrderFailed());
	}

	/**
	 * Integration test to check the happy path for check out of a valid cart with non-physical goods.
	 */
	@Ignore("FIXME: Checkout with electronic shipments will fail intermittently due to @PrePersist and @PostPersist doing too much")
	@DirtiesDatabase
	@Test
	public void checkoutValidCartWithNonPhysicalGoods() {
		setupValidShippingOptionOnCart(shoppingCart);
		cartDirector.addItemToCart(shoppingCart, shoppingItemDtoFactory.createDto(createNonShippableProduct(), 1));

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		CheckoutResults checkoutResult = checkoutService.checkout(shoppingCart,
				taxSnapshot,
				shoppingContext.getCustomerSession(),
				false);

		assertFalse("Order should succeed.", checkoutResult.isOrderFailed());
	}

	/**
	 * Checkout cart with a selected shipping option, but missing shipping address.
	 * <p>
	 * This is an unlikely scenario as a shipping address is required to obtain the valid shipping options, and so select one.
	 * However this test ensures that the checkout process will absolutely ensure that a shipping address has been set
	 * in case it is has been cleared somehow after a shipping option has been selected.
	 */
	@DirtiesDatabase
	@Test(expected = CheckoutValidationException.class)
	public void checkoutCartWithMissingShippingAddress() {
		// Given I have set up a shipping option on the cart, added an item to cart, and calculated pricing
		setupValidShippingOptionOnCart(shoppingCart);
		cartDirector.addItemToCart(shoppingCart, shoppingItemDtoFactory.createDto(createPhysicalProduct(), 1));

		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

		// When I set the shipping address to null and checkout
		shoppingCart.setShippingAddress(null);
		checkoutService.checkout(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), true);

		// Then I get a MissingShippingAddressException thrown
	}

	private ShoppingCart createEmptyShoppingCartWithScenarioStore() {
		final ShoppingCart shoppingCart = TestShoppingCartFactoryForTestApplication.getInstance().createNewCartWithMemento(
				shoppingContext.getShopper(), scenario.getStore());

		final CustomerSession customerSession = shoppingContext.getCustomerSession();
		customerSession.setCurrency(Currency.getInstance(Locale.US));

		// FIXME: Remove once shoppingCart does not delegate back to CustomerSession.
		shoppingCart.setCustomerSession(customerSession);
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

	private Product createNonShippableProduct() {
		Product digitalProduct = createPhysicalProduct();
		digitalProduct.getDefaultSku().setShippable(false);

		ProductService prodService = getBeanFactory().getSingletonBean(ContextIdNames.PRODUCT_SERVICE, ProductService.class);
		digitalProduct = prodService.saveOrUpdate(digitalProduct);
		return digitalProduct;
	}
}
