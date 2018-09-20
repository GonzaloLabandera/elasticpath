/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.shoppingcart.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.impl.AbstractItemData;
import com.elasticpath.domain.shoppingcart.PriceCalculator;
import com.elasticpath.money.Money;
import com.elasticpath.service.catalog.ProductSkuLookup;

/**
 * Tests the {@code ShoppingItemImpl} class.
 */
public class ShoppingItemImplTest {
	private static final Currency USD = Currency.getInstance("USD");
	private static final long CART_UID = 123456L;
	private static final int SCALE_2 = 2;
	private static final int SCALE_3 = 3;
	private static final int SCALE_4 = 4;

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	@Mock private ProductSkuLookup productSkuLookup;

	private int uid;

	private ShoppingItemImpl createShoppingItem() {
		final ShoppingItemImpl shoppingItem = new ShoppingItemImpl();

		final String skuGuid = "ProductSku-" + ++uid;
		final ProductSku productSku = context.mock(ProductSku.class, skuGuid);
		context.checking(new Expectations() {
			{
				allowing(productSku).getGuid(); will(returnValue(skuGuid));
				allowing(productSku);

				allowing(productSkuLookup).findByGuid(skuGuid); will(returnValue(productSku));
			}
		});
		shoppingItem.setSkuGuid(productSku.getGuid());

		return shoppingItem;
	}

	/**
	 * Tests that two children with the same sku can be added as dependent.
	 **/
	@Test
	public void testAddingTwoDependentItemsWithSameSku() {
		ShoppingItemImpl item = createShoppingItem();
		ShoppingItemImpl child1 = createShoppingItem();
		
		child1.setCartUid(CART_UID);
		ShoppingItemImpl child2 = createShoppingItem();
		child2.setCartUid(CART_UID);
		
		item.addChildItem(child1);
		item.addChildItem(child2);

		assertEquals("Should be two separate items", 2, item
				.getDependentItems().size());
	}

	/** Test for testHasDependentCartItems(). */
	@Test
	public void testHasDependentCartItems() {
		CartItem cartItemImpl = createShoppingItem();
		CartItem dependentCartItem = createShoppingItem();
		dependentCartItem.setCartUid(CART_UID);
		assertFalse(cartItemImpl.hasDependentItems());
		cartItemImpl.addChildItem(dependentCartItem);
		assertTrue(cartItemImpl.hasDependentItems());
	}

	/**
	 * Tests math for rounding discount to nearest cent.
	 */
	@Test
	public void testCartDiscountAmount() {
		String four99Dollars = "499";
		String fiveDollars = "500";
		assertCartDiscountScaling("4996", SCALE_3, fiveDollars, SCALE_2);
		assertCartDiscountScaling("49951", SCALE_4, fiveDollars, SCALE_2);
		assertCartDiscountScaling("4995", SCALE_3, fiveDollars, SCALE_2);
		assertCartDiscountScaling("49950", SCALE_4, fiveDollars, SCALE_2);
		assertCartDiscountScaling("49949", SCALE_4, four99Dollars, SCALE_2);
		assertCartDiscountScaling("4994", SCALE_3, four99Dollars, SCALE_2);
		assertCartDiscountScaling(four99Dollars, SCALE_2, four99Dollars, SCALE_2);
		assertCartDiscountScaling("4990", SCALE_3, four99Dollars, SCALE_2);
	}

	/**
	 * Asserts math for rounding discount to nearest cent.
	 *
	 * @param unscaledValue1 The amount to be rounded
	 * @param scale1 The scale of the amount to be rounded
	 * @param unscaledValue2 The expected amount
	 * @param scale2 The scale of the expected amount
	 */
	protected void assertCartDiscountScaling(final String unscaledValue1, final int scale1, final String unscaledValue2, final int scale2) {
		BigDecimal unroundedDiscount = new BigDecimal(new BigInteger(unscaledValue1), scale1);
		BigDecimal expectedDiscount = new BigDecimal(new BigInteger(unscaledValue2), scale2);
		final ShoppingItemImpl item = createShoppingItem();
		PriceImpl price = new PriceImpl();
		price.setCurrency(USD);
		item.setPrice(1, price);

		// Rounding of the discount
		item.applyDiscount(unroundedDiscount, productSkuLookup);

		BigDecimal actualUnscaledDiscount = item.getDiscount().getAmountUnscaled();
		assertEquals("Discount not rounded correctly", expectedDiscount, actualUnscaledDiscount);

		// Ensure the additional scaling by the Money object does not interfere with the expected value
		BigDecimal actualScaledDiscount = item.getDiscount().getAmount();
		assertEquals("Discount is rounded correctly, but not equal", 0, expectedDiscount.compareTo(actualScaledDiscount));
	}
	
	/**
	 * Test price calculator without cart discounts or taxes.
	 * $80 unit price @ tier 6+ (computed) = $80.00
	 */
	@Test
	public void testGetUnitPriceCalcWithoutDiscountsOrTaxes() {
		final int quantity = 10;
		ShoppingItemImpl item = createShoppingItem();
		item.setPrice(quantity, createComplexPrice());
		item.applyDiscount(BigDecimal.TEN, productSkuLookup);
		assertEquals(new BigDecimal("80.00"), item.getPriceCalc().forUnitPrice().getAmount());
	}

	/**
	 * Test price calculator without cart discounts or taxes.
	 * ($80 unit price @ tier 6+) * 10 = $800.00
	 */
	@Test
	public void testGetPriceCalcWithoutDiscountsOrTaxes() {
		final int quantity = 10;
		ShoppingItemImpl item = createShoppingItem();
		item.setPrice(quantity, createComplexPrice());
		item.applyDiscount(new BigDecimal("10.00"), productSkuLookup);
		assertEquals(new BigDecimal("800.00"), item.getPriceCalc().getAmount());
	}

	/**
	 * Test price calculator with cart discounts and taxes.
	 * $80 unit price - ($20 discount / 10 qty) + ($10 taxes / 10 qty) = $79.00
	 */
	@Test
	public void testGetUnitPriceCalcWithDiscountsAndTaxes() {
		final int quantity = 10;
		ShoppingItemImpl item = createShoppingItem();
		item.setPrice(quantity, createComplexPrice());
		item.setTaxAmount(new BigDecimal("10.00"));
		item.applyDiscount(new BigDecimal("20.00"), productSkuLookup);
		
		assertEquals(new BigDecimal("79.00"), item.getTaxPriceCalculator().forUnitPrice().withCartDiscounts().getAmount());
	}

	/**
	 * Test price calculator with cart discounts and taxes.
	 * ($80 unit price * 10) - $20 discount + $10 taxes = $790.00
	 */
	@Test
	public void testGetPriceCalcWithDiscountsAndTaxes() {
		final int quantity = 10;
		ShoppingItemImpl item = createShoppingItem();
		item.setPrice(quantity, createComplexPrice());
		item.setTaxAmount(BigDecimal.TEN);
		item.applyDiscount(new BigDecimal("20.00"), productSkuLookup);

		PriceCalculator discountedPriceCalculator = item.getTaxPriceCalculator().withCartDiscounts();
		assertEquals(new BigDecimal("790.00"), discountedPriceCalculator.getAmount());
		
		// This ensures that item.getPriceCalc().withCartDiscounts().getAmount() returns the same value as getTotal.
		// Can be removed once the deprecated getLowestUnitPrice method is removed.
		discountedPriceCalculator = item.getPriceCalc().withCartDiscounts();
		assertEquals(item.getTotal().getAmount(), discountedPriceCalculator.getAmount());
	}

	/**
	 * Test price calculator without taxes on an item with inclusive tax.
	 * $80 unit price = $80.00
	 */
	@Test
	public void testInclusiveTaxGetUnitPriceCalcDefaultTaxes() {
		final int quantity = 10;
		ShoppingItemImpl item = createShoppingItem();
		item.setTaxInclusive(true);
		item.setPrice(quantity, createComplexPrice());
		item.setTaxAmount(BigDecimal.TEN);
		
		assertEquals(new BigDecimal("80.00"), item.getPriceCalc().forUnitPrice().getAmount());
	}

	/**
	 * Test price calculator without taxes on an item with inclusive tax.
	 * $80 unit price - ($10 taxes / 10 qty) = $79.00
	 */
	@Test
	public void testInclusiveTaxGetUnitPriceCalcWithoutTaxes() {
		final int quantity = 10;
		ShoppingItemImpl item = createShoppingItem();
		item.setTaxInclusive(true);
		item.setPrice(quantity, createComplexPrice());
		item.setTaxAmount(BigDecimal.TEN);
		
		assertEquals(new BigDecimal("79.00"), item.getTaxPriceCalculator().forUnitPrice().getAmount());
	}

	/**
	 * Test that shopping item data is created with the correct key and value.
	 */
	@Test
	public void testCreateShoppingItemData() {
		final String key = "Record";
		final String value = "Test Data";
		ShoppingItemImpl item = createShoppingItem();
		AbstractItemData itemData = item.createItemData(key, value);

		assertEquals(key, itemData.getKey());
		assertEquals(value, itemData.getValue());
	}

	/**
	 * Create a complicated price object for testing purposes.
	 * List Prices:
	 * Qty 1+: $100 USD
	 * Qty 6+: $90 USD
	 * 
	 * Sale Prices:
	 * Qty 6+: $86 USD
	 * 
	 * Computed Prices:
	 * Qty 6+: $80 USD
	 * 
	 * @return
	 */
	private Price createComplexPrice() {
		final int minQty = 6;
		Price price = new PriceImpl();

		price.setListPrice(Money.valueOf("100.00", USD), 1);
		price.setListPrice(Money.valueOf("90.00", USD), minQty);
		price.setSalePrice(Money.valueOf("86.00", USD), minQty);
		price.setComputedPriceIfLower(Money.valueOf("80.00", USD), minQty);
		return price;
	}
}
