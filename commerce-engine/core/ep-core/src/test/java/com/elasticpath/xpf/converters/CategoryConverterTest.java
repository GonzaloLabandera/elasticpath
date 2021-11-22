/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCatalog;
import com.elasticpath.xpf.connectivity.entity.XPFCategory;

@RunWith(MockitoJUnitRunner.class)
public class CategoryConverterTest {
	private static final String CODE = "code";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DISPLAY_NAME_FR = "frenchDisplayName";

	@Mock
	private Catalog catalog;
	@Mock
	private XPFCatalog contextCatalog;
	@Mock
	private Category category;
	@Mock
	private CatalogConverter catalogConverter;
	@Mock
	private Map<String, AttributeValue> attributeValueMap;
	@Mock
	private Map<Locale, Map<String, XPFAttributeValue>> contextAttributeValues;
	@Mock
	private XPFConverterUtil xpfConverterUtil;

	@InjectMocks
	private CategoryConverter categoryConverter;

	@Mock
	private Store store;

	@Before
	public void setup() {
		when(category.getCode()).thenReturn(CODE);
		when(category.getDisplayName(Locale.ENGLISH)).thenReturn(DISPLAY_NAME);
		when(category.getDisplayName(Locale.FRENCH)).thenReturn(DISPLAY_NAME_FR);
		when(category.getCatalog()).thenReturn(catalog);
		when(catalogConverter.convert(catalog)).thenReturn(contextCatalog);
		when(xpfConverterUtil.getLocalesForStore(Optional.empty())).thenReturn(Collections.singleton(Locale.ENGLISH));
		when(xpfConverterUtil.getLocalesForStore(Optional.of(store))).thenReturn(Collections.singleton(Locale.FRENCH));

		when(category.getAttributeValueMap()).thenReturn(attributeValueMap);
		when(xpfConverterUtil.convertToXpfAttributeValues(attributeValueMap, Optional.empty()))
				.thenReturn(contextAttributeValues);
		when(xpfConverterUtil.convertToXpfAttributeValues(attributeValueMap, Optional.of(store)))
				.thenReturn(contextAttributeValues);
	}

	@Test
	public void testConvert() {
		XPFCategory contextCategory = categoryConverter.convert(new StoreDomainContext<>(category, Optional.empty()));

		assertEquals(CODE, contextCategory.getCode());
		assertEquals(Collections.singletonMap(Locale.ENGLISH, DISPLAY_NAME), contextCategory.getDisplayNames());
		assertEquals(contextAttributeValues, contextCategory.getAttributeValues());
		assertEquals(contextCatalog, contextCategory.getCatalog());
	}

	@Test
	public void testConvertWithStore() {
		XPFCategory contextCategory = categoryConverter.convert(new StoreDomainContext<>(category, store));

		assertEquals(CODE, contextCategory.getCode());
		assertEquals(Collections.singletonMap(Locale.FRENCH, DISPLAY_NAME_FR), contextCategory.getDisplayNames());
		assertEquals(contextAttributeValues, contextCategory.getAttributeValues());
		assertEquals(contextCatalog, contextCategory.getCatalog());
	}
}