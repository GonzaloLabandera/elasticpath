/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.skuconfiguration.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.elasticpath.commons.util.Utility;
import com.elasticpath.commons.util.impl.UtilityImpl;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.BrandLocalizedPropertyValueImpl;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.skuconfiguration.SkuOptionValue;

/**
 * Test cases for <code>SkuOptionValue</code>.
 */
public class SkuOptionValueImplTest {

	private static final String COMMON_GUID = "COMMON_GUID";

	private static final String TEST_STRING = "testString";

	private static final int TEST_INT = 27;

	private SkuOptionValueImpl skuOptionValue;

	private static final Locale CATALOG_DEFAULT_LOCALE = Locale.GERMANY;
	private static final String DISPLAYNAME_DEFAULT_LOCALE = "default locale display name";

	private static final Locale OTHER_LOCALE = Locale.ITALIAN;
	private static final String DISPLAYNAME_OTHER_LOCALE = "other locale display name";

	@Before
	public void setUp() throws Exception {
		skuOptionValue = new SkuOptionValueImpl();
	}

	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl.getOptionCode()'.
	 */
	@Test
	public void testGetSetOptionCode() {
		skuOptionValue.setOptionValueKey(TEST_STRING);
		assertThat(skuOptionValue.getOptionValueKey()).isEqualTo(TEST_STRING);
	}

	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl.getSmallImage()'.
	 */
	@Test
	public void testGetSetImage() {
		skuOptionValue.setImage("TEST_STRING");
		assertThat(skuOptionValue.getImage()).isEqualTo("TEST_STRING");
	}


	/**
	 * Test method for 'com.elasticpath.domain.skuconfiguration.impl.SkuOptionValueImpl.getOrder()'.
	 */
	@Test
	public void testGetSetOrder() {
		skuOptionValue.setOrdering(TEST_INT);
		assertThat(skuOptionValue.getOrdering()).isEqualTo(TEST_INT);
	}

	/**
	 * Test that getDisplayName() falls back when asked to do so.
	 */
	@Test
	public void testGetDisplayNameFallsBackIfNecessary() {
		final SkuOptionValueImpl skuOptionValue = createSkuOptionValueWithOnlyDefaultLocaleDisplayName();
		skuOptionValue.setDisplayName(CATALOG_DEFAULT_LOCALE, DISPLAYNAME_DEFAULT_LOCALE);
		assertThat(skuOptionValue.getDisplayName(OTHER_LOCALE, true))
			.as("Should fall back to default locale's display name since display name for other locale non-existent")
			.isEqualTo(DISPLAYNAME_DEFAULT_LOCALE);
	}

	/**
	 * Test that getDisplayName() doesn't fall back when the correct value exists,
	 * even though it could.
	 */
	@Test
	public void testGetDisplayNameDoesNotFallBackIfNotNecessary() {
		final SkuOptionValueImpl skuOptionValue = createSkuOptionValueWithDisplayNameInDefaultAndOtherLocale();
		skuOptionValue.setDisplayName(CATALOG_DEFAULT_LOCALE, DISPLAYNAME_DEFAULT_LOCALE);
		skuOptionValue.setDisplayName(OTHER_LOCALE, DISPLAYNAME_OTHER_LOCALE);
		assertThat(skuOptionValue.getDisplayName(OTHER_LOCALE, true))
			.as("Should not fall back since it doesn't have to")
			.isEqualTo(DISPLAYNAME_OTHER_LOCALE);
	}

	/**
	 * Test that getDisplayName doesn't fall back if it's not supposed to.
	 */
	@Test
	public void testGetDisplayNameDoesNotFallBackIfForbidden() {
		final SkuOptionValueImpl skuOptionValue = createSkuOptionValueWithOnlyDefaultLocaleDisplayName();
		assertThat(skuOptionValue.getDisplayName(OTHER_LOCALE, false))
			.as("Should not fall back if forbidden")
			.isNull();
	}

	private SkuOptionValueImpl createSkuOptionValueWithDisplayNameInDefaultAndOtherLocale() {
		return new SkuOptionValueImpl() {
			private static final long serialVersionUID = -5082836448820498157L;

			@Override
			public LocalizedProperties getLocalizedProperties() {
				final LocalizedProperties localizedProperties = new LocalizedPropertiesImpl() {
					private static final long serialVersionUID = 8267811593561001044L;

					@Override
					protected LocalizedPropertyValue getNewLocalizedPropertyValue() {
						return new BrandLocalizedPropertyValueImpl(); // arbitrary implementation
					}

					@Override
					public Utility getUtility() {
						return new UtilityImpl();
					}
				};

				localizedProperties.setValue(SkuOptionValueImpl.LOCALIZED_PROPERTY_DISPLAY_NAME, CATALOG_DEFAULT_LOCALE, DISPLAYNAME_DEFAULT_LOCALE);
				localizedProperties.setValue(SkuOptionValueImpl.LOCALIZED_PROPERTY_DISPLAY_NAME, OTHER_LOCALE, DISPLAYNAME_OTHER_LOCALE);
				return localizedProperties;
			}

			@Override
			protected Locale getMasterCatalogLocale() {
				return CATALOG_DEFAULT_LOCALE;
			}
		};
	}

	private SkuOptionValueImpl createSkuOptionValueWithOnlyDefaultLocaleDisplayName() {
		return new SkuOptionValueImpl() {
			private static final long serialVersionUID = 7996850480495524696L;

			@Override
			public LocalizedProperties getLocalizedProperties() {
				final LocalizedProperties localizedProperties = new LocalizedPropertiesImpl() {
					private static final long serialVersionUID = 9069602459685662545L;

					@Override
					protected LocalizedPropertyValue getNewLocalizedPropertyValue() {
						return new BrandLocalizedPropertyValueImpl(); // arbitrary implementation
					}

					@Override
					public Utility getUtility() {
						return new UtilityImpl();
					}
				};

				localizedProperties.setValue(SkuOptionValueImpl.LOCALIZED_PROPERTY_DISPLAY_NAME, CATALOG_DEFAULT_LOCALE, DISPLAYNAME_DEFAULT_LOCALE);
				return localizedProperties;
			}

			@Override
			protected Locale getMasterCatalogLocale() {
				return CATALOG_DEFAULT_LOCALE;
			}
		};
	}

	/**
	 * Tests that hashCode() works for hash based collections.
	 */
	@Test
	public void testHashCode() {
		final SkuOptionValue skuOption1 = new SkuOptionValueImpl();
		// test two objects with no data defined for them
		final SkuOptionValue skuOption2 = new SkuOptionValueImpl();
		final Set<SkuOptionValue> set = new HashSet<>();
		set.add(skuOption1);
		set.add(skuOption2);

		// the sku options are identical as they do not have anything set yet
		assertThat(set).hasSize(1);

		// checks if one of the objects have different value for one of the attributes
		skuOption1.setOptionValueKey("key1");
		skuOption2.setOptionValueKey(null);
		set.clear();

		set.add(skuOption1);
		set.add(skuOption2);

		assertThat(set).containsExactlyInAnyOrder(skuOption1, skuOption2);

		// make the two objects equal
		skuOption2.setOptionValueKey("key1");
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
		final SkuOptionValue skuOption1 = new SkuOptionValueImpl();
		// test two objects with no data defined for them
		final SkuOptionValue skuOption2 = new SkuOptionValueImpl();

		assertThat(skuOption1).isEqualTo(skuOption2);

		skuOption2.setOptionValueKey("key1");

		assertThat(skuOption1).isNotEqualTo(skuOption2);
		assertThat(skuOption2).isNotEqualTo(skuOption1);
	}

	/**
	 * Test symmetry of the SkuOptionValue equals method.
	 */
	@Test
	public void testEqualsSymmetric() {

		final SkuOptionValue skuOptionValue1 = new SkuOptionValueImpl();
		skuOptionValue1.setGuid(COMMON_GUID);

		final SkuOptionValueImpl skuOptionValue2 = new SkuOptionValueImpl();
		skuOptionValue2.setGuid(COMMON_GUID);

		assertThat(skuOptionValue2).isEqualTo(skuOptionValue1);
		assertThat(skuOptionValue1).isEqualTo(skuOptionValue2);

		// create an instance that shares the same parent entity equals method
		final Attribute attribute = new AttributeImpl();
		attribute.setGuid(COMMON_GUID);

		assertThat(attribute).isNotEqualTo(skuOptionValue1);
		assertThat(skuOptionValue1).isNotEqualTo(attribute);
	}

	/**
	 * Test the null case of the SkuOptionValue equals method.
	 */
	@Test
	public void testEqualsNull() {
		final SkuOptionValue skuOptionValue1 = new SkuOptionValueImpl();
		skuOptionValue1.setGuid(COMMON_GUID);

		final SkuOptionValueImpl skuOptionValue2 = null;

		assertThat(skuOptionValue2).isNotEqualTo(skuOptionValue1);
	}



}
