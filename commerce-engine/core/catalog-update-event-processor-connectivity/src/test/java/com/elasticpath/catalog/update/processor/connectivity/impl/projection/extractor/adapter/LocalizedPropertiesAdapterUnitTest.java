/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.catalog.update.processor.connectivity.impl.projection.extractor.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.misc.impl.SkuOptionLocalizedPropertyValueImpl;

/**
 * Test for {@link LocalizedPropertiesAdapter}.
 */
public class LocalizedPropertiesAdapterUnitTest {

	/**
	 * Adapter get LocalizedProperties with en and fr values, and return map, that contain Locales en, fr as key,
	 * and en_value, fr_value as value.
	 */
	@Test
	public void ensureThatAdapterReturnLocaleByValueMapThatContainEnAndFrValues() {
		final Map<String, LocalizedPropertyValue> localizedPropertiesMap = new HashMap<>();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueEn = new SkuOptionLocalizedPropertyValueImpl();
		final SkuOptionLocalizedPropertyValueImpl skuOptionLocalizedPropertyValueFr = new SkuOptionLocalizedPropertyValueImpl();

		skuOptionLocalizedPropertyValueEn.setValue("en_value");
		skuOptionLocalizedPropertyValueFr.setValue("fr_value");
		localizedPropertiesMap.put("localizedProperty_en", skuOptionLocalizedPropertyValueEn);
		localizedPropertiesMap.put("localizedProperty_fr", skuOptionLocalizedPropertyValueFr);
		LocalizedProperties localizedProperties = new LocalizedPropertiesImpl();

		final String beanId = ContextIdNames.SKU_OPTION_VALUE;
		localizedProperties.setLocalizedPropertiesMap(localizedPropertiesMap, beanId);

		final LocalizedPropertiesAdapter adapter = new LocalizedPropertiesAdapter(Locale.getDefault(), localizedProperties);

		Map<Locale, String> resultMap = adapter.getCatalogLocaleByValue();
		String expectedResultEn = localizedPropertiesMap.get("localizedProperty_en").getValue();
		String expectedResultFr = localizedPropertiesMap.get("localizedProperty_fr").getValue();

		assertThat(resultMap).contains(entry(new Locale("fr"), expectedResultFr));
		assertThat(resultMap).contains(entry(new Locale("en"), expectedResultEn));

	}
}
