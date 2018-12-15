/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.domain.shoppingcart.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import static com.elasticpath.domain.misc.impl.DisplayNameComparatorImplTest.LOCALE;

import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartVisitor;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.domain.shoppingcart.ShoppingItemPricingSnapshot;
import com.elasticpath.domain.store.impl.StoreImpl;
import com.elasticpath.test.factory.TestCustomerSessionFactory;
import com.elasticpath.test.factory.TestShopperFactory;
import com.elasticpath.test.factory.TestShoppingCartFactory;

/**
 * Test class that verifies that {@link ShoppingCartImpl} and {@link ShoppingItemImpl} implementations handle {@link ShoppingCartVisitor}s
 * appropriately.
 */
public class ShoppingCartVisitorTest {

	private static final Currency CAD = Currency.getInstance(Locale.CANADA);

	private ShoppingCartImpl shoppingCart;

	@Before
	public void setUp() {
		final Shopper shopper = TestShopperFactory.getInstance().createNewShopperWithMemento();
		final CustomerSession customerSession = TestCustomerSessionFactory.getInstance().createNewCustomerSessionWithContext(shopper);
		final Customer customer = mock(Customer.class);
		customerSession.getShopper().setCustomer(customer);
		customerSession.setCurrency(CAD);
		customerSession.setLocale(LOCALE);

		shoppingCart = TestShoppingCartFactory.getInstance().createNewCartWithMemento(customerSession.getShopper(), new StoreImpl());
	}

	@Test
	public void verifyShoppingCartCountedOnce() {
		final ItemCounterShoppingCartVisitor visitor = new ItemCounterShoppingCartVisitor();

		shoppingCart.accept(visitor);

		assertThat(visitor.getShoppingCartCount(shoppingCart))
				.isEqualTo(1);
	}

	@Test
	public void verifyShoppingItemsEachCountedOnceFromShoppingCart() {
		final ShoppingItem rootLevelItem = createMockShoppingItem();
		final ShoppingItem oneLevelDeepItem = createMockShoppingItem();
		final ShoppingItem twoLevelsDeepItem = createMockShoppingItem();

		rootLevelItem.addChildItem(oneLevelDeepItem);
		oneLevelDeepItem.addChildItem(twoLevelsDeepItem);

		shoppingCart.getShoppingCartMemento().getAllItems().add(rootLevelItem);

		final ItemCounterShoppingCartVisitor visitor = new ItemCounterShoppingCartVisitor();

		shoppingCart.accept(visitor);

		final SoftAssertions softly = new SoftAssertions();

		softly.assertThat(visitor.getShoppingItemCount(rootLevelItem)).isEqualTo(1);
		softly.assertThat(visitor.getShoppingItemCount(oneLevelDeepItem)).isEqualTo(1);
		softly.assertThat(visitor.getShoppingItemCount(twoLevelsDeepItem)).isEqualTo(1);

		softly.assertAll();
	}

	@Test
	public void verifyShoppingItemsEachCountedExactlyOnceFromParent() {
		final ShoppingItem rootLevelItem = createMockShoppingItem();
		final ShoppingItem oneLevelDeepItem = createMockShoppingItem();
		final ShoppingItem twoLevelsDeepItem = createMockShoppingItem();

		rootLevelItem.addChildItem(oneLevelDeepItem);
		oneLevelDeepItem.addChildItem(twoLevelsDeepItem);

		final ItemCounterShoppingCartVisitor visitor = new ItemCounterShoppingCartVisitor();

		rootLevelItem.accept(visitor, null);

		final SoftAssertions softly = new SoftAssertions();

		softly.assertThat(visitor.getShoppingItemCount(rootLevelItem)).isEqualTo(1);
		softly.assertThat(visitor.getShoppingItemCount(oneLevelDeepItem)).isEqualTo(1);
		softly.assertThat(visitor.getShoppingItemCount(twoLevelsDeepItem)).isEqualTo(1);

		softly.assertAll();
	}

	private ShoppingItem createMockShoppingItem() {
		final ShoppingItem shoppingItem = new ShoppingItemImpl();

		shoppingItem.setGuid(UUID.randomUUID().toString());

		return shoppingItem;
	}

	private static class ItemCounterShoppingCartVisitor implements ShoppingCartVisitor {

		private final Multiset<ShoppingCart> shoppingCartCounter = HashMultiset.create();
		private final Multiset<ShoppingItem> shoppingItemCounter = HashMultiset.create();

		@Override
		public void visit(final ShoppingCart cart) {
			shoppingCartCounter.add(cart);
		}

		@Override
		public void visit(final ShoppingItem item, final ShoppingItemPricingSnapshot pricingSnapshot) {
			shoppingItemCounter.add(item);
		}

		protected int getShoppingCartCount(final ShoppingCart shoppingCart) {
			return shoppingCartCounter.count(shoppingCart);
		}

		protected int getShoppingItemCount(final ShoppingItem shoppingItem) {
			return shoppingItemCounter.count(shoppingItem);
		}

	}

}