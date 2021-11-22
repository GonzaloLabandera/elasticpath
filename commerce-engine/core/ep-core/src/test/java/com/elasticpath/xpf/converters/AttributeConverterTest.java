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

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttribute;

@RunWith(MockitoJUnitRunner.class)
public class AttributeConverterTest {
	private static final String KEY = "key";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DISPLAY_NAME_FR = "frenchDisplayName";

	@Mock
	private Attribute attribute;

	@Mock
	private XPFConverterUtil xpfConverterUtil;

	@Mock
	private Store store;

	@InjectMocks
	private AttributeConverter attributeConverter;

	@Before
	public void setup() {
		when(attribute.getKey()).thenReturn(KEY);
		when(attribute.getDisplayName(Locale.ENGLISH)).thenReturn(DISPLAY_NAME);
		when(attribute.getDisplayName(Locale.FRENCH)).thenReturn(DISPLAY_NAME_FR);
		when(xpfConverterUtil.getLocalesForStore(Optional.empty())).thenReturn(Collections.singleton(Locale.ENGLISH));
		when(xpfConverterUtil.getLocalesForStore(Optional.of(store))).thenReturn(Collections.singleton(Locale.FRENCH));
	}

	@Test
	public void testConvertWithoutStore() {
		XPFAttribute contextAttribute = attributeConverter.convert(new StoreDomainContext<>(attribute, Optional.empty()));

		assertEquals(KEY, contextAttribute.getKey());
		assertEquals(Collections.singletonMap(Locale.ENGLISH, DISPLAY_NAME), contextAttribute.getDisplayNames());
	}

	@Test
	public void testConvertWithStore() {
		XPFAttribute contextAttribute = attributeConverter.convert(new StoreDomainContext<>(attribute, store));

		assertEquals(KEY, contextAttribute.getKey());
		assertEquals(Collections.singletonMap(Locale.FRENCH, DISPLAY_NAME_FR), contextAttribute.getDisplayNames());
	}
}