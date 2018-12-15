/**
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceSchedule;
import com.elasticpath.domain.catalog.PriceScheduleType;
import com.elasticpath.domain.catalog.PriceTier;
import com.elasticpath.domain.catalog.PricingScheme;
import com.elasticpath.domain.quantity.Quantity;
import com.elasticpath.domain.shoppingcart.DiscountRecord;
import com.elasticpath.domain.subscriptions.PaymentSchedule;
import com.elasticpath.domain.subscriptions.impl.PaymentScheduleImpl;
import com.elasticpath.money.Money;

/**
 * Test <code>PriceImpl</code>.
 */
@RunWith(MockitoJUnitRunner.class)
public class PriceImplTest {

	private PriceImpl priceImpl;

	private static final int FIRST_TIER_MTY = 1;

	private static final int SECOND_TIER_MTY = 5;

	private static final int THIRD_TIER_MTY = 10;

	private static final int QTY_5 = 5;

	private static final int QTY_6 = 6;

	private static final int NON_EXIST_TIER_MTY = 99;

	private static final int QTY_100 = 100;

	private static final Quantity MONTHLY_QTY = new Quantity(1, "month");

	private static final Currency CURRENCY_CAD = Currency.getInstance(Locale.CANADA);

	private PricingScheme pricingScheme;

	/**
	 * Prepares for the next test.
	 */
	@Before
	public void setUp() {

		priceImpl = new PriceImpl();

		PriceTierImpl priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(FIRST_TIER_MTY);
		priceImpl.addOrUpdatePriceTier(priceTier1);

		PriceTierImpl priceTier2 = new PriceTierImpl();
		priceTier2.setMinQty(SECOND_TIER_MTY);
		priceImpl.addOrUpdatePriceTier(priceTier2);

		PriceTierImpl priceTier3 = new PriceTierImpl();
		priceTier3.setMinQty(THIRD_TIER_MTY);
		priceImpl.addOrUpdatePriceTier(priceTier3);

		PaymentSchedule monthlyPaymentSchedule = new PaymentScheduleImpl();
		monthlyPaymentSchedule.setName("Monthly");
		monthlyPaymentSchedule.setPaymentFrequency(MONTHLY_QTY);
		//null duration on purpose

		PriceSchedule monthlyPriceSchedule = new PriceScheduleImpl();
		monthlyPriceSchedule.setType(PriceScheduleType.RECURRING);
		monthlyPriceSchedule.setPaymentSchedule(monthlyPaymentSchedule);

		Price monthlyPrice = new PriceImpl();
		monthlyPrice.setCurrency(CURRENCY_CAD);
		final Money cadMoney = Money.valueOf("22.22", CURRENCY_CAD);
		monthlyPrice.setListPrice(cadMoney);

		pricingScheme = new PricingSchemeImpl();
		pricingScheme.setPriceForSchedule(monthlyPriceSchedule, monthlyPrice);



	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.getCurrency()'.
	 */
	@Test
	public void testGetCurrency() {
		assertThat(priceImpl.getCurrency()).as("Check get currency").isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.setCurrency(Currency)'.
	 */
	@Test
	public void testSetCurrency() {
		priceImpl.setCurrency(CURRENCY_CAD);
		assertThat(priceImpl.getCurrency())
			.as("Check set currency")
			.isEqualTo(CURRENCY_CAD);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.setDefaultValues()'.
	 */
	@Test
	public void testGetCurrencyFromPriceScheme() {
		final Price priceImpl = new PriceImpl(); //use an empty PriceImpl object
		assertThat(priceImpl.getCurrency()).isNull();  //make sure currency at the priceImpl level is null
		assertThat(priceImpl.getPriceTiers()).isNull();

		priceImpl.setPricingScheme(pricingScheme); //add the scheme to the price

		assertThat(priceImpl.getCurrency()).isEqualTo(CURRENCY_CAD);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.getListPrice()'.
	 */
	@Test
	public void testGetListPrice() {
		assertThat(priceImpl.getListPrice()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.setListPrice()'.
	 */
	@Test
	public void testSetListPrice() {
		priceImpl.setCurrency(CURRENCY_CAD);

		final Money cadMoney = Money.valueOf("18.88", CURRENCY_CAD);
		priceImpl.setListPrice(cadMoney);
		Money cadListPrice = priceImpl.getListPrice();
		assertThat(cadListPrice.getCurrency()).isEqualTo(CURRENCY_CAD);
		assertThat(cadListPrice.getAmount()).isEqualTo(cadMoney.getAmount());

		priceImpl.setListPrice(cadMoney, SECOND_TIER_MTY);
		cadListPrice = priceImpl.getListPrice(QTY_6);
		assertThat(cadListPrice.getCurrency()).isEqualTo(CURRENCY_CAD);
		assertThat(cadListPrice.getAmount()).isEqualTo(cadMoney.getAmount());

		final Money cadMoney2 = Money.valueOf("8.88", CURRENCY_CAD);
		priceImpl.setListPrice(cadMoney2, NON_EXIST_TIER_MTY);
		cadListPrice = priceImpl.getListPrice(QTY_100);
		assertThat(cadListPrice.getCurrency()).isEqualTo(CURRENCY_CAD);
		assertThat(cadListPrice.getAmount()).isEqualTo(cadMoney2.getAmount());

	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.getSalePrice()'.
	 */
	@Test
	public void testGetSalePrice() {
		assertThat(priceImpl.getSalePrice()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.setSalePrice()'.
	 */
	@Test
	public void testSetSalePrice() {
		final Currency currencyOfCAD = Currency.getInstance(Locale.CANADA);
		priceImpl.setCurrency(currencyOfCAD);

		final Money cadMoney = Money.valueOf("18.88", currencyOfCAD);
		priceImpl.setSalePrice(cadMoney);
		Money cadSalePrice = priceImpl.getSalePrice();
		assertThat(cadSalePrice.getCurrency()).isEqualTo(currencyOfCAD);
		assertThat(cadSalePrice.getAmount()).isEqualTo(cadMoney.getAmount());

		priceImpl.setSalePrice(cadMoney, SECOND_TIER_MTY);
		cadSalePrice = priceImpl.getSalePrice(QTY_6);
		assertThat(cadSalePrice.getCurrency()).isEqualTo(currencyOfCAD);
		assertThat(cadSalePrice.getAmount()).isEqualTo(cadMoney.getAmount());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.getComputedPrice()'.
	 */
	@Test
	public void testGetComputedPrice() {
		assertThat(priceImpl.getComputedPrice()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.setComputedPriceIfLower()'.
	 */
	@Test
	public void testSetComputedPrice() {
		final Currency currencyOfCAD = Currency.getInstance(Locale.CANADA);
		priceImpl.setCurrency(currencyOfCAD);

		final Money cadMoney = Money.valueOf("18.88", currencyOfCAD);
		priceImpl.setComputedPriceIfLower(cadMoney);
		Money cadComputedPrice = priceImpl.getComputedPrice();
		assertThat(cadComputedPrice.getCurrency()).isEqualTo(currencyOfCAD);
		assertThat(cadComputedPrice.getAmount()).isEqualTo(cadMoney.getAmount());

		priceImpl.setComputedPriceIfLower(cadMoney, SECOND_TIER_MTY);
		cadComputedPrice = priceImpl.getComputedPrice(QTY_6);
		assertThat(cadComputedPrice.getCurrency()).isEqualTo(currencyOfCAD);
		assertThat(cadComputedPrice.getAmount()).isEqualTo(cadMoney.getAmount());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.setComputedPriceIfLower()'.
	 */
	@Test
	public void testSetComputedPriceNegative() {
		final Currency currencyOfCAD = Currency.getInstance(Locale.CANADA);
		priceImpl.setCurrency(currencyOfCAD);

		final Money cadMoney = Money.valueOf("-18.88", currencyOfCAD);
		priceImpl.setComputedPriceIfLower(cadMoney);
		Money returnedCadComputedPrice = priceImpl.getComputedPrice();
		assertThat(returnedCadComputedPrice.getCurrency()).isEqualTo(currencyOfCAD);
		assertThat(returnedCadComputedPrice.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.getMatchPriceTier(minQty)'.
	 */
	@Test
	public void testGetMatchPriceTier() {

		PriceTier priceTier = priceImpl.getPriceTierByExactMinQty(1);
		assertThat(priceTier.getMinQty()).isEqualTo(1);

		priceTier = priceImpl.getPriceTierByExactMinQty(SECOND_TIER_MTY);
		assertThat(priceTier.getMinQty()).isEqualTo(QTY_5);

		priceTier = priceImpl.getPriceTierByExactMinQty(QTY_6);
		assertThat(priceTier).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.getFirstPriceTierMinQty()'.
	 */
	@Test
	public void testGetFirstPriceTierMinQty() {
		assertThat(priceImpl.getFirstPriceTierMinQty()).isEqualTo(1);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.hasPriceTiers()'.
	 */
	@Test
	public void testhasPriceTiers() {
		assertThat(priceImpl.hasPriceTiers()).isTrue();

		priceImpl.setPersistentPriceTiers(null);
		PriceTier priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(SECOND_TIER_MTY);
		assertThat(priceImpl.hasPriceTiers()).isFalse();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AbstractPriceImpl.setDefaultValues()'.
	 */
	@Test
	public void testInitialize() {
		final Price priceImpl = new PriceImpl();
		assertThat(priceImpl.getCurrency()).isNull();
		assertThat(priceImpl.getPriceTiers()).isNull();

		priceImpl.initialize();
		// After set default values, the following field should get populated.
		assertThat(priceImpl.getPriceTiers()).isEmpty();
	}

	@Test
	public void verifyGetDiscountRecordsWithQuantityFindsDiscountRecordsOfCorrespondingPriceTier() {
		final Price price = new PriceImpl();
		final DiscountRecord discountRecord1 = mock(DiscountRecord.class, "Discount Record 1");
		final DiscountRecord discountRecord2 = mock(DiscountRecord.class, "Discount Record 2");

		// Given price has a price tier for qty 1 with discount record 1
		final PriceTierImpl priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(FIRST_TIER_MTY);
		priceTier1.addDiscountRecord(discountRecord1);
		price.addOrUpdatePriceTier(priceTier1);

		// And price has a price tier for qty 5 with discount record 2
		final PriceTierImpl priceTier2 = new PriceTierImpl();
		priceTier2.setMinQty(SECOND_TIER_MTY);
		priceTier2.addDiscountRecord(discountRecord2);
		price.addOrUpdatePriceTier(priceTier2);

		// When I get the Discount Records for qty 5
		final Collection<DiscountRecord> discountRecords = price.getDiscountRecords(SECOND_TIER_MTY);

		// Then I get discount record 2
		assertThat(discountRecords).containsExactly(discountRecord2);
	}

	@Test
	public void verifyGetDiscountRecordsFindsFromFirstPriceTier() {
		final Price price = new PriceImpl();
		final DiscountRecord discountRecord1 = mock(DiscountRecord.class, "Discount Record 1");
		final DiscountRecord discountRecord2 = mock(DiscountRecord.class, "Discount Record 2");

		// Given price has a price tier for qty 1 with discount record 1
		final PriceTierImpl priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(FIRST_TIER_MTY);
		priceTier1.addDiscountRecord(discountRecord1);
		price.addOrUpdatePriceTier(priceTier1);

		// And price has a price tier for qty 5 with discount record 2
		final PriceTierImpl priceTier2 = new PriceTierImpl();
		priceTier2.setMinQty(SECOND_TIER_MTY);
		priceTier2.addDiscountRecord(discountRecord2);
		price.addOrUpdatePriceTier(priceTier2);

		// When I get the Discount Record without specifying a quantity
		final Collection<DiscountRecord> discountRecords = price.getDiscountRecords();

		// Then I get the first discount record, discount record 1
		assertThat(discountRecords).containsExactly(discountRecord1);
	}

	@Test
	public void verifyGetDiscountsRecordsEmptyWhenNoPriceTiers() {
		// Given price with no price tiers
		final Price price = new PriceImpl();

		// When I get the Discount Record without specifying a quantity
		final Collection<DiscountRecord> discountRecords = price.getDiscountRecords();

		// Then no Discount Record is returned
		assertThat(discountRecords).isEmpty();
	}

	@Test
	public void verifyGetDiscountRecordsEmptyWhenNoMatchingPriceTier() {
		final Price price = new PriceImpl();
		final DiscountRecord discountRecord = mock(DiscountRecord.class);

		// Given price has a price tier for qty 5 with discount record 1
		final PriceTier priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(SECOND_TIER_MTY);
		priceTier1.addDiscountRecord(discountRecord);
		price.addOrUpdatePriceTier(priceTier1);

		// When I get the Discount Record for qty 1
		final Collection<DiscountRecord> discountRecords = price.getDiscountRecords(FIRST_TIER_MTY);

		// Then no Discount Record is returned
		assertThat(discountRecords).isEmpty();
	}

	@Test
	public void verifyAddDiscountRecordWithoutQtySetsOnFirstPriceTier() {
		final Price price = new PriceImpl();
		final DiscountRecord discountRecord = mock(DiscountRecord.class);

		// Given price has a price tier for qty 1
		final PriceTierImpl priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(FIRST_TIER_MTY);
		price.addOrUpdatePriceTier(priceTier1);

		// And price has a price tier for qty 5
		final PriceTierImpl priceTier2 = new PriceTierImpl();
		priceTier2.setMinQty(SECOND_TIER_MTY);
		price.addOrUpdatePriceTier(priceTier2);

		// When I add a discount record without specifying a quantity
		price.addDiscountRecord(discountRecord);

		// Then the discount record is set on the first price tier
		assertThat(priceTier1.getDiscountRecords()).containsExactly(discountRecord);
	}

	@Test
	public void verifyAddDiscountRecordWithQtySetsOnCorrespondingPriceTier() {
		final Price price = new PriceImpl();
		final DiscountRecord discountRecord = mock(DiscountRecord.class);

		// Given price has a price tier for qty 1
		final PriceTierImpl priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(FIRST_TIER_MTY);
		price.addOrUpdatePriceTier(priceTier1);

		// And price has a price tier for qty 5
		final PriceTierImpl priceTier2 = new PriceTierImpl();
		priceTier2.setMinQty(SECOND_TIER_MTY);
		price.addOrUpdatePriceTier(priceTier2);

		// When I add a discount record for qty 5
		price.addDiscountRecord(discountRecord, SECOND_TIER_MTY);

		// Then the discount record is set on the second price tier
		assertThat(priceTier2.getDiscountRecords()).containsExactly(discountRecord);
	}

	@Test
	public void verifyClearDiscountRecordsClearsFromAllPriceTiers() {
		final Price price = new PriceImpl();
		final DiscountRecord discountRecord1 = mock(DiscountRecord.class, "Discount Record 1");
		final DiscountRecord discountRecord2 = mock(DiscountRecord.class, "Discount Record 2");

		// Given price has a price tier for qty 1 with discount record 1
		final PriceTierImpl priceTier1 = new PriceTierImpl();
		priceTier1.setMinQty(FIRST_TIER_MTY);
		priceTier1.addDiscountRecord(discountRecord1);
		price.addOrUpdatePriceTier(priceTier1);

		// And price has a price tier for qty 5 with discount record 2
		final PriceTierImpl priceTier2 = new PriceTierImpl();
		priceTier2.setMinQty(SECOND_TIER_MTY);
		priceTier2.addDiscountRecord(discountRecord2);
		price.addOrUpdatePriceTier(priceTier2);

		// When I clear the computed price
		price.clearDiscountRecords();

		// Then the first price tier has no discount record
		assertThat(priceTier1.getDiscountRecords()).isEmpty();

		// And second price tier has no discount record
		assertThat(priceTier2.getDiscountRecords()).isEmpty();
	}

}
