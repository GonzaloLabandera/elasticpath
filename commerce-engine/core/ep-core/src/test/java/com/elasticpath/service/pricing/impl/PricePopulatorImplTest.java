/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.pricing.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.Price;
import com.elasticpath.domain.catalog.PriceTier;
import com.elasticpath.domain.catalog.impl.PriceImpl;
import com.elasticpath.domain.catalog.impl.PriceTierImpl;
import com.elasticpath.domain.pricing.BaseAmount;
import com.elasticpath.domain.pricing.BaseAmountObjectType;
import com.elasticpath.domain.pricing.impl.BaseAmountImpl;
import com.elasticpath.money.Money;

/**
 * Test class for @{PricePopulatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PricePopulatorImplTest {
	private static final String GUID = "GUID";
	private static final String SKU = "SKU";
	private static final int TEN = 10;

	@Mock
	private BeanFactory beanFactory;

	private final PricePopulatorImpl pricePopulator = new PricePopulatorImpl();
	/**
	 * Setup.
	 */
	@Before
	public void setUp() {
		pricePopulator.setBeanFactory(beanFactory);
	}

	/**
	 * Test that if no base amounts are found, Price is null.
	 */
	@Test
	public void testPopulatePriceWithNoBaseAmounts() {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());

		Collection<BaseAmount> amounts = new ArrayList<>();
		Currency currency = Currency.getInstance(Locale.CANADA);
		Price price = beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class);
		boolean found = pricePopulator.populatePriceFromBaseAmounts(amounts, currency, price);
		assertThat(found).isFalse();
	}

	/**
	 * Test that if null list price AND sale price are found, Price is null.
	 */
	@Test
	public void testPopulatePriceWithNullBaseAmounts() {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());

		BaseAmount baseAmount = new BaseAmountImpl(GUID, GUID, SKU, BigDecimal.ONE, null, null, GUID);
		Collection<BaseAmount> amounts = new ArrayList<>();
		amounts.add(baseAmount);
		Currency currency = Currency.getInstance(Locale.CANADA);
		Price price = beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class);
		boolean found = pricePopulator.populatePriceFromBaseAmounts(amounts, currency, price);
		assertThat(found).isFalse();
	}

	/**
	 * Test the price object does not contain a price tier for null prices.
	 */
	@Test
	public void testPopulatePriceWithNullPrices() {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_TIER, PriceTier.class)).thenAnswer(invocation -> new PriceTierImpl());

		Collection<BaseAmount> amounts = new ArrayList<>();
		amounts.add(createBaseAmount("PL", "1", "9.99", "4.99", "PROD",
				BaseAmountObjectType.PRODUCT.toString()));

		amounts.add(new BaseAmountImpl(GUID, GUID, SKU, BigDecimal.TEN, null, null, GUID));

		Currency currency = Currency.getInstance(Locale.CANADA);
		Price price = beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class);
		boolean found = pricePopulator.populatePriceFromBaseAmounts(amounts, currency, price);
		assertThat(found).isTrue();
		assertThat(price.getPriceTiers()).hasSize(1);
	}
	/**
	 * Test the price object is populated by the base amounts.
	 */
	@Test
	public void testPopulatePrice() {
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class)).thenAnswer(invocation -> new PriceImpl());
		when(beanFactory.getPrototypeBean(ContextIdNames.PRICE_TIER, PriceTier.class)).thenAnswer(invocation -> new PriceTierImpl());

		Collection<BaseAmount> amounts = new ArrayList<>();
		amounts.add(createBaseAmount("PL", "1", "9.99", "4.99", "PROD",
				BaseAmountObjectType.PRODUCT.toString()));
		amounts.add(createBaseAmount("PL", "5", "8.99", "3.99", "PROD",
				BaseAmountObjectType.PRODUCT.toString()));

		Currency currency = Currency.getInstance(Locale.CANADA);
		Price price = beanFactory.getPrototypeBean(ContextIdNames.PRICE, Price.class);
		boolean found = pricePopulator.populatePriceFromBaseAmounts(amounts, currency, price);
		assertThat(found).isTrue();
		assertThat(price.getPriceTiers()).hasSize(2);

		assertThat(price.getListPrice(1)).isEqualTo(Money.valueOf("9.99", currency));
		assertThat(price.getSalePrice(1)).isEqualTo(Money.valueOf("4.99", currency));

		assertThat(price.getListPrice(TEN)).isEqualTo(Money.valueOf("8.99", currency));
		assertThat(price.getSalePrice(TEN)).isEqualTo(Money.valueOf("3.99", currency));
	}


	private BaseAmount createBaseAmount(final String priceListGuid,
			final String quantity, final String list, final String sale,
			final String objectGuid, final String objectType) {
		BaseAmountImpl baseAmount = new BaseAmountImpl();
		baseAmount.setPriceListDescriptorGuid(priceListGuid);
		baseAmount.setObjectGuid(objectGuid);
		baseAmount.setObjectType(objectType);
		baseAmount.setSaleValue(new BigDecimal(sale));
		baseAmount.setListValue(new BigDecimal(list));
		baseAmount.setQuantity(new BigDecimal(quantity));
		return baseAmount;
	}

}
