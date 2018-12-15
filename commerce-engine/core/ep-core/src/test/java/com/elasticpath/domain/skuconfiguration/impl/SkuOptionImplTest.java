/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.skuconfiguration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.skuconfiguration.SkuOption;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;

/**
 * Test cases for {@link SkuOptionImpl}.
 */
@SuppressWarnings({"PMD.TooManyStaticImports" })
@RunWith(MockitoJUnitRunner.class)
public class SkuOptionImplTest {

	private static final String KEY1 = "key1";

	private static final String TEST_STRING = "testString";

	private static final String VALUE_CODE_1 = "value_code_1";

	private static final String VALUE_CODE_2 = "value_code_2";
	
	private static final Locale CATALOG_DEFAULT_LOCALE = Locale.GERMANY;
	private static final String DISPLAYNAME_DEFAULT_LOCALE = "default locale display name";
	private static final Locale OTHER_LOCALE = Locale.ITALIAN;
	private static final String DISPLAYNAME_OTHER_LOCALE = "other locale display name";

	@Mock
	private ElasticPath elasticPath;

	private SkuOptionImpl skuOption;

	/**
	 * Prepare for the tests.
	 */
	@Before
	public void setUp() {
		skuOption = new SkuOptionImpl() {
			private static final long serialVersionUID = 1L;

			@Override
			public ElasticPath getElasticPath() {
				return elasticPath;
			}
		};

		final Catalog catalog = mock(Catalog.class);
		when(catalog.getDefaultLocale()).thenReturn(CATALOG_DEFAULT_LOCALE);
		skuOption.setCatalog(catalog);
	}

	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl.getName(Locale)'.
	 */
	@Test
	public void testGetSetName() {
		skuOption.setOptionKey(TEST_STRING);
		assertThat(skuOption.getOptionKey()).isEqualTo(TEST_STRING);
	}

	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl.getOptionValueMap()'.
	 */
	@Test
	public void testGetSetOptionValueMap() {
		Map<String, SkuOptionValue> optionValueMap = getOptionValueMap();
		skuOption.setOptionValueMap(optionValueMap);
		assertThat(skuOption.getOptionValueMap()).isEqualTo(optionValueMap);
	}

	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl.getOptionValues()'.
	 */
	@Test
	public void testGetSetOptionValues() {
		Set<SkuOptionValue> optionValues = getOptionValueSet();
		skuOption.setOptionValues(optionValues);
		assertThat(skuOption.getOptionValueMap()).hasSize(1);
		assertThat(skuOption.contains(VALUE_CODE_1)).isTrue();
	}

	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl.addOptionValue(SkuOptionValue)'.
	 */
	@Test
	public void testAddOptionValue() {
		Map<String, SkuOptionValue> optionValues = getOptionValueMap();
		int numOptionValues = optionValues.size();
		skuOption.setOptionValueMap(optionValues);

		final SkuOptionValue newValue = mock(SkuOptionValue.class);
		when(newValue.getOptionValueKey()).thenReturn(VALUE_CODE_2);
		when(newValue.getSkuOption()).thenReturn(null);

		skuOption.addOptionValue(newValue);
		verify(newValue).setSkuOption(skuOption);
		assertThat(skuOption.getOptionValues()).hasSize(numOptionValues + 1);
	}
	
	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl.contains(SkuOptionValueCode)'.
	 */
	@Test
	public void testContains() {
		Map<String, SkuOptionValue> optionValues = getOptionValueMap();
		skuOption.setOptionValueMap(optionValues);

		assertThat(skuOption.contains(VALUE_CODE_1)).isTrue();
		assertThat(skuOption.contains(VALUE_CODE_2)).isFalse();
	}

	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionImpl.getOptionValue(SkuOptionValueCode)'.
	 */
	@Test
	public void testGetOptionValue() {
		Map<String, SkuOptionValue> optionValues = getOptionValueMap();
		skuOption.setOptionValueMap(optionValues);

		assertThat(skuOption.getOptionValue(VALUE_CODE_1)).isNotNull();
		assertThat(skuOption.getOptionValue(VALUE_CODE_2)).isNull();
	}

	private Map<String, SkuOptionValue> getOptionValueMap() {
		Map<String, SkuOptionValue> valueSet = new HashMap<>();

		final SkuOptionValue skuOptionValue = mock(SkuOptionValue.class, "SkuOptionValue-" + VALUE_CODE_1);
		when(skuOptionValue.getOptionValueKey()).thenReturn(VALUE_CODE_1);

		valueSet.put(VALUE_CODE_1, skuOptionValue);
		return valueSet;
	}

	private Set<SkuOptionValue> getOptionValueSet() {
		return new HashSet<>(getOptionValueMap().values());
	}

	/**
	 * Test method for 'com.elasticpath.domain.catalog.impl.SkuOptionImpl.setOptionKey(String)'.
	 */
	@Test
	public void testSetKey() {
		assertThat(skuOption.getOptionKey()).isNull();

		final String key1 = KEY1;
		skuOption.setOptionKey(key1);
		assertThat(skuOption.getOptionKey()).isEqualTo(key1);
		assertThat(skuOption.getGuid()).isEqualTo(key1);

		final String key2 = "key2";
		skuOption.setGuid(key2);
		assertThat(skuOption.getOptionKey()).isEqualTo(key2);
		assertThat(skuOption.getGuid()).isEqualTo(key2);
	}
	
	private void setupLocalizedDisplayNames(final boolean includeOtherLocale) {
		LocalizedProperties localizedProperties = mock(LocalizedProperties.class);
		final String skuOptionDisplayName = "skuOptionDisplayName";

		when(elasticPath.getBean(ContextIdNames.LOCALIZED_PROPERTIES)).thenReturn(localizedProperties);
		when(localizedProperties.getValue(skuOptionDisplayName, CATALOG_DEFAULT_LOCALE)).thenReturn(DISPLAYNAME_DEFAULT_LOCALE);

		if (includeOtherLocale) {
			when(localizedProperties.getValue(skuOptionDisplayName, OTHER_LOCALE)).thenReturn(DISPLAYNAME_OTHER_LOCALE);
		} else {
			when(localizedProperties.getValue(skuOptionDisplayName, OTHER_LOCALE)).thenReturn(null);
		}

		skuOption.setDisplayName(DISPLAYNAME_DEFAULT_LOCALE, CATALOG_DEFAULT_LOCALE);
		if (includeOtherLocale) {
			skuOption.setDisplayName(DISPLAYNAME_OTHER_LOCALE, OTHER_LOCALE);
		}

		if (includeOtherLocale) {
			verify(localizedProperties).setValue(skuOptionDisplayName, OTHER_LOCALE, DISPLAYNAME_OTHER_LOCALE);
		} else {
			verify(localizedProperties).setValue(skuOptionDisplayName, CATALOG_DEFAULT_LOCALE, DISPLAYNAME_DEFAULT_LOCALE);
		}
	}
	
	/**
	 * Test that getDisplayName will fall back to the default locale if
	 * the display name doesn't exist in the given locale and we're allowing fallbacks.
	 * Also tests that if fallbacks are forbidden then it will NOT fall back to the
	 * default locale.
	 */
	@Test
	public void testGetDisplayNameFallsBackIfNecessaryButNotIfForbidden() {
		setupLocalizedDisplayNames(false);
			
		assertThat(skuOption.getDisplayName(OTHER_LOCALE, true))
			.as("Should fall back to default locale when asked for display name for non-existent locale and fallback is set to true")
			.isEqualTo(DISPLAYNAME_DEFAULT_LOCALE);

		assertThat(skuOption.getDisplayName(OTHER_LOCALE, false))
			.as("Should not fall back if forbidden")
			.isNull();
	}
	
	/**
	 * Test that getDisplayName doesn't fall back to the default locale if it
	 * finds the display name in the given locale.
	 */
	@Test
	public void testGetDisplayNameDoesNotFallBackIfNotNecessary() {
		setupLocalizedDisplayNames(true);

		assertThat(skuOption.getDisplayName(OTHER_LOCALE, true))
			.as("Should not fall back if not necessary")
			.isEqualTo(DISPLAYNAME_OTHER_LOCALE);
	}
	
	/**
	 * Tests that hashCode() works for hash based collections.
	 */
	@Test
	public void testHashCode() {
		SkuOption skuOption1 = new SkuOptionImpl();
		// test two objects with no data defined for them
		SkuOption skuOption2 = new SkuOptionImpl();
		Set<SkuOption> set = new HashSet<>();
		set.add(skuOption1);
		set.add(skuOption2);
		
		// the sku options are identical as they do not have anything set yet
		assertThat(set).hasSize(1);

		// checks if one of the objects have different value for one of the attributes
		skuOption1.setOptionKey(KEY1);
		skuOption2.setOptionKey(null);
		set.clear();
		
		set.add(skuOption1);
		set.add(skuOption2);
		
		assertThat(set).containsExactlyInAnyOrder(skuOption1, skuOption2);

		// make the two objects equal
		skuOption2.setOptionKey(KEY1);
		set.clear();

		set.add(skuOption1);
		set.add(skuOption2);
		
		assertThat(set)
			.contains(skuOption1)
			.contains(skuOption2)
			.hasSize(1);
	}

	/**
	 * Tests that equals() behaves in the expected way.
	 */
	@Test
	public void testEquals() {
		SkuOption skuOption1 = new SkuOptionImpl();
		// test two objects with no data defined for them
		SkuOptionImpl skuOption2 = new SkuOptionImpl();

		assertThat(skuOption1).isEqualTo(skuOption2);

		skuOption2.setOptionKey(KEY1);
		assertThat(skuOption1).isNotEqualTo(skuOption2);
		assertThat(skuOption2).isNotEqualTo(skuOption1);
	}
}
