/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.catalog.SelectionRule;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFBundleConstituent;
import com.elasticpath.xpf.connectivity.entity.XPFCategory;
import com.elasticpath.xpf.connectivity.entity.XPFProductBundle;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;

@SuppressWarnings("PMD.TooManyFields")
@RunWith(MockitoJUnitRunner.class)
public class ProductBundleConverterTest {
	private static final String CODE = "code";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DISPLAY_NAME_FR = "frenchDisplayName";

	@Mock
	private CategoryConverter categoryConverter;
	@Mock
	private ProductTypeConverter xpfProductTypeConverter;
	@Mock
	private XPFConverterUtil xpfConverterUtil;
	@Mock
	private BundleConstituentConverter bundleConstituentConverter;
	@Mock
	private ProductBundle productBundle;
	@Mock
	private Category category1, category2;
	@Mock
	private XPFCategory contextCategory1, contextCategory2;
	@Mock
	private BundleConstituent bundleConstituent1, bundleConstituent2;
	@Mock
	private XPFBundleConstituent
			contextBundleConstituent1, contextBundleConstituent2;
	@Mock
	private ProductType productType;
	@Mock
	private XPFProductType contextProductType;
	@Mock
	private Map<String, AttributeValue> attributeValueMap;
	@Mock
	private SelectionRule selectionRule;
	@Mock
	private Map<Locale, Map<String, XPFAttributeValue>> contextAttributeValues;

	@InjectMocks
	private ProductBundleConverter productBundleConverter;

	@Mock
	private Store store;

	private final Date date = new Date();

	private final Set<Category> categories = Sets.newHashSet(category1, category2);
	private final Set<XPFCategory> contextCategories = Sets.newHashSet(contextCategory1, contextCategory2);
	private final List<BundleConstituent> bundleConstituents = Lists.newArrayList(bundleConstituent1, bundleConstituent2);
	private final List<XPFBundleConstituent> contextBundleConstituents =
			Lists.newArrayList(contextBundleConstituent1, contextBundleConstituent2);

	@Before
	public void setup() {
		when(productBundle.getCode()).thenReturn(CODE);
		when(productBundle.getStartDate()).thenReturn(date);
		when(productBundle.getEndDate()).thenReturn(date);

		when(productBundle.getDisplayName(Locale.ENGLISH)).thenReturn(DISPLAY_NAME);
		when(productBundle.getDisplayName(Locale.FRENCH)).thenReturn(DISPLAY_NAME_FR);
		when(xpfConverterUtil.getLocalesForStore(Optional.empty())).thenReturn(Collections.singleton(Locale.ENGLISH));
		when(xpfConverterUtil.getLocalesForStore(Optional.of(store))).thenReturn(Collections.singleton(Locale.FRENCH));

		when(productBundle.getCategories()).thenReturn(categories);
		when(productBundle.getProductType()).thenReturn(productType);
		lenient().when(categoryConverter.convert(new StoreDomainContext<>(category1, Optional.empty())))
				.thenReturn(contextCategory1);
		lenient().when(categoryConverter.convert(new StoreDomainContext<>(category2, Optional.empty())))
				.thenReturn(contextCategory2);

		when(xpfProductTypeConverter.convert(productType)).thenReturn(contextProductType);

		when(productBundle.getAttributeValueMap()).thenReturn(attributeValueMap);
		when(xpfConverterUtil.convertToXpfAttributeValues(attributeValueMap, Optional.empty()))
				.thenReturn(contextAttributeValues);
		when(xpfConverterUtil.convertToXpfAttributeValues(attributeValueMap, Optional.of(store)))
				.thenReturn(contextAttributeValues);

		when(productBundle.getConstituents()).thenReturn(bundleConstituents);
		lenient().when(bundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent1, Optional.empty())))
				.thenReturn(contextBundleConstituent1);
		lenient().when(bundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent2, Optional.empty())))
				.thenReturn(contextBundleConstituent2);
		lenient().when(bundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent1, Optional.of(store))))
				.thenReturn(contextBundleConstituent1);
		lenient().when(bundleConstituentConverter.convert(new StoreDomainContext<>(bundleConstituent2, Optional.of(store))))
				.thenReturn(contextBundleConstituent2);

		when(productBundle.isNotSoldSeparately()).thenReturn(true);
		when(productBundle.isHidden()).thenReturn(true);
		when(productBundle.getSelectionRule()).thenReturn(selectionRule);
		when(selectionRule.getParameter()).thenReturn(1);
	}

	@Test
	public void testConvertWithoutStore() {
		XPFProductBundle contextProductBundle = productBundleConverter.convert(new StoreDomainContext<>(productBundle, Optional.empty()));

		assertEquals(CODE, contextProductBundle.getCode());
		assertEquals(Collections.singletonMap(Locale.ENGLISH, DISPLAY_NAME), contextProductBundle.getDisplayNames());
		assertEquals(date.toInstant(), contextProductBundle.getStartDate());
		assertEquals(date.toInstant(), contextProductBundle.getEndDate());
		assertEquals(contextCategories, contextProductBundle.getCategories());
		assertEquals(contextAttributeValues, contextProductBundle.getAttributeValues());
		assertTrue(contextProductBundle.isBundle());
		assertTrue(contextProductBundle.isNotSoldSeparately());
		assertTrue(contextProductBundle.isHidden());
		assertEquals(contextBundleConstituents, contextProductBundle.getConstituents());
		assertEquals(Long.valueOf(1L), contextProductBundle.getMaxConstituentSelections());
		assertEquals(Long.valueOf(1L), contextProductBundle.getMinConstituentSelections());
	}

	@Test
	public void testConvertWithStore() {
		XPFProductBundle contextProductBundle = productBundleConverter.convert(new StoreDomainContext<>(productBundle, store));

		assertEquals(CODE, contextProductBundle.getCode());
		assertEquals(Collections.singletonMap(Locale.FRENCH, DISPLAY_NAME_FR), contextProductBundle.getDisplayNames());
		assertEquals(date.toInstant(), contextProductBundle.getStartDate());
		assertEquals(date.toInstant(), contextProductBundle.getEndDate());
		assertEquals(contextCategories, contextProductBundle.getCategories());
		assertEquals(contextAttributeValues, contextProductBundle.getAttributeValues());
		assertTrue(contextProductBundle.isBundle());
		assertTrue(contextProductBundle.isNotSoldSeparately());
		assertTrue(contextProductBundle.isHidden());
		assertEquals(contextBundleConstituents, contextProductBundle.getConstituents());
		assertEquals(Long.valueOf(1L), contextProductBundle.getMaxConstituentSelections());
		assertEquals(Long.valueOf(1L), contextProductBundle.getMinConstituentSelections());
	}
}