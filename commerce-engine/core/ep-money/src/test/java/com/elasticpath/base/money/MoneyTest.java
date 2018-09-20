/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.base.money;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import com.elasticpath.money.Money;

/**
 * Test for <code>Money</code>.
 */
@SuppressWarnings({ "PMD.AvoidDecimalLiteralsInBigDecimalConstructor" })
public class MoneyTest {

	private static final Currency CAD = Currency.getInstance("CAD");
	private static final Currency JPY = Currency.getInstance("JPY");
	private static final float FLOAT_DELTA = 0.001F;

	@Test
	public void testEqualsHashCode() {
		new EqualsTester()
				.addEqualityGroup(
						Money.valueOf(BigDecimal.ZERO, CAD),
						Money.valueOf("0.0", CAD))
				.addEqualityGroup(
						Money.valueOf(BigDecimal.ONE, CAD))
				.addEqualityGroup(
						Money.valueOf(BigDecimal.ZERO, JPY))
				.testEquals();
	}

	private static void assertMoneyValuesNotEqual(final String message, final Currency currency, final float largerValue, final float smallerValue) {
		Money larger = Money.valueOf(new BigDecimal(largerValue), currency);
		Money smaller = Money.valueOf(new BigDecimal(smallerValue), currency);

		assertTrue(message, larger.compareTo(smaller) > 0);
		assertTrue(message, smaller.compareTo(larger) < 0);
	}

	private static void assertMoneyValuesNotEqual(final Currency currency, final float largerValue, final float smallerValue) {
		assertMoneyValuesNotEqual(null, currency, largerValue, smallerValue);
	}

	private static void assertMoneyValuesEqual(final String message, final Currency currency, final float value1, final float value2) {
		Money money1 = Money.valueOf(new BigDecimal(value1), currency);
		Money money2 = Money.valueOf(new BigDecimal(value2), currency);

		assertEquals(message, 0, money1.compareTo(money2));
		assertEquals(message, 0, money2.compareTo(money1));
	}

	private static void assertMoneyValuesEqual(final Currency currency, final float value1, final float value2) {
		assertMoneyValuesEqual(null, currency, value1, value2);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.Money.getCurrency()'.
	 */
	@Test
	public void testGetCurrency() {
		final Money money = Money.valueOf(BigDecimal.ZERO, CAD);
		assertSame(money.getCurrency(), CAD);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.Money.subtract(Money)'.
	 */
	@Test
	public void testSubtract() {
		// CHECKSTYLE:OFF  Magic Numbers OK!
		final BigDecimal amount1 = new BigDecimal(11F);
		final BigDecimal amount2 = new BigDecimal(5.5F);

		Money money1 = Money.valueOf(amount1, CAD);
		Money money2 = Money.valueOf(amount2, CAD);

		Money subtractedMoney = money1.subtract(money2);

		assertEquals(amount2.setScale(2), subtractedMoney.getAmount());
		// CHECKSTYLE:ON
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.Money.add(Money)'.
	 */
	@Test
	public void testAdd() {
		// CHECKSTYLE:OFF  Magic Numbers OK!
		final BigDecimal amount1 = new BigDecimal(5.5F);
		Money money1 = Money.valueOf(amount1, CAD);
		Money money2 = Money.valueOf(amount1, CAD);

		Money sum = money1.add(money2);

		final BigDecimal amount2 = new BigDecimal(11F).setScale(2);
		assertEquals(amount2, sum.getAmount());
		// CHECKSTYLE:ON
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.Money.getSaleSavings(Money)'.
	 */
	@Test
	public void testGetSaleSavings() {
		// CHECKSTYLE:OFF  Magic Numbers OK!
		Money money1 = Money.valueOf(new BigDecimal(10F), CAD);
		Money money2 = Money.valueOf(new BigDecimal(5F), CAD);

		assertEquals(Money.valueOf(new BigDecimal(5F), CAD), money1.getSaleSavings(money2));
		assertEquals(Money.valueOf(new BigDecimal(0F), CAD), money1.getSaleSavings(money1));
		assertEquals(Money.valueOf(new BigDecimal(0F), CAD), money2.getSaleSavings(money1));
		// CHECKSTYLE:ON
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.Money.displaySalePercentage(Money, Locale)'.
	 */
	@Test
	public void testGetSalePercentage() {
		// CHECKSTYLE:OFF  Magic Numbers OK!
		Money money1 = Money.valueOf(new BigDecimal(10F), CAD);
		Money money2 = Money.valueOf(new BigDecimal(5F), CAD);

		assertEquals(0.50, money1.getSalePercentage(money2), FLOAT_DELTA);
		assertEquals(0.0, money1.getSalePercentage(money1), FLOAT_DELTA);
		assertEquals(0.0, money2.getSalePercentage(money1), FLOAT_DELTA);
		// CHECKSTYLE:ON
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.Money.greaterThan(Money)'.
	 */
	@Test
	public void testGreaterThan() {
		// CHECKSTYLE:OFF  Magic Numbers OK!
		final BigDecimal amount1 = new BigDecimal(12.34F);
		Money money1 = Money.valueOf(amount1, CAD);
		final BigDecimal amount2 = new BigDecimal(55.55F);
		Money money2 = Money.valueOf(amount2, CAD);

		assertTrue(money2.greaterThan(money1));
		assertFalse(money2.greaterThan(money2));
		// CHECKSTYLE:ON
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.Money.lessThan(Money)'.
	 */
	@Test
	public void testLessThan() {
		// CHECKSTYLE:OFF  Magic Numbers OK!
		final BigDecimal amount1 = new BigDecimal(12.34F);
		Money money1 = Money.valueOf(amount1, CAD);
		final BigDecimal amount2 = new BigDecimal(55.55F);
		Money money2 = Money.valueOf(amount2, CAD);

		assertTrue(money1.lessThan(money2));
		assertFalse(money1.lessThan(money1));
		// CHECKSTYLE:ON
	}

	/** Tests {@link Money#compareTo(Money)}. */
	@Test
	public void testCompareTo() {
		// CHECKSTYLE:OFF -- we want magic number here
		assertMoneyValuesEqual(CAD, 3.00f, 3f);
		assertMoneyValuesNotEqual(CAD, 3.12f, 3.00f);
		assertMoneyValuesEqual(CAD, 9.3f, 9.3f);
		assertMoneyValuesEqual(CAD, 7.8f, 7.8f);
		assertMoneyValuesEqual("CAD has 2 decimals, should be rounded the same", CAD, 7.8f, 7.804f);
		assertMoneyValuesEqual("JPY has 0 decimals, should round to same value", JPY, 7.6f, 7.904f);
		assertMoneyValuesNotEqual("Money values should be rounded to different values", CAD, 7.806f, 7.801f);
		assertMoneyValuesEqual("JPY has 0 decimals, round limits", JPY, 23.49337f, 23f);
		assertMoneyValuesNotEqual("JPY has 0 decimals, rounds to different values", JPY, 23.5f, 23f);
		assertMoneyValuesEqual("JPY has 0 decimals, rounds to same values", JPY, 23.49f, 23f);
		// CHECKSTYLE:ON
	}

	/**
	 * Tests the multiply() method.
	 */
	@Test
	public void testMultiply() {
		Currency currency = Currency.getInstance(Locale.CANADA);
		Money money1 = Money.valueOf("3.00", currency);
		
		Money expectedResult = Money.valueOf("6.00", currency);
		assertEquals(expectedResult, money1.multiply(new BigDecimal("2")));
		assertEquals(expectedResult, money1.multiply(2));
	}
	
	/**
	 * Tests that toString() returns a non-null result.
	 */
	@Test
	public void testToString() {
		Currency currency = Currency.getInstance(Locale.CANADA);
		Money money = Money.valueOf(BigDecimal.ONE, currency);
		assertNotNull(money.toString());
	}
}
