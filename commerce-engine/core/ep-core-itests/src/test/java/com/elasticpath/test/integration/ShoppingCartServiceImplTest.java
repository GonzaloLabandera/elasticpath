/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMementoHolder;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.persister.TestDataPersisterFactory;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * An integration test for the ShoppingCartServiceImpl, we are testing from a client's point of view with Spring and the Database up and running.
 */
public class ShoppingCartServiceImplTest extends BasicSpringContextTest {

	private SimpleStoreScenario scenario;
	private TestDataPersisterFactory persisterFactory;

	/** The main object under test. */
	@Autowired private ShoppingCartService shoppingCartService;

	@Autowired private ShopperService shopperService;
	@Autowired private CustomerSessionService customerSessionService;
	@Autowired private CartDirector cartDirector;
	@Autowired private CustomerService customerService;
	@Autowired private CartOrderService cartOrderService;
	@Autowired private ProductSkuLookup productSkuLookup;

	/**
	 * Get a reference to TestApplicationContext for use within the test. Setup scenarios.
	 * @throws Exception when exception
	 */
	@Before
	public void setUp() throws Exception {
		scenario = getTac().useScenario(SimpleStoreScenario.class);
		persisterFactory = getTac().getPersistersFactory();
	}

	/**
	 * A new non-persistent instance should be created when load(0) is called.
	 */
	@DirtiesDatabase
	@Test
	public void testLoadWithUidPk0() {
		final ShoppingCart shoppingCart = (ShoppingCart) shoppingCartService.getObject(0);
		assertThat(shoppingCartService.isPersisted(shoppingCart)).isFalse();
	}

	/**
	 * Should be identical to testLoadWithUidPk0 - implementation uses same code.
	 */
	@DirtiesDatabase
	@Test
	public void testGetObjectWithUidPk0() {
		final ShoppingCartImpl shoppingCart = (ShoppingCartImpl) shoppingCartService.getObject(0);
		assertThat(shoppingCart.getShoppingCartMemento().isPersisted()).isFalse();
		assertThat(shoppingCart.getShoppingCartMemento().getUidPk()).isEqualTo(0);
	}

	/**
	 * Create a new instance, store it and retrieve it.
	 */
	@DirtiesDatabase
	@Test
	public void testAddAndLoad() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		final ShoppingCart retrievedShoppingCart = shoppingCartService.findByGuid(shoppingCart.getGuid());
		assertThat(retrievedShoppingCart.getGuid()).isEqualTo(shoppingCart.getGuid());
		assertThat(retrievedShoppingCart)
			.as("Should be distinct objects")
			.isNotSameAs(shoppingCart);
	}

	/**
	 * Should be exactly the same as testAddAndLoad - the implementation uses the same code.
	 */
	@DirtiesDatabase
	@Test
	public void testAddAndGetObject() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		final ShoppingCart retrievedShoppingCart = shoppingCartService.findByGuid(shoppingCart.getGuid());
		assertThat(retrievedShoppingCart.getGuid()).isEqualTo(shoppingCart.getGuid());
		assertThat(shoppingCart)
			.as("Should be distinct objects")
			.isNotSameAs(retrievedShoppingCart);
	}

	/**
	 * Create a new instance, store it, modify it, and update it.
	 */
	@DirtiesDatabase
	@Test
	public void testAddItemToExistingCartObject() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final ShoppingItemDto dto = new ShoppingItemDto(product.getDefaultSku().getSkuCode(), 2);
		cartDirector.addItemToCart(shoppingCart, dto);
		final ShoppingCart updatedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);
		assertThat(updatedShoppingCart.getRootShoppingItems()).isNotEmpty();
		final ShoppingItem cartItem = updatedShoppingCart.getRootShoppingItems().iterator().next();
		assertThat(cartItem).isNotNull();
		assertThat(cartItem.getUidPk()).isGreaterThan(0);
		assertThat(cartItem.getSkuGuid()).isNotNull();
	}

	/**
	 * Create a new instance, store it, add a few items, and update it. Checks that the cart items were correctly persisted via cascade.
	 */
	@DirtiesDatabase
	@Test
	public void testAddMultipleItemsToExistingCartObject() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final String skuCode = product.getDefaultSku().getSkuCode();
		cartDirector.addItemToCart(shoppingCart, new ShoppingItemDto(skuCode, 2));
		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final String skuCode2 = product2.getDefaultSku().getSkuCode();
		cartDirector.addItemToCart(shoppingCart, new ShoppingItemDto(skuCode2, 1));

		final ShoppingCart updatedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);
		assertThat(updatedShoppingCart.getRootShoppingItems())
			.hasSize(2)
			.allSatisfy(cartItem -> {
				assertThat(cartItem).isNotNull();
				assertThat(cartItem.getUidPk()).isGreaterThan(0);
				assertThat(cartItem.getSkuGuid()).isNotNull();
			});
	}

	/**
	 * Test adding items to the cart, with changes being persisted in between additions. This is reasonably similar behaviour to what happens when a
	 * user adds things to their cart in the storefront.
	 */
	@DirtiesDatabase
	@Test
	public void testAddMultipleItemsUpdatingBetweenEach() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);


		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final String skuCode = product.getDefaultSku().getSkuCode();
		cartDirector.addItemToCart(shoppingCart, new ShoppingItemDto(skuCode, 2));
		final ShoppingCart updatedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		assertThat(updatedShoppingCart.getRootShoppingItems())
			.hasSize(1)
			.allSatisfy(cartItem -> {
				assertThat(cartItem).isNotNull();
				assertThat(cartItem.getUidPk()).isGreaterThan(0);
				assertThat(cartItem.getSkuGuid()).isNotNull();
			});

		final Product product2 = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final String skuCodeProduct2 = product2.getDefaultSku().getSkuCode();
		cartDirector.addItemToCart(shoppingCart, new ShoppingItemDto(skuCodeProduct2, 1));

		assertThat(updatedShoppingCart.getRootShoppingItems())
			.anySatisfy(item -> item.getSkuGuid().equals(product2.getDefaultSku().getGuid()));

		final ShoppingCart updatedTwiceShoppingCart = shoppingCartService.saveOrUpdate(updatedShoppingCart);

		assertThat(updatedTwiceShoppingCart.getRootShoppingItems())
			.hasSize(2)
			.allSatisfy(cartItem -> {
				assertThat(cartItem).isNotNull();
				assertThat(cartItem.getUidPk()).isGreaterThan(0);
				assertThat(cartItem.getSkuGuid()).isNotNull();
			});
	}

	/**
	 * Test adding an item to the cart, persisting the cart, and then changing the quantity of that item.
	 */
	@DirtiesDatabase
	@Test
	public void testAddItemUpdateThenChangeQuantity() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final String skuCode = product.getDefaultSku().getSkuCode();
		cartDirector.addItemToCart(shoppingCart, new ShoppingItemDto(skuCode, 1));

		final ShoppingCart updatedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		assertThat(updatedShoppingCart.getRootShoppingItems()).hasSize(1);
		ShoppingItem addedItem = updatedShoppingCart.getRootShoppingItems().iterator().next();
		assertThat(addedItem.isPersisted()).isTrue();
		assertThat(updatedShoppingCart.getCartItemById(addedItem.getUidPk()).getQuantity()).isEqualTo(1);
		final int newQuantity = 1 + 2;
		addedItem = cartDirector.updateCartItem(updatedShoppingCart, addedItem.getUidPk(), new ShoppingItemDto(skuCode, newQuantity));
		final ShoppingCart updatedTwiceShoppingCart = shoppingCartService.saveOrUpdate(updatedShoppingCart);

		assertThat(updatedTwiceShoppingCart.getRootShoppingItems()).hasSize(1);
		assertThat(updatedTwiceShoppingCart.getCartItemByGuid(addedItem.getGuid()).getQuantity()).isEqualTo(newQuantity);

	}

	/**
	 * Create a non-persistent {@link ShoppingCart} tied to the default store.
	 *
	 * @return the shopping cart
	 */
	private ShoppingCart createShoppingCart() {
		Currency currency = Currency.getInstance("USD");

		final Shopper shopper = shopperService.createAndSaveShopper(scenario.getStore().getCode());
		CustomerSession customerSession = customerSessionService.createWithShopper(shopper);
		customerSession.setCurrency(currency);
		customerSession.setLocale(Locale.ENGLISH);
		customerSession = customerSessionService.initializeCustomerSessionForPricing(customerSession, scenario.getStore().getCode(),
				currency);
		final ShoppingCart shoppingCart = shoppingCartService.findOrCreateDefaultCartByCustomerSession(customerSession);
		shoppingCart.setStore(scenario.getStore());
		((ShoppingCartMementoHolder) shoppingCart).getShoppingCartMemento().setGuid(Utils.uniqueCode("CART-"));
		return shoppingCart;
	}

	/**
	 * Create a non-persistent shopping cart tied to the default store.
	 *
	 * @return the gift certificate.
	 */
	private ShoppingCart createFullShoppingCart() {
		final ShoppingCart shoppingCart = createShoppingCart();
		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());
		final ShoppingItemDto dto = new ShoppingItemDto(product.getDefaultSku().getSkuCode(), 2);
		cartDirector.addItemToCart(shoppingCart, dto);
		// note that the cart isn't saved as the callers do this for us.
		return shoppingCart;
	}

	/**
	 * Test add multisku prod.
	 */
	@DirtiesDatabase
	@Test
	public void testAddMultiSkuToShoppingCart() {
		ShoppingCart shoppingCart = createFullShoppingCart();
		shoppingCart.clearItems();

		final Product multiSkuProduct = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
			scenario.getCategory(), scenario.getWarehouse());
		final ProductSku defaultSku = multiSkuProduct.getDefaultSku();

		final String skuCode = defaultSku.getSkuCode();
		cartDirector.addItemToCart(shoppingCart, new ShoppingItemDto(skuCode, 1));
		for (final SkuOption skuOption : multiSkuProduct.getProductType().getSkuOptions()) {
			final SkuOptionValue skuOptionValue = defaultSku.getSkuOptionValue(skuOption);
			assertThat(skuOptionValue).isNotNull();
			final String displayName = skuOptionValue.getDisplayName(Locale.US, false);
			assertThat(displayName).isNotBlank();
		}

		assertThat(shoppingCart.getRootShoppingItems().size()).isEqualTo(1);
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		final ShoppingCart updatedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		assertThat(updatedShoppingCart.getRootShoppingItems()).isNotEmpty();
		final ShoppingItem cartItem = updatedShoppingCart.getRootShoppingItems().iterator().next();
		assertThat(cartItem).isNotNull();
		assertThat(cartItem.getSkuGuid()).isNotNull();

		assertThat(updatedShoppingCart.getRootShoppingItems().size()).isEqualTo(1);

		for (final ShoppingItem shoppingCartItem : updatedShoppingCart.getRootShoppingItems()) {
			final ProductSku sku = productSkuLookup.findByGuid(shoppingCartItem.getSkuGuid());

			for (final SkuOption skuOption : sku.getProduct().getProductType().getSkuOptions()) {

				final SkuOptionValue skuOptionValue = sku.getSkuOptionValue(skuOption);
				assertThat(skuOptionValue).isNotNull();
				final String displayName = skuOptionValue.getDisplayName(Locale.US, false);
				assertThat(displayName).isNotBlank();
			}
		}
	}

	/** */
	@DirtiesDatabase
	@Test
	public void testFindByGuidWhenCartHasShoppingItems() {
		ShoppingCart cart = createFullShoppingCart();
		cart = shoppingCartService.saveOrUpdate(cart);
		final int totalNumberOfShoppingItems = 1;

		final ShoppingCart foundCart = shoppingCartService.findByGuid(cart.getGuid());
		assertThat(foundCart.getGuid()).isEqualTo(cart.getGuid());
		assertThat(foundCart.getRootShoppingItems()).hasSize(totalNumberOfShoppingItems);
	}

	/**
	 * Creates a Customer and two ShoppingCarts and asserts that the cart GUIDs can be retrieved.
	 */
	@DirtiesDatabase
	@Test
	public void testFindShoppingCartGuidsByCustomerAndStore() {
		final Customer savedCustomer = createSavedCustomer();
		final ShoppingCart savedShoppingCart1 = createSavedShoppingCart(savedCustomer);
		final ShoppingCart savedShoppingCart2 = createSavedShoppingCart(savedCustomer);

		List<String> guids = shoppingCartService.findByCustomerAndStore(savedCustomer.getGuid(), scenario.getStore().getCode());
		assertThat(guids)
			.containsExactlyInAnyOrder(savedShoppingCart1.getGuid(), savedShoppingCart2.getGuid());
	}

	/**
	 * Creates a Customer and two ShoppingCarts and asserts that the cart GUIDs cannot be retrieved
	 * because the Store Code does not exist.
	 */
	@DirtiesDatabase
	@Test
	public void testFindNoShoppingCartGuids() {
		final Customer savedCustomer = createSavedCustomer();
		createSavedShoppingCart(savedCustomer);
		createSavedShoppingCart(savedCustomer);

		// Store doesn't exist.
		List<String> guids = shoppingCartService.findByCustomerAndStore(savedCustomer.getGuid(), "BAD_STORE_CODE");
		assertThat(guids).isEmpty();
	}


	/**
	 * Tests the {@link ShoppingCartService#getShoppingCartLastModifiedDate(String)}.
	 */
	@DirtiesDatabase
	@Test
	public void testGetLastModifiedDate() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCartService.saveOrUpdate(shoppingCart);

		ShoppingCart retrievedShoppingCart = shoppingCartService.findByGuid(shoppingCart.getGuid());
		Date dateFromCart = retrievedShoppingCart.getLastModifiedDate();

		Date dateFromService = shoppingCartService.getShoppingCartLastModifiedDate(shoppingCart.getGuid());
		assertThat(dateFromService).isEqualTo(dateFromCart);
	}

	/**
	 * Verifies the {@link ShoppingCartService#getShoppingCartLastModifiedDate(String)} returns a <code>null</code> for an invalid cart GUID.
	 */
	@DirtiesDatabase
	@Test
	public void testGetLastModifiedDateInvalidGuid() {
		Date dateFromService = shoppingCartService.getShoppingCartLastModifiedDate("INVALID_GUID");
		assertThat(dateFromService).isNull();
	}

	/**
	 * Ensure delete all shopping carts by shopper uids correctly handles associated cart orders.
	 */
	@DirtiesDatabase
	@Test
	public void ensureDeleteAllShoppingCartsByShopperUidsCorrectlyHandlesAssociatedCartOrders() {
		final Customer savedCustomer = createSavedCustomer();
		ShoppingCart cartWithCartOrderToDelete = createSavedShoppingCart(savedCustomer);
		ShoppingCart cartWithoutCartOrderToKeep = createSavedShoppingCart(savedCustomer);
		ShoppingCart cartWithCartOrderToKeep = createSavedShoppingCart(savedCustomer);

		List<Long> shopperUidsForDeletion = ImmutableList.of(cartWithCartOrderToDelete.getShopper().getUidPk());
		shoppingCartService.deleteAllShoppingCartsByShopperUids(shopperUidsForDeletion);

		assertThat(shoppingCartService.shoppingCartExists(cartWithCartOrderToDelete.getGuid())).isFalse();
		assertThat(cartOrderService.findByShoppingCartGuid(cartWithCartOrderToDelete.getGuid())).isNull();
		assertThat(shoppingCartService.shoppingCartExists(cartWithCartOrderToKeep.getGuid())).isTrue();
		assertThat(cartOrderService.findByShoppingCartGuid(cartWithCartOrderToKeep.getGuid())).isNotNull();
		assertThat(shoppingCartService.shoppingCartExists(cartWithoutCartOrderToKeep.getGuid())).isTrue();
	}

	private Customer createSavedCustomer() {
		final Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		String email = "a@b.com";
		customer.setUserId(email);
		customer.setEmail(email);
		customer.setStoreCode(scenario.getStore().getCode());
		customer.setAnonymous(false);
		return customerService.add(customer);
	}

	private ShoppingCart createSavedShoppingCart(final Customer savedCustomer) {
		final Shopper savedShopper = shopperService.createAndSaveShopper(scenario.getStore().getCode());
		final CustomerSession customerSession = customerSessionService.createWithShopper(savedShopper);
		customerSession.setCurrency(Currency.getInstance("USD"));
		savedShopper.setCustomer(savedCustomer);
		shopperService.save(savedShopper);

		final ShoppingCart shoppingCart = shoppingCartService.findOrCreateDefaultCartByCustomerSession(customerSession);
		shoppingCart.setStore(scenario.getStore());
		((ShoppingCartMementoHolder) shoppingCart).getShoppingCartMemento().setGuid(Utils.uniqueCode("CART-"));
		final ShoppingCart persistedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		persisterFactory.getPaymentInstrumentPersister().persistPaymentInstrument(persistedShoppingCart);

		return persistedShoppingCart;
	}

	/**
	 * Create a new instance, store it and retrieve the storeCode.
	 */
	@DirtiesDatabase
	@Test
	public void testFindStoreCodeByCartGuid() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		String storeCode = shoppingCartService.findStoreCodeByCartGuid(shoppingCart.getGuid());
		assertThat(storeCode).isEqualTo(shoppingCart.getStore().getCode());
	}

	/**
	 * Create a new instance, store it and retrieve the cartGuid.
	 */
	@DirtiesDatabase
	@Test
	public void testFindShoppingCartGuidByCustomerSession() {
		ShoppingCart shoppingCart = createShoppingCart();
		shoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);

		String cartGuid = shoppingCartService.findDefaultShoppingCartGuidByCustomerSession(shoppingCart.getCustomerSession());
		assertThat(cartGuid).isEqualTo(shoppingCart.getGuid());
	}


}
