/**
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.xpf.extensions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Iterators;
import com.google.common.net.HttpHeaders;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.xpf.connectivity.context.XPFHttpTagSetContext;
import com.elasticpath.xpf.connectivity.entity.XPFStore;

@RunWith(MockitoJUnitRunner.class)
public class LocaleTagSetPopulatorTest {

	private static final String LOCALE = "LOCALE";

	@Mock
	private XPFStore store;
	@Mock
	private XPFHttpTagSetContext mockContext;
	@Mock
	private HttpServletRequest httpRequest;
	@InjectMocks
	private LocaleTagSetPopulator classUnderTest;

	@Before
	public void setUp() {
		when(mockContext.getHttpRequest()).thenReturn(httpRequest);
	}

	@Test
	public void testMissingStore() {
		Map<String, String> actual = classUnderTest.collectTagValues(mockContext);

		assertTrue(actual.isEmpty());
	}

	@Test
	public void testGetLocaleFromStore() {
		Locale expectedLocale = Locale.ENGLISH;
		setupStore(expectedLocale);
		Map<String, String> actualAttributes = classUnderTest.collectTagValues(mockContext);

		assertEquals(1, actualAttributes.size());
		assertEquals("Expected store locale.", expectedLocale.toLanguageTag(), actualAttributes.get(LOCALE));
	}

	@Test
	public void testBadLocaleStringInHeader() {
		Locale expectedLocale = Locale.ENGLISH;
		setupStore(expectedLocale);

		when(mockContext.getUserTraitValues()).thenReturn(TagSetPopulatorTestUtil.singletonCaseInsensitiveMap(LOCALE, "not a locale"));

		Map<String, String> actualAttributes = classUnderTest.collectTagValues(mockContext);

		assertEquals(1, actualAttributes.size());
		assertEquals("Expected store locale.", expectedLocale.toLanguageTag(), actualAttributes.get(LOCALE));
	}


	@Test
	public void testPreferLocaleFromAttributeHeader() {
		setupStore(Locale.FRENCH, Locale.CANADA_FRENCH, Locale.GERMAN);
		Locale expectedLocale = Locale.CANADA_FRENCH;
		setupAcceptLanguageHeader(Locale.GERMAN);
		when(mockContext.getUserTraitValues()).
				thenReturn(TagSetPopulatorTestUtil.singletonCaseInsensitiveMap(LOCALE, expectedLocale.toLanguageTag()));

		Map<String, String> actualAttributes = classUnderTest.collectTagValues(mockContext);

		assertEquals(1, actualAttributes.size());
		assertEquals("Expected attribute header locale over store or accept-language locale.",
				expectedLocale.toLanguageTag(), actualAttributes.get(LOCALE));
	}

	@Test
	public void testPreferLocaleFromAcceptLanguageWhenNoAttributeHeader() {
		setupStore(Locale.FRENCH, Locale.CANADA_FRENCH);
		Locale expectedLocale = Locale.CANADA_FRENCH;
		setupAcceptLanguageHeader(expectedLocale);
		when(mockContext.getUserTraitValues()).thenReturn(new CaseInsensitiveMap<>());
		Map<String, String> actualAttributes = classUnderTest.collectTagValues(mockContext);

		assertEquals(1, actualAttributes.size());
		assertEquals("Expected accept-language locale.", expectedLocale.toLanguageTag(), actualAttributes.get(LOCALE));
	}


	@Test
	public void testFindBestSupportedLocaleWhenRequestHasNoPreference() {
		Locale expected = Locale.CHINA;
		when(store.getDefaultLocale()).thenReturn(expected);

		Locale actual = classUnderTest.findBestSupportedLocale(Stream.empty(), store);

		assertEquals(expected, actual);
	}

	@Test
	public void testFindBestSupportedLocaleWhenRequestHasSamePreferenceAsStore() {
		Locale expected = Locale.CHINA;
		when(store.getSupportedLocales()).thenReturn(Collections.singleton(expected));

		Locale actual = classUnderTest.findBestSupportedLocale(Stream.of(expected), store);

		assertEquals(expected, actual);
	}

	@Test
	public void testFindBestSupportedLocaleWhenRequestHasDifferentPreferenceAsStore() {
		Locale expected = Locale.CHINA;
		when(store.getSupportedLocales()).thenReturn(Collections.singleton(expected));
		when(store.getDefaultLocale()).thenReturn(expected);

		Locale actual = classUnderTest.findBestSupportedLocale(Stream.of(Locale.CANADA_FRENCH), store);

		assertEquals(expected, actual);
	}

	@Test
	public void testFindBestLocaleWhenSubjectHasRegionalPreferenceAndStoreHasLanguage() {
		Locale expected = Locale.ENGLISH;
		when(store.getSupportedLocales()).thenReturn(Collections.singleton(expected));

		Locale actual = classUnderTest.findBestSupportedLocale(Stream.of(Locale.CANADA), store);

		assertEquals(expected, actual);
	}

	private void setupStore(final Locale... locales) {
		when(mockContext.getStore()).thenReturn(store);
		when(store.getSupportedLocales()).thenReturn(new HashSet<>(Arrays.asList(locales)));
		when(store.getDefaultLocale()).thenReturn(locales[0]);
	}

	private void setupAcceptLanguageHeader(final Locale locale) {
		when(httpRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE.toLowerCase(Locale.getDefault())))
				.thenReturn(locale.toLanguageTag());
		when(httpRequest.getLocales())
				.thenReturn(Iterators.asEnumeration(Iterators.forArray(locale)));
	}
}
