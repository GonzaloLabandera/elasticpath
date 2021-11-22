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
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOption;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOptionValue;

@RunWith(MockitoJUnitRunner.class)
public class ProductSkuOptionValueConverterTest {
	private static final String KEY = "key";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DISPLAY_NAME_FR = "frenchDisplayName";

	@Mock
	private SkuOptionValue skuOptionValue;
	@Mock
	private SkuOption skuOption;
	@Mock
	private XPFProductSkuOption productSkuOption;
	@Mock
	private ProductSkuOptionConverter productSkuOptionConverter;
	@Mock
	private XPFConverterUtil xpfConverterUtil;

	@InjectMocks
	private ProductSkuOptionValueConverter productSkuOptionValueConverter;

	@Mock
	private Store store;

	@Before
	public void setup() {
		when(skuOptionValue.getOptionValueKey()).thenReturn(KEY);
		when(skuOptionValue.getDisplayName(Locale.ENGLISH, false)).thenReturn(DISPLAY_NAME);
		when(skuOptionValue.getDisplayName(Locale.FRENCH, false)).thenReturn(DISPLAY_NAME_FR);
		when(xpfConverterUtil.getLocalesForStore(Optional.empty())).thenReturn(Collections.singleton(Locale.ENGLISH));
		when(xpfConverterUtil.getLocalesForStore(Optional.of(store))).thenReturn(Collections.singleton(Locale.FRENCH));
		when(skuOptionValue.getSkuOption()).thenReturn(skuOption);
		when(productSkuOptionConverter.convert(new StoreDomainContext<>(skuOption, Optional.empty()))).thenReturn(productSkuOption);
		when(productSkuOptionConverter.convert(new StoreDomainContext<>(skuOption, Optional.of(store)))).thenReturn(productSkuOption);
	}

	@Test
	public void testConvertWithoutStore() {
		XPFProductSkuOptionValue contextProductSkuOptionValue =
				productSkuOptionValueConverter.convert(new StoreDomainContext<>(skuOptionValue, Optional.empty()));

		assertEquals(KEY, contextProductSkuOptionValue.getKey());
		assertEquals(Collections.singletonMap(Locale.ENGLISH, DISPLAY_NAME), contextProductSkuOptionValue.getDisplayNames());
		assertEquals(productSkuOption, contextProductSkuOptionValue.getProductSkuOption());
	}

	@Test
	public void testConvertWithStore() {
		XPFProductSkuOptionValue contextProductSkuOptionValue =
				productSkuOptionValueConverter.convert(new StoreDomainContext<>(skuOptionValue, store));

		assertEquals(KEY, contextProductSkuOptionValue.getKey());
		assertEquals(Collections.singletonMap(Locale.FRENCH, DISPLAY_NAME_FR), contextProductSkuOptionValue.getDisplayNames());
	}
}