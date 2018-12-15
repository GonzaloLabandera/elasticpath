/*
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.domain.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.DefaultValueRemovalForbiddenException;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;

/**
 * Tests for various methods of the CatalogImpl class.
 */
public class CatalogImplTest {

	private static final Locale DEFAULT_LOCALE = Locale.GERMANY;

	/**
	 * Test that setting a Default Locale adds the locale
	 * to the collection of Supported Locales if not already there.
	 */
	@Test
	public void testSetDefaultLocaleAddsSupportedLocale() {
		CatalogImpl catalog = getCatalog();
		catalog.setMaster(true);
		assertThat(catalog.getSupportedLocales()).doesNotContain(DEFAULT_LOCALE);

		catalog.setDefaultLocale(DEFAULT_LOCALE);
		assertThat(catalog.getSupportedLocales())
			.as("Adding a default locale should add to the supported locales")
			.contains(DEFAULT_LOCALE);
		assertThat(catalog.getDefaultLocale()).isEqualTo(DEFAULT_LOCALE);
	}

	/**
	 * Test that removing the default locale from the collection
	 * of supported locales throws a DefaultValueRemovalForbiddenException.
	 */
	@Test
	public void testRemoveDefaultLocaleFromSupportedLocalesForbidden() {
		CatalogImpl catalog = getCatalog();
		catalog.setMaster(true);
		catalog.setDefaultLocale(Locale.US);
		Collection<Locale> supportedLocales = new ArrayList<>();
		supportedLocales.add(Locale.US);
		supportedLocales.add(Locale.CANADA);
		assertThat(catalog.getSupportedLocales())
			.as("Setting the default locale should add that locale to supported locales")
			.contains(Locale.US);
		assertThat(catalog.getDefaultLocale()).isSameAs(Locale.US);
		Collection<Locale> localesWithoutDefault = new ArrayList<>();
		localesWithoutDefault.add(Locale.CANADA);

		assertThatThrownBy(() -> catalog.setSupportedLocales(getMultipleLocalesWithoutDefault()))
			.as("Removal of default locale from supported locales is forbidden")
			.isInstanceOf(DefaultValueRemovalForbiddenException.class);
	}

	/**
	 * Test that you can set the collection of supported locales without
	 * first setting a default locale.
	 */
	@Test
	public void testSettingSupportedLocalesWithoutDefaultLocale() throws DefaultValueRemovalForbiddenException {
		CatalogImpl catalog = getCatalog();
		catalog.setMaster(true);
		catalog.setSupportedLocales(this.getMultipleLocalesWithoutDefault());
		assertThat(catalog.getSupportedLocales())
			.as("The collection of supported locales should remain unchanged")
			.isEqualTo(getMultipleLocalesWithoutDefault());
	}

	private CatalogImpl getCatalog() {
		final CatalogImpl catalog = new StubbedCatalogImpl();
		catalog.initialize();
		catalog.setCode(new RandomGuidImpl().toString());
		return catalog;
	}
	
	/**
	 * Test that you can't set the default locale to null.
	 */
	@Test
	public void testSetDefaultLocaleNullForbidden() {
		CatalogImpl catalog = getCatalog();
		assertThatThrownBy(() -> catalog.setDefaultLocale(null))
			.as("Should not be able to set default locale to null")
			.isInstanceOf(IllegalArgumentException.class);
	}

	/**
	 * Test that getting the supported locales results in an unmodifiable collection.
	 */
	@Test
	public void testGetSupportedLocalesIsUnmodifiable() {
		CatalogImpl catalog = getCatalog();
		catalog.setMaster(true);
		assertThatThrownBy(() -> catalog.getSupportedLocales().add(Locale.US))
			.as("Should not be allowed to modify the collection of returned locales")
			.isInstanceOf(UnsupportedOperationException.class);
	}

	/**
	 * Test that supported currencies, currencies in use,
	 * supported locales, and locales in use are never null but
	 * are instead empty if none are set.
	 */
	@Test
	public void testSetsNeverNull() {
		CatalogImpl catalog = getCatalog();
		catalog.setMaster(true);
		assertThat(catalog.getSupportedLocales()).isEmpty();
	}

	/**
	 * Test that adding a supported locale adds the locale to the list of supported locales.
	 */
	@Test
	public void testAddSupportedLocale() {
		Catalog catalog = getCatalog();
		catalog.setMaster(true);
		Locale locale = Locale.JAPANESE;
		catalog.addSupportedLocale(locale);
		assertThat(catalog.getSupportedLocales())
			.containsOnly(locale)
			.extracting(Locale::getDisplayName)
			.containsOnly(locale.getDisplayName());
	}

	/**
	 * Test that adding a supported locale adds nothing for a virtual catalog.
	 */
	@Test
	public void testAddSupportedLocaleVirtualCatalog() {
		CatalogImpl vcatalog = getCatalog();
		Locale locale = Locale.JAPANESE;
		assertThatThrownBy(() -> vcatalog.addSupportedLocale(locale))
			.as("Virtual catalog should not support this operation")
			.isInstanceOf(UnsupportedOperationException.class);
		assertThat(vcatalog.getSupportedLocalesInternal()).isEmpty();
	}

	/**
	 * Gets a collection of Locales that does not contain the
	 * US locale.
	 *
	 * @return collection of locales not containing the default locale
	 */
	private Set<Locale> getMultipleLocalesWithoutDefault() {
		Set<Locale> locales = new HashSet<>();
		locales.add(Locale.FRANCE);
		locales.add(Locale.GERMANY);
		return locales;
	}

	/**
	 * Test that getGuid() and setGuid() delegate to the getCode() and setCode() methods.
	 */
	@Test
	public void testGetSetGuid() {
		final String code = "MyCode";
		CatalogImpl catalog = getCatalog();
		catalog.setGuid(code);
		assertThat(catalog.getCode())
			.as("setGuid and getGuid should delegate to setCode/getCode")
			.isEqualTo(code);
	}

	/**
	 * Test specifically for equality between transient and persistent instances.
	 */
	@Test
	public void testHashcodeAndEquals() {
		// Two transient instances.
		CatalogImpl catalog1 = getCatalog();
		CatalogImpl catalog2 = getCatalog();

		// A persistent instance
		CatalogImpl catalog3 = getCatalog();
		final long uidPk5000 = 5000;
		catalog3.setUidPk(uidPk5000);

		// A duplicate persistent instance.
		CatalogImpl catalog4 = getCatalog();
		catalog4.setUidPk(catalog3.getUidPk());
		catalog4.setCode(catalog3.getCode());

		// A new unique persistent instance
		CatalogImpl catalog5 = getCatalog();
		final long uidPk9999 = 9999;
		catalog5.setUidPk(uidPk9999);

		new EqualsTester()
			.addEqualityGroup(catalog1)
			.addEqualityGroup(catalog2)
			.addEqualityGroup(catalog3, catalog4)
			.addEqualityGroup(catalog5)
			.testEquals();

	}

	/**
	 * Separate class to work around a Checkstyle defect.
	 *
	 * @see <a href="http://sourceforge.net/p/checkstyle/bugs/472/">the defect</a>
	 */
	private class StubbedCatalogImpl extends CatalogImpl {
		private static final long serialVersionUID = 911805475665750452L;

		@Override
		public ElasticPath getElasticPath() {
			return new StubbedElasticPathImpl();
		}

		final class StubbedElasticPathImpl extends ElasticPathImpl {
			@SuppressWarnings("unchecked")
			@Override
			public <T> T getBean(final String name) {
				if (name.equals(ContextIdNames.CATALOG_LOCALE)) {
					return (T) new CatalogLocaleImpl();
				}
				if (name.equals(ContextIdNames.RANDOM_GUID)) {
					return (T) new RandomGuidImpl();
				}
				return null;
			}
		}
	}
}
