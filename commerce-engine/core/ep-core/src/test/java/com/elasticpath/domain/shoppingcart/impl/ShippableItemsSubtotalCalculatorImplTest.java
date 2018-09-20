/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.domain.shoppingcart.impl;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.order.OrderSku;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingItem;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductSkuLookup;
import com.elasticpath.service.shoppingcart.ShoppingItemSubtotalCalculator;
import com.elasticpath.service.shoppingcart.impl.ShippableItemsSubtotalCalculatorImpl;

public class ShippableItemsSubtotalCalculatorImplTest {

	private static final Currency CURRENCY = Currency.getInstance(Locale.CANADA);

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Mock
	private ProductSkuLookup productSkuLookup;

	@Mock
	private ShoppingCartPricingSnapshot cartPricingSnapshot;

	@Mock
	private ShoppingItemSubtotalCalculator shoppingItemSubtotalCalculator;

	private ShippableItemsSubtotalCalculatorImpl calculator;

	@Before
	public void setUp() {
		calculator = new ShippableItemsSubtotalCalculatorImpl();
		calculator.setProductSkuLookup(productSkuLookup);
		calculator.setShoppingItemSubtotalCalculator(shoppingItemSubtotalCalculator);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void verifyEmptyCollectionSentToCalculatorWhenNoCartItems() throws Exception {
		final Money zero = Money.valueOf(BigDecimal.ZERO, CURRENCY);

		final Collection<ShoppingItem> capturedItems = new ArrayList<>();
		context.checking(new Expectations() {
			{
				oneOf(shoppingItemSubtotalCalculator).calculate(
					(Collection<ShoppingItem>) with(any(Collection.class)),
					with(cartPricingSnapshot),
					with(CURRENCY));
				will(doAll(new CaptureItemsAction<>(capturedItems), returnValue(zero)));
			}
		});

		final Money actual = calculator.calculateSubtotalOfShippableItems(Collections.<ShoppingItem>emptyList(), cartPricingSnapshot, CURRENCY);
		assertEquals("The value returned should match the value returned by the shopping item calculator", zero, actual);
		assertThat("The collection should be empty", capturedItems, is(empty()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void verifyNonShippableShoppingItemsOmitted() throws Exception {
		final Money expectedSubtotal = Money.valueOf(new BigDecimal("3.00"), CURRENCY); // 1 + 2 = 3

		final ShoppingItem shoppingItem1 = createShippableShoppingItemWithTotal();
		final ShoppingItem shoppingItem2 = createShippableShoppingItemWithTotal();
		final ShoppingItem shoppingItem3 = createNonShippableShoppingItem();

		final Collection<ShoppingItem> items = ImmutableList.of(shoppingItem1, shoppingItem2, shoppingItem3);
		final Collection<ShoppingItem> capturedItems = new ArrayList<>();

		context.checking(new Expectations() {
			{
				oneOf(shoppingItemSubtotalCalculator).calculate(
					(Collection<ShoppingItem>) with(any(Collection.class)),
					with(cartPricingSnapshot),
					with(CURRENCY));
				will(doAll(new CaptureItemsAction<>(capturedItems), returnValue(expectedSubtotal)));
			}
		});

		final Money actual = calculator.calculateSubtotalOfShippableItems(items, cartPricingSnapshot, CURRENCY);

		assertThat(capturedItems, containsInAnyOrder(shoppingItem1, shoppingItem2));
		assertThat(capturedItems, not(contains(shoppingItem3)));
		assertEquals(expectedSubtotal, actual);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void verifyEmptyCollectionSentToCalculatorWhenNoOrderSkus() throws Exception {
		final Money zero = Money.valueOf(BigDecimal.ZERO, CURRENCY);

		final Collection<OrderSku> capturedItems = new ArrayList<>();
		context.checking(new Expectations() {
			{
				oneOf(shoppingItemSubtotalCalculator).calculate((Collection<OrderSku>) with(any(Collection.class)), with(CURRENCY));
				will(doAll(new CaptureItemsAction<>(capturedItems), returnValue(zero)));
			}
		});

		final Money actual = calculator.calculateSubtotalOfShippableItems(Collections.<OrderSku>emptyList(), CURRENCY);
		assertEquals("The value returned should match the value returned by the shopping item calculator", zero, actual);
		assertThat("The collection should be empty", capturedItems, is(empty()));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void verifySubtotalOmitsNonShippableOrderSkus() throws Exception {
		final Money orderSkuMoney1 = Money.valueOf(new BigDecimal("10.00"), CURRENCY);
		final Money expectedSubtotal = orderSkuMoney1;

		final OrderSku orderSku1 = createNonShippableOrderSku();
		final OrderSku orderSku2 = createShippableOrderSkuWithTotal();
		final Collection<OrderSku> capturedItems = new ArrayList<>();

		context.checking(new Expectations() {
			{
				oneOf(shoppingItemSubtotalCalculator).calculate((Collection<OrderSku>) with(any(Collection.class)), with(CURRENCY));
				will(doAll(new CaptureItemsAction<>(capturedItems), returnValue(expectedSubtotal)));
			}
		});

		final Money actual = calculator.calculateSubtotalOfShippableItems(ImmutableSet.of(orderSku1, orderSku2), CURRENCY);

		assertThat(capturedItems, contains(orderSku2));
		assertThat(capturedItems, not(contains(orderSku1)));
		assertEquals("The result should be the valkue returned by the shopping subtotal calculator", expectedSubtotal, actual);
	}

	private ShoppingItem createShippableShoppingItemWithTotal() {
		final ShoppingItem shoppingItem = context.mock(ShoppingItem.class, "Shopping Item " + UUID.randomUUID());

		context.checking(new Expectations() {
			{
				oneOf(shoppingItem).isShippable(productSkuLookup);
				will(returnValue(true));
			}
		});

		return shoppingItem;
	}

	private OrderSku createShippableOrderSkuWithTotal() {
		final OrderSku orderSku = context.mock(OrderSku.class, "Order Sku " + UUID.randomUUID());

		context.checking(new Expectations() {
			{
				oneOf(orderSku).isShippable(productSkuLookup);
				will(returnValue(true));
			}
		});

		return orderSku;
	}

	private ShoppingItem createNonShippableShoppingItem() {
		return createNonShippableShoppingItem(ShoppingItem.class);
	}

	private OrderSku createNonShippableOrderSku() {
		return createNonShippableShoppingItem(OrderSku.class);
	}

	private <T extends ShoppingItem> T createNonShippableShoppingItem(final Class<T> shoppingItemClass) {
		final T shoppingItem = context.mock(shoppingItemClass, "Shopping Item " + UUID.randomUUID());

		context.checking(new Expectations() {
			{
				atLeast(1).of(shoppingItem).isShippable(productSkuLookup);
				will(returnValue(false));
			}
		});

		return shoppingItem;
	}

	/**
	 * Action to allow capturing of parameters sent to a mock object.
	 *
	 * @param <T> ShoppingItem or a subclass thereof
	 */
	private final class CaptureItemsAction<T extends ShoppingItem> implements Action {

		private final Collection<T> items;

		/**
		 * Constructor that passes in the collection to populate.
		 *
		 * @param items the collection of items to populate with the capture results
		 */
		CaptureItemsAction(final Collection<T> items) {
			this.items = items;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object invoke(final Invocation invocation) throws Throwable {
			items.clear();
			items.addAll((Collection<T>) invocation.getParameter(0));
			return null;
		}

		@Override
		public void describeTo(final Description description) {
			description.appendText("captures a collection from the first parameter");

		}
	}

}