/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFCategory;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductType;

@RunWith(MockitoJUnitRunner.class)
public class ProductConverterTest {
	private static final String CODE = "code";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DISPLAY_NAME_FR = "frenchDisplayName";

	@Mock
	private Product product;
	@Mock
	private com.elasticpath.domain.catalog.Category category1, category2;
	@Mock
	private XPFCategory contextCategory1, contextCategory2;
	@Mock
	private Map<String, com.elasticpath.domain.attribute.AttributeValue> attributeValueMap;
	@Mock
	private Map<Locale, Map<String, XPFAttributeValue>> contextAttributeValues;
	@Mock
	private CategoryConverter categoryConverter;
	@Mock
	private XPFConverterUtil xpfConverterUtil;
	@Mock
	private Store store;
	@Mock
	private ProductType productType;
	@Mock
	private XPFProductType xpfProductType;
	@Mock
	private ProductTypeConverter xpfProductTypeConverter;

	@InjectMocks
	private ProductConverter productConverter;

	private final Date date = new Date();
	private Set<com.elasticpath.domain.catalog.Category> categories;
	private Set<XPFCategory> contextCategories;

	@Before
	public void setup() {
		categories = Sets.newHashSet(category1, category2);
		contextCategories = Sets.newHashSet(contextCategory1, contextCategory2);
		when(categoryConverter.convert(new StoreDomainContext<>(category1, Optional.empty()))).thenReturn(contextCategory1);
		when(categoryConverter.convert(new StoreDomainContext<>(category2, Optional.empty()))).thenReturn(contextCategory2);
		when(xpfConverterUtil.getLocalesForStore(Optional.empty())).thenReturn(Collections.singleton(Locale.ENGLISH));
		when(xpfConverterUtil.convertToXpfAttributeValues(attributeValueMap, Optional.empty())).thenReturn(contextAttributeValues);
		when(xpfProductTypeConverter.convert(productType)).thenReturn(xpfProductType);

		when(product.getCode()).thenReturn(CODE);
		when(product.getDisplayName(Locale.ENGLISH)).thenReturn(DISPLAY_NAME);
		when(product.getDisplayName(Locale.FRENCH)).thenReturn(DISPLAY_NAME_FR);
		when(product.getStartDate()).thenReturn(date);
		when(product.getEndDate()).thenReturn(date);
		when(product.getCategories()).thenReturn(categories);
		when(product.getAttributeValueMap()).thenReturn(attributeValueMap);
		when(product.isNotSoldSeparately()).thenReturn(false);
		when(product.isHidden()).thenReturn(false);
		when(product.getProductType()).thenReturn(productType);
	}

	@Test
	public void testConvertWithoutStore() {
		XPFProduct contextProduct = productConverter.convert(new StoreDomainContext<>(product, Optional.empty()));

		assertEquals(CODE, contextProduct.getCode());
		assertEquals(Collections.singletonMap(Locale.ENGLISH, DISPLAY_NAME), contextProduct.getDisplayNames());
		assertEquals(date.toInstant(), contextProduct.getStartDate());
		assertEquals(date.toInstant(), contextProduct.getEndDate());
		assertEquals(contextCategories, contextProduct.getCategories());
		assertEquals(contextAttributeValues, contextProduct.getAttributeValues());
		assertFalse(contextProduct.isBundle());
		assertFalse(contextProduct.isNotSoldSeparately());
		assertFalse(contextProduct.isHidden());
	}

	@Test
	public void testConvertWithStore() {
		when(xpfConverterUtil.getLocalesForStore(Optional.of(store))).thenReturn(Collections.singleton(Locale.FRENCH));

		XPFProduct contextProduct = productConverter.convert(new StoreDomainContext<>(product, store));

		assertEquals(CODE, contextProduct.getCode());
		assertEquals(Collections.singletonMap(Locale.FRENCH, DISPLAY_NAME_FR), contextProduct.getDisplayNames());

	}
}