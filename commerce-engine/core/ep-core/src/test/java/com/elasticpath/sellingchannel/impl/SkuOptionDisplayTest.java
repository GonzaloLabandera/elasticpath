/**
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.sellingchannel.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;
import com.elasticpath.sellingchannel.presentation.impl.SkuOptionDisplay;

/**
 * Tests for SkuOptionDisplayTest.
 */
@RunWith(MockitoJUnitRunner.class)
public class SkuOptionDisplayTest {

	private static final String SKU_OPTION_KEY_1 = "skuOptionKey1";
	private static final String SKU_OPTION_KEY_2 = "skuOptionKey2";
	private static final String SKU_OPTION_VALUE_KEY_1 = "skuOptionValueKey1";
	private static final String SKU_OPTION_VALUE_KEY_2 = "skuOptionValueKey2";
	private static final String SKU_OPTION_VALUE_DISPLAY_NAME = "skuOptionValueDisplayName";
	private static final Locale LOCALE = Locale.ENGLISH;

	private final Collection<SkuOptionValue> valueList = new ArrayList<SkuOptionValue>();

	@Mock
	ProductSku productSku;

	@Mock
	SkuOption skuOption1;

	@Mock
	SkuOption skuOption2;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	SkuOptionValue skuOptionValue1;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	SkuOptionValue skuOptionValue2;

	@Mock
	LocalizedProperties localizedProperties1;

	@Mock
	LocalizedProperties localizedProperties2;

	/**
	 * Prepare for the test.
	 */
	@Before
	public void init() {
		when(localizedProperties1.getValue(SKU_OPTION_VALUE_DISPLAY_NAME, LOCALE))
				.thenReturn(SKU_OPTION_VALUE_KEY_1);
		when(localizedProperties2.getValue(SKU_OPTION_VALUE_DISPLAY_NAME, LOCALE))
				.thenReturn(SKU_OPTION_VALUE_KEY_2);

		when(skuOptionValue1.getLocalizedProperties()).thenReturn(localizedProperties1);
		when(skuOptionValue1.getSkuOption()).thenReturn(skuOption1);
		when(skuOptionValue1.getLocalizedProperties().getValue(SKU_OPTION_VALUE_DISPLAY_NAME, LOCALE))
				.thenReturn(SKU_OPTION_VALUE_KEY_1);

		when(skuOptionValue2.getLocalizedProperties()).thenReturn(localizedProperties2);
		when(skuOptionValue2.getSkuOption()).thenReturn(skuOption2);
		when(skuOptionValue2.getLocalizedProperties().getValue(SKU_OPTION_VALUE_DISPLAY_NAME, LOCALE))
				.thenReturn(SKU_OPTION_VALUE_KEY_2);

		valueList.add(skuOptionValue1);
		valueList.add(skuOptionValue2);

		when(skuOption1.getOptionKey()).thenReturn(SKU_OPTION_KEY_1);
		when(skuOption2.getOptionKey()).thenReturn(SKU_OPTION_KEY_2);
		when(productSku.getOptionValues()).thenReturn(valueList);
	}

	@Test
	public void testGetSkuOptionDisplayName() {
		assertThat(SkuOptionDisplay.getSkuOptionDisplayName(productSku, LOCALE, SKU_OPTION_KEY_1))
				.as("check sku option value 1")
				.isEqualTo(SKU_OPTION_VALUE_KEY_1);
		assertThat(SkuOptionDisplay.getSkuOptionDisplayName(productSku, LOCALE, SKU_OPTION_KEY_2))
				.as("check sku option value 2")
				.isEqualTo(SKU_OPTION_VALUE_KEY_2);
	}

	@Test
	public void testGetFilteredSkuDisplay() {
		String skuOptionValues = SKU_OPTION_VALUE_KEY_1.concat(",").concat(SKU_OPTION_VALUE_KEY_2);

		assertThat(SkuOptionDisplay.getFilteredSkuDisplay(productSku, LOCALE))
				.as("check sku option values")
				.isEqualTo(skuOptionValues);
	}

}
