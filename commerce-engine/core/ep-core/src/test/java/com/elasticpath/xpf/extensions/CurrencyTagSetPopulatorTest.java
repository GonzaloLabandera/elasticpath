/**
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.HashSet;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyTagSetPopulatorTest {

	private static final Currency CAD_CURRENCY = Currency.getInstance("CAD");
	private static final Currency USD_CURRENCY = Currency.getInstance("USD");
	private static final String EXPECTED_CURRENCY_DOES_NOT_MATCH_RESULT = "Expected currency does not match result.";
	private static final String HEADER_CURRENCY = "CURRENCY";

	@Mock
	private XPFStore store;
	@Mock
	private XPFHttpTagSetContext mockContext;

	@InjectMocks
	private CurrencyTagSetPopulator classUnderTest;

	@Test
	public void testMissingStore() {
		Map<String, String> actual = classUnderTest.collectTagValues(mockContext);

		assertTrue(actual.isEmpty());
	}

	@Test
	public void testGetCurrencyFromStore() {
		setupStore(CAD_CURRENCY);

		Map<String, String> actualAttributes = classUnderTest.collectTagValues(mockContext);

		assertEquals(1, actualAttributes.size());
		assertEquals(EXPECTED_CURRENCY_DOES_NOT_MATCH_RESULT, CAD_CURRENCY.getCurrencyCode(), actualAttributes.get(HEADER_CURRENCY));
	}

	@Test
	public void testBadCurrencyFromHeader() {
		setupStore(CAD_CURRENCY);
		when(mockContext.getUserTraitValues()).thenReturn(
				TagSetPopulatorTestUtil.singletonCaseInsensitiveMap(HEADER_CURRENCY, "not a currency code"));

		Map<String, String> actualAttributes = classUnderTest.collectTagValues(mockContext);

		assertEquals(1, actualAttributes.size());
		assertEquals(EXPECTED_CURRENCY_DOES_NOT_MATCH_RESULT, CAD_CURRENCY.getCurrencyCode(), actualAttributes.get(HEADER_CURRENCY));
	}

	@Test
	public void testPreferCurrencyFromHeader() {
		setupStore(USD_CURRENCY, CAD_CURRENCY);

		when(mockContext.getUserTraitValues()).thenReturn(
				TagSetPopulatorTestUtil.singletonCaseInsensitiveMap(HEADER_CURRENCY, CAD_CURRENCY.getCurrencyCode()));

		Map<String, String> actualAttributes = classUnderTest.collectTagValues(mockContext);

		assertEquals(EXPECTED_CURRENCY_DOES_NOT_MATCH_RESULT, CAD_CURRENCY.getCurrencyCode(), actualAttributes.get(HEADER_CURRENCY));
	}

	@Test
	public void testFindBestSupportedCurrencyWhenRequestHasNoPreference() {
		when(store.getDefaultCurrency()).thenReturn(USD_CURRENCY);

		Currency actual = classUnderTest.findBestSupportedCurrency(null, store);

		assertEquals(USD_CURRENCY, actual);
	}

	@Test
	public void testFindBestSupportedCurrencyWhenRequestHasSamePreferenceAsStore() {
		when(store.getSupportedCurrencies()).thenReturn(Collections.singleton(USD_CURRENCY));

		Currency actual = classUnderTest.findBestSupportedCurrency(USD_CURRENCY, store);

		assertEquals(USD_CURRENCY, actual);
	}

	@Test
	public void testFindBestCurrencyWhenRequestHasDifferentPreferenceAsStore() {
		when(store.getSupportedCurrencies()).thenReturn(new HashSet<>(Arrays.asList(CAD_CURRENCY, USD_CURRENCY)));

		Currency actual = classUnderTest.findBestSupportedCurrency(USD_CURRENCY, store);

		assertEquals(USD_CURRENCY, actual);
	}

	private void setupStore(final Currency... currencies) {
		when(mockContext.getStore()).thenReturn(store);
		when(store.getSupportedCurrencies()).thenReturn(new HashSet<>(Arrays.asList(currencies)));
		when(store.getDefaultCurrency()).thenReturn(currencies[0]);
	}
}
