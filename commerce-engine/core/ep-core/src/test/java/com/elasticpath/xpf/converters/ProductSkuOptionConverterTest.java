/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOption;

@RunWith(MockitoJUnitRunner.class)
public class ProductSkuOptionConverterTest {
	private static final String KEY = "key";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DISPLAY_NAME_FR = "frenchDisplayName";

	@Mock
	private SkuOption skuOption;
	@Mock
	private XPFConverterUtil xpfConverterUtil;

	@InjectMocks
	private ProductSkuOptionConverter productSkuOptionConverter;

	@Mock
	private Store store;

	@Before
	public void setup() {
		when(skuOption.getOptionKey()).thenReturn(KEY);
		when(skuOption.getDisplayName(Locale.ENGLISH, false)).thenReturn(DISPLAY_NAME);
		when(skuOption.getDisplayName(Locale.FRENCH, false)).thenReturn(DISPLAY_NAME_FR);
		when(xpfConverterUtil.getLocalesForStore(Optional.empty())).thenReturn(Collections.singleton(Locale.ENGLISH));
		when(xpfConverterUtil.getLocalesForStore(Optional.of(store))).thenReturn(Collections.singleton(Locale.FRENCH));
	}

	@Test
	public void testConvertWithNoStore() {
		XPFProductSkuOption contextProductSkuOption = productSkuOptionConverter
				.convert(new StoreDomainContext<>(skuOption, Optional.empty()));

		assertEquals(KEY, contextProductSkuOption.getKey());
		assertEquals(Collections.singletonMap(Locale.ENGLISH, DISPLAY_NAME), contextProductSkuOption.getDisplayNames());
	}

	@Test
	public void testConvertWithStore() {
		XPFProductSkuOption contextProductSkuOption = productSkuOptionConverter.convert(new StoreDomainContext<>(skuOption, store));

		assertEquals(KEY, contextProductSkuOption.getKey());
		assertEquals(Collections.singletonMap(Locale.FRENCH, DISPLAY_NAME_FR), contextProductSkuOption.getDisplayNames());
	}
}
