/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.service.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.domain.catalog.PriceTier;
import com.elasticpath.domain.catalog.impl.PriceTierImpl;
import com.elasticpath.domain.shoppingcart.DiscountRecord;

/** Test case for <code>PriceTierImpl</code>. */
public class PriceTierImplTest {

	private static final String FIFTY = "50";
	private static final String ONE_HUNDRED = "100";
	private PriceTier priceTier;

	/**
	 * Prepare for the tests.
	 * @throws Exception on error
	 */
	@Before
	public void setUp() throws Exception {
		priceTier = new PriceTierImpl();
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.PriceTierImpl.getComputedPrice()'.
	 */
	@Test
	public void testGetComputedPrice1() {
		BigDecimal highPrice = new BigDecimal(ONE_HUNDRED);
		BigDecimal lowPrice = new BigDecimal(FIFTY);

		priceTier.setComputedPriceIfLower(highPrice);
		priceTier.setComputedPriceIfLower(lowPrice);

		assertThat(priceTier.getComputedPrice()).isEqualTo(lowPrice);
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.PriceTierImpl.getComputedPrice()'.
	 */
	@Test
	public void testGetComputedPrice2() {
		BigDecimal highPrice = new BigDecimal(ONE_HUNDRED);
		BigDecimal lowPrice = new BigDecimal(FIFTY);

		priceTier.setComputedPriceIfLower(lowPrice);
		priceTier.setComputedPriceIfLower(highPrice);

		assertThat(priceTier.getComputedPrice()).isEqualTo(lowPrice);
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.PriceTierImpl.getComputedPrice()'.
	 */
	@Test
	public void testGetComputedPrice3() {
		BigDecimal highPrice = new BigDecimal(ONE_HUNDRED);
		BigDecimal lowPrice = new BigDecimal(FIFTY);

		priceTier.setComputedPriceIfLower(lowPrice);
		priceTier.clearComputedPrice();
		priceTier.setComputedPriceIfLower(highPrice);

		assertThat(priceTier.getComputedPrice()).isEqualTo(highPrice);
	}

	/**
	 * Test getLowestPrice when sale price is lower than list.
	 */
	@Test
	public void testGetLowestPrice1() {
		BigDecimal listPrice = new BigDecimal(ONE_HUNDRED);
		BigDecimal salePrice = new BigDecimal(FIFTY);

		priceTier.setListPrice(listPrice);
		priceTier.setSalePrice(salePrice);

		assertThat(priceTier.getLowestPrice()).isEqualTo(salePrice);
	}

	/**
	 * Test getLowestPrice when sale price is higher than list.
	 */
	@Test
	public void testGetLowestPrice2() {
		BigDecimal listPrice = new BigDecimal(FIFTY);
		BigDecimal salePrice = new BigDecimal(ONE_HUNDRED);

		priceTier.setListPrice(listPrice);
		priceTier.setSalePrice(salePrice);

		assertThat(priceTier.getLowestPrice()).isEqualTo(listPrice);
	}

	/**
	 * Test getLowestPrice when list price is null.
	 */
	@Test
	public void testGetLowestPrice3() {
		BigDecimal listPrice = null;
		BigDecimal salePrice = new BigDecimal(FIFTY);

		priceTier.setListPrice(listPrice);
		priceTier.setSalePrice(salePrice);

		assertThat(priceTier.getLowestPrice()).isEqualTo(salePrice);
	}

	/**
	 * Test getLowestPrice when sale price is null.
	 */
	@Test
	public void testGetLowestPrice4() {
		BigDecimal listPrice = new BigDecimal(ONE_HUNDRED);
		BigDecimal salePrice = null;

		priceTier.setListPrice(listPrice);
		priceTier.setSalePrice(salePrice);

		assertThat(priceTier.getLowestPrice()).isEqualTo(listPrice);
	}

	/**
	 * Test getLowestPrice when both prices are null.
	 */
	@Test
	public void testGetLowestPrice5() {
		BigDecimal listPrice = null;
		BigDecimal salePrice = null;

		priceTier.setListPrice(listPrice);
		priceTier.setSalePrice(salePrice);

		assertThat(priceTier.getLowestPrice()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.PriceTierImpl.setComputedPriceIfLower()'.
	 */
	@Test
	public void testSetComputedPriceNegative() {
		BigDecimal negativePrice = new BigDecimal("-100");
		priceTier.setComputedPriceIfLower(negativePrice);
		assertThat(priceTier.getComputedPrice()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.PriceTierImpl.setSalePrice()'.
	 */
	@Test
	public void testSetSalePriceNegative() {
		BigDecimal negativePrice = new BigDecimal("-100");
		priceTier.setSalePrice(negativePrice);
		assertThat(priceTier.getSalePrice()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.PriceTierImpl.setListPrice()'.
	 */
	@Test
	public void testSetListPriceNegative() {
		BigDecimal negativePrice = new BigDecimal("-100");
		priceTier.setListPrice(negativePrice);
		assertThat(priceTier.getListPrice()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	/**
	 * Test compare when both prices are equal.
	 */
	@Test
	public void testCompare1() {
		priceTier.setListPrice(new BigDecimal(ONE_HUNDRED));

		PriceTier priceTier2 = new PriceTierImpl();
		priceTier2.setListPrice(new BigDecimal(ONE_HUNDRED));

		assertThat(priceTier).isEqualByComparingTo(priceTier2);
	}

	/**
	 * Test compare when left price is lower.
	 */
	@Test
	public void testCompare2() {
		priceTier.setListPrice(new BigDecimal(FIFTY));

		PriceTier priceTier2 = new PriceTierImpl();
		priceTier2.setListPrice(new BigDecimal(ONE_HUNDRED));

		assertThat(priceTier).isLessThan(priceTier2);
	}

	/**
	 * Test compare when left price is higher.
	 */
	@Test
	public void testCompare3() {
		priceTier.setListPrice(new BigDecimal(ONE_HUNDRED));

		PriceTier priceTier2 = new PriceTierImpl();
		priceTier2.setListPrice(new BigDecimal(FIFTY));

		assertThat(priceTier).isGreaterThan(priceTier2);
	}

	/**
	 * Test compare when left price is null.
	 */
	@Test
	public void testCompare4() {
		PriceTier priceTier2 = new PriceTierImpl();
		priceTier2.setListPrice(new BigDecimal(ONE_HUNDRED));

		assertThat(priceTier).isLessThan(priceTier2);
	}

	/**
	 * Test compare when right price is null.
	 */
	@Test
	public void testCompare5() {
		priceTier.setListPrice(new BigDecimal(ONE_HUNDRED));

		PriceTier priceTier2 = new PriceTierImpl();

		assertThat(priceTier).isGreaterThan(priceTier2);
	}

	/**
	 * Test compare when both prices are null.
	 */
	@Test
	public void testCompare6() {
		PriceTier priceTier2 = new PriceTierImpl();

		assertThat(priceTier).isEqualByComparingTo(priceTier2);
	}

	@Test
	public void verifyDiscountRecordCollectionIsInitiallyEmpty() {
		// Given a price with no discount records
		// When I retrieve the set of discount records
		final Collection<DiscountRecord> discountRecords = priceTier.getDiscountRecords();

		// Then no discount records exist
		assertThat(discountRecords).isEmpty();
	}

	@Test(expected = UnsupportedOperationException.class)
	public void verifyGetDiscountRecordsSetIsImmutable() {
		// Given a price with discount records
		priceTier.addDiscountRecord(mock(DiscountRecord.class, "DiscountRecord 1"));
		priceTier.addDiscountRecord(mock(DiscountRecord.class, "DiscountRecord 2"));

		// When I retrieve the set of discount records
		final Collection<DiscountRecord> discountRecords = priceTier.getDiscountRecords();

		// Then I am unable to modify its contents
		discountRecords.clear(); // throws exception
	}

	@Test
	public void verifyManyDiscountRecordsCanBeAdded() {
		// Given a price with no discount record
		// When I add discount records
		final DiscountRecord discountRecord1 = mock(DiscountRecord.class, "DiscountRecord 1");
		final DiscountRecord discountRecord2 = mock(DiscountRecord.class, "DiscountRecord 2");
		priceTier.addDiscountRecord(discountRecord1);
		priceTier.addDiscountRecord(discountRecord2);

		// Then the price contains the discount records
		assertThat(priceTier.getDiscountRecords()).contains(discountRecord1, discountRecord2);
	}

	@Test
	public void verifyClearDiscountRecordsClearsDiscountRecords() {
		// Given a price with discount records
		priceTier.addDiscountRecord(mock(DiscountRecord.class, "DiscountRecord 1"));
		priceTier.addDiscountRecord(mock(DiscountRecord.class, "DiscountRecord 2"));

		// When I clear the discount records
		priceTier.clearDiscountRecords();

		// Then no discount records exist
		assertThat(priceTier.getDiscountRecords()).isEmpty();
	}

}
