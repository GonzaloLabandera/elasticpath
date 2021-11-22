/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Price;
import com.elasticpath.money.Money;
import com.elasticpath.xpf.connectivity.entity.XPFPrice;

@RunWith(MockitoJUnitRunner.class)
public class PriceConverterTest {

	@Mock
	private Price price;

	@InjectMocks
	private PriceConverter priceConverter;

	@Test
	public void testConvert() {
		Currency currency = Currency.getInstance("USD");
		BigDecimal amount = BigDecimal.valueOf(1L).setScale(currency.getDefaultFractionDigits());
		when(price.getListPrice()).thenReturn(Money.valueOf("1", currency));
		when(price.getSalePrice()).thenReturn(Money.valueOf("1", currency));
		when(price.getCurrency()).thenReturn(currency);

		XPFPrice contextPrice = priceConverter.convert(price);

		assertEquals(amount, contextPrice.getUnitListPrice());
		assertEquals(amount, contextPrice.getUnitSalePrice());
		assertEquals(currency, contextPrice.getCurrency());
	}
}