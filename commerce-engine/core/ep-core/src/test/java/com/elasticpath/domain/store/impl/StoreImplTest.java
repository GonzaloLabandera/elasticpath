/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.store.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.domain.misc.SupportedCurrency;
import com.elasticpath.domain.misc.SupportedLocale;

/**
 * Tests for various methods of the StoreImpl class.
 */
public class StoreImplTest {

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	/**
	 * Test that getting the supported locales results in an unmodifiable collection.
	 */
	@Test
	public void testGetSupportedLocalesIsUnmodifiable() {
		StoreImpl store = new StoreImpl() {
			private static final long serialVersionUID = 1473756577002186326L;

			@Override
			protected Set<SupportedLocale> getSupportedLocalesInternal() {
				return new HashSet<>();
			}
		};
		try {
			store.getSupportedLocales().add(Locale.US);
			fail("Should not be allowed to modify the collection of returned locales");
		} catch (UnsupportedOperationException ex) {
			assertNotNull(ex);
		}
	}

	/**
	 * Test that getting the supported currencies results in an unmodifiable collection.
	 */
	@Test
	public void testGetSupportedCurrenciesIsUnmodifiable() {
		StoreImpl store = new StoreImpl() {
			private static final long serialVersionUID = 1381843899210938176L;

			@Override
			protected Set<SupportedCurrency> getSupportedCurrenciesInternal() {
				return new HashSet<>();
			}
		};

		try {
			store.getSupportedCurrencies().add(Currency.getInstance(Locale.US));
			fail("Should not be allowed to modify the collection of returned currencies");
		} catch (UnsupportedOperationException ex) {
			assertNotNull(ex);
		}
	}

	/**
	 * Test that you can only set a default locale if that locale
	 * is already in the collection of locales supported by the underlying catalog,
	 * otherwise throw an illegal argument exception.
	 */
	@Test
	public void testSetDefaultLocaleMustBeSupportedLocale() {
		final Set<SupportedLocale> supportedLocales = new HashSet<>();
		SupportedLocale locale = new StoreLocaleImpl();
		locale.setLocale(Locale.US);
		supportedLocales.add(locale);

		StoreImpl store = new StoreImpl() {
			private static final long serialVersionUID = -3068113024945951542L;

			@Override
			protected Set<SupportedLocale> getSupportedLocalesInternal() {
				return supportedLocales;
			}
		};
		//Test that set default locale works
		assertTrue(store.getSupportedLocales().contains(Locale.US));
		store.setDefaultLocale(Locale.US);
		assertEquals(Locale.US, store.getDefaultLocale());
	}

	/**
	 * Test that you can only set a default currency if that currency
	 * is already in the collection of currencies supported by the underlying catalog,
	 * otherwise throw an illegal argument exception.
	 */
	@Test
	public void testSetDefaultCurrencyMustBeSupportedCurrency() {
		Currency usCurrency = Currency.getInstance(Locale.US);
		final Set<SupportedCurrency> supportedCurrencies = new HashSet<>();
		SupportedCurrency supportedCurrency = new StoreCurrencyImpl();
		supportedCurrency.setCurrency(usCurrency);
		supportedCurrencies.add(supportedCurrency);

		StoreImpl store = new StoreImpl() {
			private static final long serialVersionUID = -9044292213354004163L;

			@Override
			protected Set<SupportedCurrency> getSupportedCurrenciesInternal() {
				return supportedCurrencies;
			}
		};
		//Test that set default currency works
		assertTrue(store.getSupportedCurrencies().contains(usCurrency));
		store.setDefaultCurrency(usCurrency);
		assertEquals(usCurrency, store.getDefaultCurrency());
	}

}
