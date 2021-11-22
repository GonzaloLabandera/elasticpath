/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.test.integration.service.shopper.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.common.dto.ShoppingItemDto;
import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerType;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.persister.ShoppingContextPersister;
import com.elasticpath.sellingchannel.director.CartDirector;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.shopper.impl.ShoppingCartsRemoverForShopperUpdates;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.integration.cart.AbstractCartIntegrationTestParent;
import com.elasticpath.test.persister.PaymentInstrumentPersister;
import com.elasticpath.test.persister.StoreTestPersister;

public class ShoppingCartsRemoverForShopperUpdatesTest extends AbstractCartIntegrationTestParent {

	private static final String RESERVE_DATA_KEY = "reserve-data";
	private static final String CHARGE_DATA_KEY = "charge-data";

	@Autowired
	private PricingSnapshotService pricingSnapshotService;
	@Autowired
	private TaxSnapshotService taxSnapshotService;
	@Autowired
	private CheckoutService checkoutService;
	@Autowired
	private ShoppingContextPersister shoppingContextPersister;
	@Autowired
	private ShoppingCartsRemoverForShopperUpdates testee;
	@Autowired
	private CustomerService customerService;
	@Autowired
	private ShoppingCartService shoppingCartService;
	@Autowired
	private PaymentInstrumentPersister paymentInstrumentPersister;
	@Autowired
	private CartDirector cartDirector;
	@Autowired
	private CheckoutTestCartBuilder checkoutTestCartBuilder;
	@Autowired
	private ShoppingContextBuilder shoppingContextBuilder;

	@DirtiesDatabase
	@Test
	public void shouldNotRemoveShoppingCartWhenAnonymousAndRegisteredShoppersAreSame() {
		ShoppingCart shoppingCart = createShoppingCart(CustomerType.REGISTERED_USER);

		// Both shoppers intentionally refer to the same object
		Shopper anonymousShopper = shoppingCart.getShopper();
		Shopper registeredShopper = shoppingCart.getShopper();

		testee.invalidateShopper(anonymousShopper, registeredShopper);

		ShoppingCart persistedCart = shoppingCartService.findByGuid(shoppingCart.getGuid());
		assertNotNull("Shopping cart shouldn't have been removed.", persistedCart);
	}

	/*
		Testing a case when anonymous has several carts (i.e. made a few purchases), all INACITVE but one and that one has an item in it.
		THere can be only 1 ACTIVE cart and multiple INACTIVE ones.
		Then anonymous transitions to a registered user.
		During transition, only ACTIVE cart is deleted.
	 */
	@DirtiesDatabase
	@Test
	public void testTransitionFromAnonymousWithMultipleCartsToRegisteredDeletesAnonymousCart() {
		//create a cart with pricing info
		ShoppingCart anonymousShoppingCart = createShoppingCart(CustomerType.SINGLE_SESSION_USER);

		Product nonShippableeProduct = persistNonShippableProductWithSku();
		final ShoppingItemDto dto = new ShoppingItemDto(nonShippableeProduct.getDefaultSku().getSkuCode(), 1);
		cartDirector.addItemToCart(anonymousShoppingCart, dto);
		final ShoppingCart updatedShoppingCart = shoppingCartService.saveOrUpdate(anonymousShoppingCart);

		//verify that cart is not empty
		assertThat(updatedShoppingCart.getRootShoppingItems()).isNotEmpty();

		final ShoppingItem cartItem = updatedShoppingCart.getRootShoppingItems().iterator().next();

		//sanity checks
		assertThat(cartItem.getUidPk()).isGreaterThan(0);
		assertEquals(nonShippableeProduct.getProductSkus().entrySet().iterator().next().getValue().getGuid(), cartItem.getSkuGuid());

		checkout(anonymousShoppingCart);

		//after checkout there must be 1 INACTIVE and 0 ACTIVE carts
		assertNumberOfAnonymousCartsByStatus(1, "INACTIVE");
		assertNumberOfAnonymousCartsByStatus(0, "ACTIVE");

		//find newly created cart
		ShoppingCart anonymousActiveShoppingCart = createShoppingCart(CustomerType.SINGLE_SESSION_USER);

		//and add an item
		cartDirector.addItemToCart(anonymousActiveShoppingCart, dto);

		final ShoppingCart updatedAnonymousActiveShoppingCart = shoppingCartService.saveOrUpdate(anonymousActiveShoppingCart);
		assertThat(updatedAnonymousActiveShoppingCart.getRootShoppingItems()).isNotEmpty();

		Customer registeredCustomer = createCustomer(CustomerType.REGISTERED_USER);
		Shopper registeredShopper = createShopper(registeredCustomer);

		testee.invalidateShopper(anonymousActiveShoppingCart.getShopper(), registeredShopper);

		//all anonymous carts should be deleted
		assertAllAnonymousCartsAreDeleted(anonymousActiveShoppingCart.getShopper().getUidPk());
	}

	private void assertNumberOfAnonymousCartsByStatus(final int expectedNumber, final String cartStatus) {
		List<String> result = getPersistenceEngine()
				.retrieve(String.format("select cart.guid from ShoppingCartMementoImpl cart where cart.status = '%s'", cartStatus));
		assertEquals(expectedNumber, result.size());
	}

	private void assertAllAnonymousCartsAreDeleted(final long shopperUid) {
		List<Long> result = getPersistenceEngine()
				.retrieve("select cart.uidPk from ShoppingCartMementoImpl cart where cart.shopperUid = ?1", shopperUid);
		assertThat(result).isEmpty();
	}

	private Customer createCustomer(final CustomerType customerType) {
		final StoreTestPersister storeTestPersister = getTac().getPersistersFactory().getStoreTestPersister();
		Customer customer = storeTestPersister.createDefaultCustomer(scenario.getStore());
		customer.setCustomerType(customerType);
		return customerService.update(customer);
	}

	private ShoppingCart createShoppingCart(CustomerType customerType) {
		Customer customer = createCustomer(customerType);
		ShoppingContext shoppingContext = shoppingContextBuilder
				.withStoreCode(scenario.getStore().getCode())
				.withCustomer(customer)
				.build();
		shoppingContextPersister.persist(shoppingContext);

		Shopper shopper = shoppingContext.getShopper();

		final ShoppingCart shoppingCart = super.createShoppingCart(shopper);
		shoppingCart.setBillingAddress(shopper.getCustomer().getPreferredBillingAddress());

		final ShoppingCart persistedShoppingCart = shoppingCartService.saveOrUpdate(shoppingCart);
		paymentInstrumentPersister.persistPaymentInstrument(persistedShoppingCart);
		return persistedShoppingCart;
	}

	private Order checkout(final ShoppingCart shoppingCart) {
		final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
		final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);
		return checkoutService.checkout(shoppingCart, taxSnapshot, shoppingCart.getShopper().getCustomerSession(), true).getOrder();
	}
}
