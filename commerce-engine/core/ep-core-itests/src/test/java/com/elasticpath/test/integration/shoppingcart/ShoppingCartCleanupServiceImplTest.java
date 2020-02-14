/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.test.integration.shoppingcart;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMemento;
import com.elasticpath.domain.shoppingcart.ShoppingCartMementoHolder;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.WishList;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartStatus;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.director.CartDirectorService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.customer.CustomerSessionService;
import com.elasticpath.service.misc.impl.MockTimeServiceImpl;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartCleanupService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.ShoppingItemService;
import com.elasticpath.service.shoppingcart.WishListService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.PaymentInstrumentPersister;
import com.elasticpath.test.persister.testscenarios.AbstractScenario;
import com.elasticpath.test.persister.testscenarios.ProductsScenario;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * Integration testing when dealing with shopping cart abandonment issues.
 */
@ContextConfiguration(locations="/integration-mock-time-service.xml")
public class ShoppingCartCleanupServiceImplTest extends BasicSpringContextTest {

	private static final int PRODUCT_AGE = 120;
	private static final int EXPECTED_MAX_CART_HISTORY = 60;

	private static final int EXPECTED_MAX_RESULTS = 1000;

	private static final int EXPECTED_TWO_CARTS_REMOVED = 2;

	private static final String SKU_CODE = "0349B004";

	@Autowired private ShoppingCartCleanupService shoppingCartCleanupService;

	@Autowired private ShoppingCartService shoppingCartService;

	@Autowired private ShopperService shopperService;

	@Autowired private CustomerService customerService;

	@Autowired private CustomerSessionService customerSessionService;

	private SimpleStoreScenario storeScenario;

	@Autowired private CartDirectorService cartDirectorService;

	@Autowired private ShoppingItemService shoppingItemService;

	@Autowired private WishListService wishListService;

	@Autowired @Qualifier(ContextIdNames.TIME_SERVICE)
	private MockTimeServiceImpl mockTimeService;

	@Autowired private PaymentInstrumentPersister paymentInstrumentPersister;

	private Date priorDate;
	private Customer customer;

	/**
	 * Get a reference to TestApplicationContext for use within the test. <br>
	 * Setup scenarios.
	 * @throws Exception on exception
	 */
	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		Date productCreationDate = getAdjustedDate(new Date(), PRODUCT_AGE);
		priorDate = getAdjustedDate(new Date(), EXPECTED_MAX_CART_HISTORY);

		mockTimeService.setCurrentTime(productCreationDate);
		Map<Class<? extends AbstractScenario>, AbstractScenario> scenarios =
				getTac().useScenarios((List) Arrays.asList(SimpleStoreScenario.class, ProductsScenario.class));
		storeScenario = (SimpleStoreScenario) scenarios.get(SimpleStoreScenario.class);
		mockTimeService.setCurrentTime(new Date());

		customer = createSavedCustomer();
	}

	/**
	 * Test that the max history applied to a date removes the expected number of shopping carts.
	 */
	@DirtiesDatabase
	@Test
	public void testCandidateRemovalByDateBoundaries() {
		Date afterRemovalDate = new Date();
		Date equalsBoundaryDate = getAdjustedDate(afterRemovalDate, EXPECTED_MAX_CART_HISTORY);
		Date beforeRemovalDate = getAdjustedDate(afterRemovalDate, EXPECTED_MAX_CART_HISTORY + 1);

		// create shopping carts around the max history date boundaries
		Shopper shopperAfterRemovalDate = addShoppingCart(afterRemovalDate);
		Shopper shopperEqualsRemovalDate = addShoppingCart(equalsBoundaryDate);
		Shopper shopperBeforeRemovalDate = addShoppingCart(beforeRemovalDate);

		int removed = shoppingCartCleanupService.deleteAbandonedShoppingCarts(equalsBoundaryDate, EXPECTED_MAX_RESULTS);
		assertThat(removed)
			.as("There should have been two shopping carts removed.")
			.isEqualTo(EXPECTED_TWO_CARTS_REMOVED);

		ShoppingCart updatedCartAfterRemovalDate = shoppingCartService.findOrCreateByShopper(shopperAfterRemovalDate);
		assertThat(shopperAfterRemovalDate.getCurrentShoppingCart().getGuid())
			.as("The non-candidate shopping cart should still exist in database.")
			.isEqualTo(updatedCartAfterRemovalDate.getGuid());

		ShoppingCart updatedCartEqualsRemovalDate = shoppingCartService.findOrCreateByShopper(shopperEqualsRemovalDate);
		assertThat(updatedCartEqualsRemovalDate.getGuid())
			.as("The candidate shopping cart which is equals to the removal date should not exist in database. A new cart was created.")
			.isNotEqualTo(shopperEqualsRemovalDate.getCurrentShoppingCart().getGuid());

		ShoppingCart updatedCartBeforeRemovalDate = shoppingCartService.findOrCreateByShopper(shopperBeforeRemovalDate);
		assertThat(updatedCartBeforeRemovalDate.getGuid())
			.as("The candidate shopping cart which is before the removal date should not exist in database. A new cart was created.")
			.isNotEqualTo(shopperBeforeRemovalDate.getCurrentShoppingCart().getGuid());
	}

	/**
	 * Test that the the batch size limits the number of abandoned carts.
	 */
	@DirtiesDatabase
	@Test
	public void testCandidateRemovalByBatchSizeAlsoNoticeRemovesOldestAbandonedCartsFirst() {
		Date afterRemovalDate = new Date();
		Date equalsBoundaryDate = getAdjustedDate(afterRemovalDate, EXPECTED_MAX_CART_HISTORY);
		Date beforeRemovalDate = getAdjustedDate(afterRemovalDate, EXPECTED_MAX_CART_HISTORY + 1);

		// create shopping carts around the max history date boundaries
		Shopper shopperAfterRemovalDate = addShoppingCart(afterRemovalDate);
		Shopper shopperEqualsRemovalDate = addShoppingCart(equalsBoundaryDate);
		Shopper shopperBeforeRemovalDate = addShoppingCart(beforeRemovalDate);

		int removed = shoppingCartCleanupService.deleteAbandonedShoppingCarts(equalsBoundaryDate, 1);
		assertThat(removed)
			.as("There should have been one shopping cart removed.")
			.isEqualTo(1);

		ShoppingCart updatedCartAfterRemovalDate = shoppingCartService.findOrCreateByShopper(shopperAfterRemovalDate);
		assertThat(shopperAfterRemovalDate.getCurrentShoppingCart().getGuid())
			.as("The non-candidate shopping cart should still exist in database.")
			.isEqualTo(updatedCartAfterRemovalDate.getGuid());

		ShoppingCart updatedCartEqualsRemovalDate = shoppingCartService.findOrCreateByShopper(shopperEqualsRemovalDate);
		assertThat(shopperEqualsRemovalDate.getCurrentShoppingCart().getGuid())
			.as("The candidate shopping cart which is equals to the removal date should still exist in database.")
			.isEqualTo(updatedCartEqualsRemovalDate.getGuid());

		ShoppingCart updatedCartBeforeRemovalDate = shoppingCartService.findOrCreateByShopper(shopperBeforeRemovalDate);
		assertThat(updatedCartBeforeRemovalDate.getGuid())
			.as("The candidate shopping cart which is before the removal date should not exist in database. A new cart was created.")
			.isNotEqualTo(shopperBeforeRemovalDate.getCurrentShoppingCart().getGuid());
	}

	/**
	 * Test that if an abandoned shopping cart is removed, that it cascades to remove dependent shopping cart items as well.
	 */
	@DirtiesDatabase
	@Test
	public void testCandidateRemovalRemovesShoppingItems() {
		Date cartCreationTime = priorDate;

		mockTimeService.setCurrentTime(cartCreationTime);
		final Shopper shopper = shopperService.createAndSaveShopper(storeScenario.getStore().getCode());
		shopper.setCustomer(customer);
		shopperService.save(shopper);

		ShoppingCart shoppingCart = createShoppingCart(shopper);
		ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 1);
		ShoppingItem shoppingItem = cartDirectorService.addItemToCart(shoppingCart, dto);

		mockTimeService.setCurrentTime(new Date());  // Just for clarity here
		int removed = shoppingCartCleanupService.deleteAbandonedShoppingCarts(new Date(cartCreationTime.getTime() + 1000), 100);
		assertThat(removed)
			.as("There should have been one shopping cart removed.")
			.isEqualTo(1);

		ShoppingItem actualShoppingItem = shoppingItemService.findByGuid(shoppingItem.getGuid(), null);
		assertThat(actualShoppingItem)
			.as("This shopping item should have been cascade deleted when the shopping cart was removed.")
			.isNull();
	}

	/**
	 * Test that if an abandoned shopping cart is removed, that wish list items can still be accessed by shopper.
	 */
	@DirtiesDatabase
	@Test
	public void testCandidateRemovalDoesNotAdverselyAffectShopperWishLists() {
		Date afterRemovalDate = new Date();
		Date equalsBoundaryDate = getAdjustedDate(afterRemovalDate, EXPECTED_MAX_CART_HISTORY);

		Shopper shopperEqualsRemovalDate = addShoppingCart(equalsBoundaryDate);
		final Store store = storeScenario.getStore();
		ShoppingItem shoppingItem = cartDirectorService.addSkuToWishList(SKU_CODE, shopperEqualsRemovalDate, store);

		int removed = shoppingCartCleanupService.deleteAbandonedShoppingCarts(equalsBoundaryDate, 1);
		assertThat(removed)
			.as("There should have been one shopping cart removed.")
			.isEqualTo(1);

		WishList actualWishList = wishListService.findOrCreateWishListByShopper(shopperEqualsRemovalDate);
		assertThat(actualWishList)
			.as("This wishlist should not be null.")
			.isNotNull();

		assertThat(actualWishList.getAllItems())
			.as("This wishlist should have the same amount of items as previously held.")
			.hasSize(1);

		assertThat(actualWishList.getAllItems().get(0).getSkuGuid())
			.as("This wishlist should contain the same product sku.")
			.isEqualTo(shoppingItem.getSkuGuid());
	}

	/**
	 * Test that the the batch size limits the number of inactive carts.
	 */
	@DirtiesDatabase
	@Test
	public void testShouldRemovePreDefinedNumberOfInactiveCarts() {
		Shopper shopperWithActiveCart = createShoppingCartWithStatus(ShoppingCartStatus.ACTIVE);
		Shopper shopperWithInactiveCart = createShoppingCartWithStatus(ShoppingCartStatus.INACTIVE);

		int removed = shoppingCartCleanupService.deleteInactiveShoppingCarts(1);
		assertThat(removed)
			.as("There should have been one shopping cart removed.")
			.isEqualTo(1);

		boolean inactiveCartExists = shoppingCartService.shoppingCartExists(shopperWithInactiveCart.getCurrentShoppingCart().getGuid());
		assertThat(inactiveCartExists)
			.as("Inactive cart must not exist.")
			.isFalse();

		boolean activeCartExists = shoppingCartService.shoppingCartExists(shopperWithActiveCart.getCurrentShoppingCart().getGuid());
		assertThat(activeCartExists)
			.as("Active cart must exist.")
			.isTrue();
	}

	/**
	 * Test that if an inactive shopping cart is removed, that it cascades to remove dependent shopping cart items as well.
	 */
	@DirtiesDatabase
	@Test
	public void testShouldRemoveInactiveCartsAndShoppingItems() {

		Shopper shopperWithInactiveCart = createShoppingCartWithStatus(ShoppingCartStatus.INACTIVE);
		ShoppingItemDto dto = new ShoppingItemDto(SKU_CODE, 1);
		ShoppingItem shoppingItem = cartDirectorService.addItemToCart(shopperWithInactiveCart.getCurrentShoppingCart(), dto);

		int removed = shoppingCartCleanupService.deleteInactiveShoppingCarts(1);
		assertThat(removed)
			.as("There should have been one shopping cart removed.")
			.isEqualTo(1);

		boolean inactiveCartExists = shoppingCartService.shoppingCartExists(shopperWithInactiveCart.getCurrentShoppingCart().getGuid());
		assertThat(inactiveCartExists)
			.as("Inactive cart must not exist.")
			.isFalse();

		ShoppingItem actualShoppingItem = shoppingItemService.findByGuid(shoppingItem.getGuid(), null);
		assertThat(actualShoppingItem)
			.as("This shopping item should have been cascade deleted when the shopping cart was removed.")
			.isNull();
	}

	/**
	 * Test that if an inactive shopping cart is removed, that wish list items can still be accessed by shopper.
	 */
	@DirtiesDatabase
	@Test
	public void testShopperWishListsShouldNotBeAffectedByRemovalOfInactiveCarts() {

		Shopper shopperWithInactiveCart = createShoppingCartWithStatus(ShoppingCartStatus.INACTIVE);

		final Store store = storeScenario.getStore();
		ShoppingItem shoppingItem = cartDirectorService.addSkuToWishList(SKU_CODE, shopperWithInactiveCart, store);

		int removed = shoppingCartCleanupService.deleteInactiveShoppingCarts(1);
		assertThat(removed)
			.as("There should have been one shopping cart removed.")
			.isEqualTo(1);

		WishList actualWishList = wishListService.findOrCreateWishListByShopper(shopperWithInactiveCart);
		assertThat(actualWishList)
			.as("This wishlist should not be null.")
			.isNotNull();

		assertThat(actualWishList.getAllItems())
			.as("This wishlist should have the same amount of items as previously held.")
			.hasSize(1);

		assertThat(actualWishList.getAllItems().get(0).getSkuGuid())
			.as("This wishlist should contain the same product sku.")
			.isEqualTo(shoppingItem.getSkuGuid());
	}

	private Date getAdjustedDate(final Date now, final int adjustment) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);
		calendar.add(Calendar.DAY_OF_YEAR, - adjustment);
		return calendar.getTime();
	}

	private Shopper addShoppingCart(final Date lastModifiedDate) {
		mockTimeService.setCurrentTime(lastModifiedDate);

		try {
			final Shopper shopper = shopperService.createAndSaveShopper(storeScenario.getStore().getCode());
			shopper.setCustomer(customer);
			shopperService.save(shopper);

			ShoppingCart cart = createShoppingCart(shopper);
			ShoppingCart updatedCart = shoppingCartService.saveOrUpdate(cart);
			shopper.setCurrentShoppingCart(updatedCart);

			return shopper;
		} finally {
			mockTimeService.setCurrentTime(new Date());
		}
	}

	private Shopper createShoppingCartWithStatus(final ShoppingCartStatus cartStatus) {
		final Shopper shopper = shopperService.createAndSaveShopper(storeScenario.getStore().getCode());
		shopper.setCustomer(customer);
		shopperService.save(shopper);

		ShoppingCart shoppingCart = createShoppingCart(shopper, cartStatus);
		shopper.setCurrentShoppingCart(shoppingCart);

		return shopper;
	}

	private Customer createSavedCustomer() {
		final Customer customer = getBeanFactory().getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		String email = "a@b.com";
		customer.setUserId(email);
		customer.setEmail(email);
		customer.setStoreCode(storeScenario.getStore().getCode());
		customer.setAnonymous(false);
		return customerService.add(customer);
	}

	/**
	 * Create a non-persistent {@link ShoppingCart} tied to the default store.
	 *
	 * @return the shopping cart
	 */
	private ShoppingCart createShoppingCart(final Shopper shopper) {
		return createShoppingCart(shopper, ShoppingCartStatus.ACTIVE);
	}

	private ShoppingCart createShoppingCart(final Shopper shopper, final ShoppingCartStatus cartStatus) {
		Currency currency = Currency.getInstance("USD");

		CustomerSession customerSession = customerSessionService.createWithShopper(shopper);
		customerSession.setCurrency(currency);
		customerSession.setLocale(Locale.ENGLISH);
		customerSession = customerSessionService
				.initializeCustomerSessionForPricing(customerSession, storeScenario.getStore().getCode(), currency);

		final ShoppingCart shoppingCart = shoppingCartService.findOrCreateDefaultCartByCustomerSession(customerSession);
		shoppingCart.setStore(storeScenario.getStore());

		final ShoppingCartMemento memento = ((ShoppingCartMementoHolder)shoppingCart).getShoppingCartMemento();
		memento.setGuid(Utils.uniqueCode("CART-"));
		memento.setStatus(cartStatus);

		shopper.setCurrentShoppingCart(shoppingCart);

		final ShoppingCart persistedCart = shoppingCartService.saveOrUpdate(shoppingCart);
		paymentInstrumentPersister.persistPaymentInstrument(persistedCart);

		return persistedCart;
	}

}
