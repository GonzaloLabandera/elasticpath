/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.attribute.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.AttributeMultiValueType;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.impl.CatalogImpl;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.LocalizedPropertyValue;
import com.elasticpath.domain.misc.impl.AttributeLocalizedPropertyValueImpl;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.test.BeanFactoryExpectationsFactory;
import com.elasticpath.test.jmock.AbstractEPTestCase;

/**
 * Test <code>AttributeImpl</code>.
 */
public class AttributeImplTest extends AbstractEPTestCase {
	private static final String TEST_ATTRIBUTE_NAME_1 = "test attribute 1";

	private static final String TEST_ATTRIBUTE_NAME_2 = "test attribute 2";

	private static final String TEST_ATTRIBUTE_NAME_3 = "test attribute 3";

	private static final String TEST_ATTRIBUTE_NAME = "test attribute";

	private static final long UID_PK_1 = 1;

	private static final long UID_PK_2 = 9999;

	private static final String TEST_ATTRIBUTE_KEY2 = "test key 2";

	private static final String TEST_ATTRIBUTE_KEY1 = "test key 1";

	private static final String TEST_ATTRIBUTE_KEY = "test key";

	private static final Locale TEST_LOCALE = Locale.ENGLISH;

	private static final Locale THAI_LOCALE_WITH_VARIANT = new Locale("th", "TH", "TH");

	private static final Locale THAI_LOCALE_NO_VARIANT = new Locale("th", "TH");

	private AttributeImpl attributeImpl1;

	private AttributeImpl attributeImpl2;

	/**
	 * Prepare for tests.
	 *
	 * @throws Exception in case of error
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		BeanFactoryExpectationsFactory bfef = getBeanFactoryExpectationsFactory();

		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.ATTRIBUTE_USAGE, AttributeUsage.class, AttributeUsageImpl.class);
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.LOCALIZED_PROPERTIES, LocalizedProperties.class, LocalizedPropertiesImpl.class);
		bfef.allowingBeanFactoryGetPrototypeBean(ContextIdNames.ATTRIBUTE_LOCALIZED_PROPERTY_VALUE, LocalizedPropertyValue.class,
				AttributeLocalizedPropertyValueImpl.class);

		attributeImpl1 = new AttributeImpl();
		attributeImpl2 = new AttributeImpl();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.getKey()'.
	 */
	@Test
	public void testGetKey() {
		assertNotNull(attributeImpl1.getKey());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setKey(String)'.
	 */
	@Test
	public void testSetKey() {
		final String key1 = "key1";
		attributeImpl1.setKey(key1);
		assertSame(key1, attributeImpl1.getKey());
		assertSame(key1, attributeImpl1.getGuid());

		final String key2 = "key2";
		attributeImpl1.setGuid(key2);
		assertSame(key2, attributeImpl1.getKey());
		assertSame(key2, attributeImpl1.getGuid());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.isLocaleDependant()'.
	 */
	@Test
	public void testIsLocaleDependant() {
		assertFalse(attributeImpl1.isLocaleDependant());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setLocaleDependant(boolean)'.
	 */
	@Test
	public void testSetLocaleDependant() {
		attributeImpl1.setLocaleDependant(true);
		assertTrue(attributeImpl1.isLocaleDependant());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.getAttributeType()'.
	 */
	@Test
	public void testGetAttributeType() {
		assertNull(attributeImpl1.getAttributeType());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setAttributeType(AttributeType)'.
	 */
	@Test
	public void testSetAttributeType() {
		attributeImpl1.setAttributeType(AttributeType.SHORT_TEXT);
		assertSame(AttributeType.SHORT_TEXT, attributeImpl1.getAttributeType());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.getDisplayName()'.
	 */
	@Test
	public void testGetName() {
		assertEquals("", attributeImpl1.getDisplayName(TEST_LOCALE));
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setDisplayName(String)'.
	 */
	@Test
	public void testSetName() {
		final String name = "name";
		attributeImpl1.setDisplayName(name, TEST_LOCALE);
		assertSame(name, attributeImpl1.getDisplayName(TEST_LOCALE));
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.compareTo()'.
	 */
	@Test
	public void testCompareTo() {

		// New categories are always dealed as the same
		assertEquals(0, attributeImpl1.compareTo(attributeImpl2));

		// compare by name
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_1, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME_2, TEST_LOCALE);
		assertTrue(attributeImpl1.compareTo(attributeImpl2) < 0);

		// compare by key
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY1);
		attributeImpl2.setKey(TEST_ATTRIBUTE_KEY2);
		assertTrue(attributeImpl1.compareTo(attributeImpl2) < 0);

		// compare by uid
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl2.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl1.setUidPk(UID_PK_1);
		attributeImpl2.setUidPk(UID_PK_2);
		assertTrue(attributeImpl1.compareTo(attributeImpl2) < 0);

		// compare the same one
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl2.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl1.setUidPk(UID_PK_1);
		attributeImpl2.setUidPk(UID_PK_1);
		assertEquals(0, attributeImpl1.compareTo(attributeImpl2));
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.isRequired()'.
	 */
	@Test
	public void testIsRequired() {
		assertFalse(attributeImpl1.isRequired());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setRequired(boolean)'.
	 */
	@Test
	public void testSetRequired() {
		attributeImpl1.setRequired(true);
		assertTrue(attributeImpl1.isRequired());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.isValueLookupEnabled()'.
	 */
	@Test
	public void testValueLookupEnabled() {
		assertFalse(attributeImpl1.isValueLookupEnabled());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setValueLookupEnabled(boolean)'.
	 */
	@Test
	public void testSetValueLookupEnabled() {
		attributeImpl1.setValueLookupEnabled(true);
		assertTrue(attributeImpl1.isValueLookupEnabled());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.getAttributeUsage()'.
	 */
	@Test
	public void testGetAttributeUsage() {
		assertNull(attributeImpl1.getAttributeUsage());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setAttributeUsage(AttributeUsage)'.
	 */
	@Test
	public void testSetAttributeUsage() {
		AttributeUsage attributeUsage = AttributeUsageImpl.getAttributeUsageByIdInternal(1);
		attributeImpl1.setAttributeUsage(attributeUsage);
		assertSame(attributeUsage, attributeImpl1.getAttributeUsage());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.isMultiValueEnabled()'.
	 */
	@Test
	public void testIsMultiValueEnabled() {
		assertFalse(attributeImpl1.isMultiValueEnabled());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.setMultiValueEnabled(multiValueEnabled)'.
	 */
	@Test
	public void testSetMultiValueEnabled() {
		attributeImpl1.setMultiValueType(AttributeMultiValueType.LEGACY);
		assertTrue(attributeImpl1.isMultiValueEnabled());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.equals(object)'.
	 */
	@Test
	public void testEquals() {
		// New attributes are equal
		assertEquals(attributeImpl1, attributeImpl2);

		// compare the same one
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl2.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl1.setUidPk(UID_PK_1);
		attributeImpl2.setUidPk(UID_PK_1);
		assertEquals(attributeImpl1, attributeImpl2);

		// uidpk should be ignored
		attributeImpl1.setLocaleDependant(true);
		attributeImpl2.setLocaleDependant(true);
		attributeImpl1.setMultiValueType(AttributeMultiValueType.LEGACY);
		attributeImpl2.setMultiValueType(AttributeMultiValueType.LEGACY);
		attributeImpl1.setRequired(true);
		attributeImpl2.setRequired(true);
		attributeImpl1.setSystem(true);
		attributeImpl2.setSystem(true);
		attributeImpl1.setValueLookupEnabled(true);
		attributeImpl2.setValueLookupEnabled(true);
		attributeImpl1.setAttributeType(AttributeType.SHORT_TEXT);
		attributeImpl2.setAttributeType(AttributeType.SHORT_TEXT);
		attributeImpl1.setAttributeUsageId(1);
		attributeImpl2.setAttributeUsageId(1);
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl2.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl1.setUidPk(UID_PK_1);
		attributeImpl2.setUidPk(0);
		assertEquals(attributeImpl1, attributeImpl2);

		// symmetric
		assertEquals(attributeImpl2, attributeImpl1);

		// equals itself
		assertEquals(attributeImpl1, attributeImpl1);

		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY1);
		assertNotEquals(attributeImpl1, attributeImpl2);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.AttributeImpl.hashCode())'.
	 */
	@Test
	public void testHashCode() {
		// needs a mock because setAttributeTypeId and setAttributeSu
		// New attributes are equal
		assertEquals(attributeImpl1.hashCode(), attributeImpl2.hashCode());

		// compare the same one
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl2.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl1.setUidPk(UID_PK_1);
		attributeImpl2.setUidPk(UID_PK_1);
		assertEquals(attributeImpl1.hashCode(), attributeImpl2.hashCode());

		// uidpk should be ignored
		attributeImpl1.setLocaleDependant(true);
		attributeImpl2.setLocaleDependant(true);
		attributeImpl1.setMultiValueType(AttributeMultiValueType.LEGACY);
		attributeImpl2.setMultiValueType(AttributeMultiValueType.LEGACY);
		attributeImpl1.setRequired(true);
		attributeImpl2.setRequired(true);
		attributeImpl1.setSystem(true);
		attributeImpl2.setSystem(true);
		attributeImpl1.setValueLookupEnabled(true);
		attributeImpl2.setValueLookupEnabled(true);
		attributeImpl1.setAttributeType(AttributeType.SHORT_TEXT);
		attributeImpl2.setAttributeType(AttributeType.SHORT_TEXT);
		attributeImpl1.setAttributeUsageId(1);
		attributeImpl2.setAttributeUsageId(1);
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl2.setDisplayName(TEST_ATTRIBUTE_NAME, TEST_LOCALE);
		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl2.setKey(TEST_ATTRIBUTE_KEY);
		attributeImpl1.setUidPk(UID_PK_1);
		attributeImpl2.setUidPk(0);
		assertEquals(attributeImpl1.hashCode(), attributeImpl2.hashCode());

		// equals itself
		assertEquals(attributeImpl1.hashCode(), attributeImpl1.hashCode());

		attributeImpl1.setKey(TEST_ATTRIBUTE_KEY1);
		assertNotSame(attributeImpl1.hashCode(), attributeImpl2.hashCode());
	}

	protected Catalog createCatalogWithDefaultLocale(final Locale locale) {
		Catalog catalog = new CatalogImpl();
		catalog.setDefaultLocale(locale);
		return catalog;
	}

	@Test
	public void testGetDisplayName() {
		attributeImpl1.setCatalog(createCatalogWithDefaultLocale(Locale.GERMAN));

		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_1, Locale.FRENCH);
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_2, THAI_LOCALE_NO_VARIANT);
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_3, Locale.GERMAN);
		// Mismatching locale, no fallback
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH)).isBlank();
		// Locale broadening
		assertThat(attributeImpl1.getDisplayName(Locale.CANADA_FRENCH)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
		// Locale broadening with variant
		assertThat(attributeImpl1.getDisplayName(THAI_LOCALE_WITH_VARIANT)).isEqualTo(TEST_ATTRIBUTE_NAME_2);
		// Match
		assertThat(attributeImpl1.getDisplayName(Locale.FRENCH)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
	}

	@Test
	public void testGetDisplayNameWithFallbackNullCatalog() {
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_1, Locale.FRENCH);
		// Use mismatching locale to trigger fallback
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH, false, true)).isBlank();
	}

	@Test
	public void testGetDisplayNameWithNoBroadeningNoFallback() {
		attributeImpl1.setCatalog(createCatalogWithDefaultLocale(Locale.GERMAN));

		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_1, Locale.FRENCH);
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_2, THAI_LOCALE_NO_VARIANT);
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_3, Locale.GERMAN);
		// Mismatching locale
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH, false, false)).isBlank();
		// Partial locale broadening implemented in LocalizedPropertiesImpl
		assertThat(attributeImpl1.getDisplayName(Locale.CANADA_FRENCH, false, false)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
		// Variants only picked up if locale broadening is requested
		assertThat(attributeImpl1.getDisplayName(THAI_LOCALE_WITH_VARIANT, false, false)).isBlank();
		// Match
		assertThat(attributeImpl1.getDisplayName(Locale.FRENCH, false, false)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
		// Mismatching and no fallback
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH, false, false)).isBlank();
	}

	@Test
	public void testGetDisplayNameWithNoBroadeningWithFallback() {
		attributeImpl1.setCatalog(createCatalogWithDefaultLocale(Locale.GERMAN));

		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_1, Locale.FRENCH);
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_2, THAI_LOCALE_NO_VARIANT);

		// No locale broadening requested, but partial locale broadening implemented in LocalizedPropertiesImpl
		assertThat(attributeImpl1.getDisplayName(Locale.CANADA_FRENCH, false, true)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
		// Variants only picked up if locale broadening is requested
		assertThat(attributeImpl1.getDisplayName(THAI_LOCALE_WITH_VARIANT, false, false)).isBlank();
		// Match
		assertThat(attributeImpl1.getDisplayName(Locale.FRENCH, false, true)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
		// Mismatching and no fallback match
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH, false, true)).isBlank();

		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_2, Locale.GERMAN);
		// Mismatching locale, hits fallback
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH, false, true)).isEqualTo(TEST_ATTRIBUTE_NAME_2);
	}

	@Test
	public void testGetDisplayNameWithBroadeningAndFallback() {
		attributeImpl1.setCatalog(createCatalogWithDefaultLocale(Locale.GERMAN));
		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_1, Locale.FRENCH);

		// Locale broadening is considered before fallback
		assertThat(attributeImpl1.getDisplayName(Locale.CANADA_FRENCH, true, true)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
		// Match
		assertThat(attributeImpl1.getDisplayName(Locale.FRENCH, true, true)).isEqualTo(TEST_ATTRIBUTE_NAME_1);
		// Mismatching and no fallback match
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH, true, true)).isBlank();
		// Add displayName matching with catalog locale

		attributeImpl1.setDisplayName(TEST_ATTRIBUTE_NAME_2, Locale.GERMAN);
		// Mismatching locale, hits fallback
		assertThat(attributeImpl1.getDisplayName(Locale.ENGLISH, true, true)).isEqualTo(TEST_ATTRIBUTE_NAME_2);
	}
}
