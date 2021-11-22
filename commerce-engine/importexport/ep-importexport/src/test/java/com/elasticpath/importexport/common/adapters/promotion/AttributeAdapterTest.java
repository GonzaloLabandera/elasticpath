/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.importexport.common.adapters.promotion;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.common.dto.DisplayValue;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.importexport.common.adapters.catalogs.AttributeAdapter;
import com.elasticpath.importexport.common.dto.catalogs.AttributeDTO;
import com.elasticpath.importexport.common.dto.catalogs.AttributeMultiValueTypeType;
import com.elasticpath.importexport.common.dto.catalogs.AttributeTypeType;
import com.elasticpath.importexport.common.dto.catalogs.AttributeUsageType;

/**
 * Tests population of <code>AttributeDTO</code> from <code>Attribute</code> and back to front.
 */
public class AttributeAdapterTest {

	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();

	private final AttributeAdapter attributeAdapter = new AttributeAdapter();

	private static final String LANGUAGE = "en";

	private static final DisplayValue DISPLAY_VALUE = new DisplayValue(LANGUAGE, "attr_name");

	private static final Locale LANGUAGE_LOCALE = LocaleUtils.toLocale(LANGUAGE);

	/**
	 * Tests that all required getters are called during DTO population.
	 */
	@Test
	public void testPopulateDTO() {

		final Attribute attribute = context.mock(Attribute.class);
		final Catalog catalog  = context.mock(Catalog.class);

		final String attributeKey = "attributeKey";
		final AttributeUsage attributeUsage = AttributeUsageImpl.CATEGORY_USAGE;
		final AttributeType attributeType = AttributeType.SHORT_TEXT;
		final boolean localeDependent = true;
		final boolean isRequired = false;
		final AttributeMultiValueType attributeMultiValueType = AttributeMultiValueType.RFC_4180;
		final boolean isGlobal = false;

		context.checking(new Expectations() { {
			oneOf(attribute).getKey(); will(returnValue(attributeKey));
			oneOf(attribute).getDisplayName(LANGUAGE_LOCALE); will(returnValue(DISPLAY_VALUE.getValue()));
			allowing(attribute).getCatalog(); will(returnValue(catalog));
			oneOf(attribute).getAttributeUsage(); will(returnValue(attributeUsage));
			oneOf(attribute).getAttributeType(); will(returnValue(attributeType));
			oneOf(attribute).isLocaleDependant(); will(returnValue(localeDependent));
			oneOf(attribute).isRequired(); will(returnValue(isRequired));
			oneOf(attribute).getMultiValueType(); will(returnValue(attributeMultiValueType));
			oneOf(attribute).isGlobal(); will(returnValue(isGlobal));
			allowing(catalog).getSupportedLocales(); will(returnValue(Arrays.asList(LANGUAGE_LOCALE)));
		} });

		final AttributeDTO attributeDto = new AttributeDTO();

		attributeAdapter.populateDTO(attribute, attributeDto);

		assertEquals(attributeKey, attributeDto.getKey());
		final List<DisplayValue> nameValues = attributeDto.getNameValues();
		assertEquals(1, nameValues.size());
		assertEquals(LANGUAGE, nameValues.get(0).getLanguage());
		assertEquals(DISPLAY_VALUE.getValue(), nameValues.get(0).getValue());
		assertEquals(AttributeUsageType.valueOf(attributeUsage), attributeDto.getUsage());
		assertEquals(AttributeTypeType.valueOf(attributeType), attributeDto.getType());
		assertEquals(localeDependent, attributeDto.getMultiLanguage());
		assertEquals(isRequired, attributeDto.getRequired());
		assertEquals(AttributeMultiValueTypeType.valueOf(attributeMultiValueType), attributeDto.getMultivalue());
		assertEquals(isGlobal, attributeDto.getGlobal());
	}
}
