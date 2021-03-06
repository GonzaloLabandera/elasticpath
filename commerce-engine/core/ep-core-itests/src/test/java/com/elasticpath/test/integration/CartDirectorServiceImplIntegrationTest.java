/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.integration;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.customer.impl.CustomerImpl;
import com.elasticpath.domain.factory.TestCustomerSessionFactoryForTestApplication;
import com.elasticpath.domain.factory.TestShopperFactoryForTestApplication;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartMementoHolder;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.sellingchannel.director.CartDirectorService;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.util.Utils;

/**
 * Test cart director service integration test.
 */
public class CartDirectorServiceImplIntegrationTest extends DbTestCase {

	/**
	 * The main object under test.
	 */
	@Autowired
	private CartDirectorService cartDirectorService;

	@Autowired
	@Qualifier("shoppingCartService")
	private ShoppingCartService shoppingCartService;

	@Autowired
	private ShopperService shopperService;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private BeanFactory beanFactory;
	@Autowired
	private CartDirector cartDirector;
	@Autowired
	private ProductSkuLookup productSkuLookup;
	@Autowired
	private PricingSnapshotService pricingSnapshotService;

	/**
	 * Test cart refresh changes on price.
	 */
	@DirtiesDatabase
	@Test
	public void testCartRefreshChangesPrices() {
		ShoppingCart shoppingCart = createFullShoppingCart();

		final ShoppingItem item = shoppingCart.getRootShoppingItems().iterator().next();
		final ProductSku sku = productSkuLookup.findByGuid(item.getSkuGuid());
		final Product product = sku.getProduct();
		persisterFactory.getCatalogTestPersister().addOrUpdateProductBaseAmount(scenario.getCatalog(), product, BigDecimal.ONE, BigDecimal.ONE,
				BigDecimal.ONE, "USD");

		cartDirector.refresh(shoppingCart);

		final ShoppingCartPricingSnapshot cartPricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);

		final ShoppingItem secondItem = shoppingCart.getCartItemsBySkuGuid(item.getSkuGuid()).get(0);

		final ShoppingItemPricingSnapshot secondItemPricingSnapshot = cartPricingSnapshot.getShoppingItemPricingSnapshot(secondItem);
		assertEquals(BigDecimal.ONE.setScale(2), secondItemPricingSnapshot.getListUnitPrice().getAmount());
	}


	@DirtiesDatabase
	@Test
	public void testAddToCartIdentityRetainer() {
		ShoppingCart shoppingCart = createFullShoppingCart();

		final ShoppingItem item = shoppingCart.getRootShoppingItems().iterator().next();
		final String itemGuid = item.getGuid();

		assertEquals(2, item.getQuantity());


		//Add the same sku to cart again
		final ProductSku sku = productSkuLookup.findByGuid(item.getSkuGuid());
		final ShoppingItemDto dto = new ShoppingItemDto(sku.getSkuCode(), 1);

		final ShoppingItem newItem = cartDirectorService.addItemToCart(shoppingCart, dto);
		assertEquals(itemGuid, newItem.getGuid());
		assertEquals(3, newItem.getQuantity());
	}

	@DirtiesDatabase
	@Test
	public void verifyRemoveAllCartItemsRemovesAllCartItems() throws Exception {
		// Given a non-empty shopping cart
		ShoppingCart shoppingCart = createFullShoppingCart();

		// When the cart is cleared
		final ShoppingCart emptyShoppingCart = cartDirectorService.clearItems(shoppingCart);

		// Then the cart is empty
		assertThat(emptyShoppingCart.getRootShoppingItems(), empty());
	}

	/**
	 * Create a non-persistent shopping cart tied to the default store. Puts a SKU with quantity of two into the cart.
	 *
	 * @return the shopping cart
	 */
	private ShoppingCart createFullShoppingCart() {
		final Shopper shopper = TestShopperFactoryForTestApplication.getInstance().createNewShopperWithMemento();

		shopper.setCustomer(createPersistedCustomer("sharedID", "email", scenario.getStore()));
		shopper.setStoreCode(scenario.getStore().getCode());
		shopperService.save(shopper);

		final CustomerSession custSession = TestCustomerSessionFactoryForTestApplication.getInstance().createNewCustomerSessionWithContext(shopper);
		custSession.setCurrency(Currency.getInstance("USD"));

		final ShoppingCart shoppingCart = createShoppingCart(shopper);

		final Product product = persisterFactory.getCatalogTestPersister().createDefaultProductWithSkuAndInventory(scenario.getCatalog(),
				scenario.getCategory(), scenario.getWarehouse());

		addSkuToCart(shoppingCart, product.getDefaultSku().getSkuCode(), 2);

		// note that the cart isn't saved as the callers do this for us.
		return shoppingCartService.saveOrUpdate(shoppingCart);
	}

	private Customer createPersistedCustomer(final String sharedId, final String email, final Store store) {
		final Customer customer = beanFactory.getPrototypeBean(ContextIdNames.CUSTOMER, Customer.class);
		customer.setSharedId(sharedId);
		customer.setCustomerType(CustomerType.REGISTERED_USER);
		customer.setEmail(email);
		customer.setFirstName("Test");
		customer.setLastName("Test");
		customer.setStoreCode(store.getCode());

		return customerService.add(customer);
	}

	private void addSkuToCart(final ShoppingCart shoppingCart, final String skuCode, final int quantity) {
		final ShoppingItemDto dto = new ShoppingItemDto(skuCode, quantity);
		cartDirector.addItemToCart(shoppingCart, dto);
	}

	/**
	 * Create a non-persistent shopping cart tied to the default store.
	 *
	 * @return the shopping cart
	 */
	private ShoppingCart createShoppingCart(final Shopper shopper) {
		final ShoppingCart shoppingCart = getBeanFactory().getPrototypeBean(ContextIdNames.SHOPPING_CART, ShoppingCart.class);
		shoppingCart.setShopper(shopper);
		shoppingCart.setStore(scenario.getStore());
		((ShoppingCartMementoHolder) shoppingCart).getShoppingCartMemento().setGuid(Utils.uniqueCode("CART-"));
		shopper.setCurrentShoppingCart(shoppingCart);
		return shoppingCart;
	}
}
