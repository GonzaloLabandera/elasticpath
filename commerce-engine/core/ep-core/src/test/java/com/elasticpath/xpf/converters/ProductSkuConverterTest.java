/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
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

import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.Product;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.connectivity.entity.XPFProduct;
import com.elasticpath.xpf.connectivity.entity.XPFProductSku;
import com.elasticpath.xpf.connectivity.entity.XPFProductSkuOptionValue;

@RunWith(MockitoJUnitRunner.class)
public class ProductSkuConverterTest {
	private static final String SKU_CODE = "skuCode";
	private static final String DISPLAY_NAME = "displayName";
	private static final String DISPLAY_NAME_FR = "frenchDisplayName";

	@Mock
	private Store store;
	@Mock
	private ProductSku productSku;
	@Mock
	private Product product;
	@Mock
	private XPFProduct contextProduct;
	@Mock
	private SkuOptionValue skuOptionValue1, skuOptionValue2;
	@Mock
	private XPFProductSkuOptionValue productSkuOptionValue1, productSkuOptionValue2;
	@Mock
	private ProductSkuOptionValueConverter productSkuOptionValueConverter;
	@Mock
	private XPFConverterUtil xpfConverterUtil;
	@Mock
	private ProductConverter productConverter;
	@Mock
	private Map<String, AttributeValue> attributeValueMap;
	@Mock
	private Map<Locale, Map<String, XPFAttributeValue>> contextAttributeValues;

	@InjectMocks
	private ProductSkuConverter productSkuConverter;

	private final Date date = new Date();
	private Set<XPFProductSkuOptionValue> productSkuOptionValues;
	private Collection<SkuOptionValue> skuOptionValues;


	@Before
	public void setup() {
		when(productSku.getProduct()).thenReturn(product);
		when(productConverter.convert(new StoreDomainContext<>(product, (Store) any()))).thenReturn(contextProduct);
		when(productSku.getSkuCode()).thenReturn(SKU_CODE);
		when(productSku.getDisplayName(Locale.ENGLISH)).thenReturn(DISPLAY_NAME);
		when(productSku.getDisplayName(Locale.FRENCH)).thenReturn(DISPLAY_NAME_FR);
		when(xpfConverterUtil.getLocalesForStore(Optional.empty())).thenReturn(Collections.singleton(Locale.ENGLISH));

		skuOptionValues = Sets.newHashSet(skuOptionValue1, skuOptionValue2);
		productSkuOptionValues = Sets.newHashSet(productSkuOptionValue1, productSkuOptionValue2);

		when(productSku.getOptionValues()).thenReturn(skuOptionValues);
		when(productSkuOptionValueConverter.convert(new StoreDomainContext<>(skuOptionValue1, Optional.empty())))
				.thenReturn(productSkuOptionValue1);
		when(productSkuOptionValueConverter.convert(new StoreDomainContext<>(skuOptionValue2, Optional.empty())))
				.thenReturn(productSkuOptionValue2);


		when(productSku.getAttributeValueMap()).thenReturn(attributeValueMap);
		when(xpfConverterUtil.convertToXpfAttributeValues(attributeValueMap, Optional.empty()))
				.thenReturn(contextAttributeValues);

		when(productSku.getEffectiveStartDate()).thenReturn(date);
		when(productSku.getEffectiveEndDate()).thenReturn(date);
		when(productSku.isShippable()).thenReturn(false);
	}

	@Test
	public void testConvertWithNoStore() {

		XPFProductSku contextProductSku = productSkuConverter.convert(new StoreDomainContext<>(productSku, Optional.empty()));

		assertEquals(contextProduct, contextProductSku.getProduct());
		assertEquals(SKU_CODE, contextProductSku.getCode());
		assertEquals(Collections.singletonMap(Locale.ENGLISH, DISPLAY_NAME), contextProductSku.getDisplayNames());
		assertEquals(productSkuOptionValues, contextProductSku.getOptionValues());
		assertEquals(contextAttributeValues, contextProductSku.getAttributeValues());
		assertEquals(date.toInstant(), contextProductSku.getEffectiveStartDate());
		assertEquals(date.toInstant(), contextProductSku.getEffectiveEndDate());
		assertFalse(contextProductSku.isShippable());
	}

	@Test
	public void testConvertWithStore() {
		when(xpfConverterUtil.getLocalesForStore(Optional.of(store))).thenReturn(Collections.singleton(Locale.FRENCH));

		XPFProductSku contextProductSku = productSkuConverter.convert(new StoreDomainContext<>(productSku, store));

		assertEquals(contextProduct, contextProductSku.getProduct());
		assertEquals(SKU_CODE, contextProductSku.getCode());
		assertEquals(Collections.singletonMap(Locale.FRENCH, DISPLAY_NAME_FR), contextProductSku.getDisplayNames());
	}
}