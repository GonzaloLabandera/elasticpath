/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.converters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeValue;
import com.elasticpath.domain.catalog.BundleConstituent;
import com.elasticpath.domain.catalog.ConstituentItem;
import com.elasticpath.domain.catalog.ProductBundle;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.store.Store;
import com.elasticpath.xpf.connectivity.context.XPFProductSkuValidationContext;
import com.elasticpath.xpf.connectivity.entity.XPFAttributeValue;
import com.elasticpath.xpf.context.builders.impl.ProductSkuValidationContextBuilderImpl;

@RunWith(MockitoJUnitRunner.class)
public class XPFConverterUtilTest {
	@Mock
	private AttributeValue attributeValue1, attributeValue2, attributeValue3;
	@Mock
	private Attribute attribute1, attribute2;
	@Mock
	private XPFAttributeValue contextAttributeValue1, contextAttributeValue2, contextAttributeValue3;
	@Mock
	private ProductSku productSku, constituentItemProductSku1, constituentItemProductSku2;
	@Mock
	private ProductBundle productBundle;
	@Mock
	private BundleConstituent bundleConstituent1, bundleConstituent2, bundleConstituent3;
	@Mock
	private ConstituentItem constituentItem1, constituentItem2;
	@Mock
	private Shopper shopper;
	@Mock
	private Store store;
	@Mock
	private XPFProductSkuValidationContext productSkuValidationContext1, productSkuValidationContext2;
	@Mock
	private AttributeValueConverter attributeValueConverter;
	@Mock
	private ProductSkuValidationContextBuilderImpl productSkuValidationContextBuilder;

	@InjectMocks
	@Spy
	private XPFConverterUtil xpfConverterUtil;

	@Test
	public void testConvertToContextAttributeValues() {
		String localizedAttributeKey1 = "key1_en";
		String localizedAttributeKey2 = "key2_en";
		String localizedAttributeKey3 = "key2";
		List<AttributeValue> attributeValueList = Lists.newArrayList(attributeValue1, attributeValue2, attributeValue3);
		when(attributeValue1.getLocalizedAttributeKey()).thenReturn(localizedAttributeKey1);
		when(attributeValue2.getLocalizedAttributeKey()).thenReturn(localizedAttributeKey2);
		when(attributeValue3.getLocalizedAttributeKey()).thenReturn(localizedAttributeKey3);
		when(attributeValue1.getAttribute()).thenReturn(attribute1);
		when(attributeValue2.getAttribute()).thenReturn(attribute2);
		when(attributeValue3.getAttribute()).thenReturn(attribute2);
		when(attribute1.getKey()).thenReturn("key1");
		when(attribute2.getKey()).thenReturn("key2");

		when(attributeValueConverter.convert(new StoreDomainContext<>(attributeValue1, Optional.empty()))).thenReturn(contextAttributeValue1);
		when(attributeValueConverter.convert(new StoreDomainContext<>(attributeValue2, Optional.empty()))).thenReturn(contextAttributeValue2);
		when(attributeValueConverter.convert(new StoreDomainContext<>(attributeValue3, Optional.empty())))
				.thenReturn(contextAttributeValue3);

		Map<Locale, Map<String, XPFAttributeValue>> expectedAttributeValueMap = new HashMap<>();
		expectedAttributeValueMap.put(Locale.ENGLISH,
				ImmutableMap.of("KEY1", contextAttributeValue1, "KEY2", contextAttributeValue2));
		expectedAttributeValueMap.put(null, ImmutableMap.of("KEY2", contextAttributeValue3));

		// Test convertToContextAttributeValues with list input
		Map<Locale, Map<String, XPFAttributeValue>> contextAttributeValues =
				xpfConverterUtil.convertToXpfAttributeValues(attributeValueList, Optional.empty());

		assertEquals(expectedAttributeValueMap, contextAttributeValues);

		// Test convertToContextAttributeValues with map input
		Map<String, AttributeValue> attributeValueMap = new HashMap<>();
		attributeValueMap.put(localizedAttributeKey1, attributeValue1);
		attributeValueMap.put(localizedAttributeKey2, attributeValue2);
		attributeValueMap.put(localizedAttributeKey3, attributeValue3);

		contextAttributeValues = xpfConverterUtil.convertToXpfAttributeValues(attributeValueMap, Optional.empty());
		assertEquals(expectedAttributeValueMap, contextAttributeValues);
	}

	@Test
	public void testConvertValidationContextChildren() {
		when(productSku.getProduct()).thenReturn(productBundle);
		when(productBundle.getConstituents()).thenReturn(Lists.newArrayList(bundleConstituent1, bundleConstituent2, bundleConstituent3));
		when(productBundle.isConstituentAutoSelectable(bundleConstituent1)).thenReturn(true);
		when(productBundle.isConstituentAutoSelectable(bundleConstituent2)).thenReturn(true);
		when(productBundle.isConstituentAutoSelectable(bundleConstituent3)).thenReturn(false);
		when(bundleConstituent1.getConstituent()).thenReturn(constituentItem1);
		when(bundleConstituent2.getConstituent()).thenReturn(constituentItem2);
		when(constituentItem1.getProductSku()).thenReturn(constituentItemProductSku1);
		when(constituentItem2.getProductSku()).thenReturn(constituentItemProductSku2);
		when(productSkuValidationContextBuilder.build(constituentItemProductSku1, productSku, shopper, store))
				.thenReturn(productSkuValidationContext1);
		when(productSkuValidationContextBuilder.build(constituentItemProductSku2, productSku, shopper, store))
				.thenReturn(productSkuValidationContext2);

		List<XPFProductSkuValidationContext> contextProductSkuValidationContextList =
				xpfConverterUtil.getProductConstituentsAsValidationContexts(productSku, shopper, store);

		assertEquals(Lists.newArrayList(productSkuValidationContext1, productSkuValidationContext2), contextProductSkuValidationContextList);
		}
}