/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.catalog.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.Utility;
import com.elasticpath.commons.util.impl.UtilityImpl;
import com.elasticpath.domain.attribute.AttributeGroupAttribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.impl.AttributeGroupAttributeImpl;
import com.elasticpath.domain.attribute.impl.AttributeGroupImpl;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.attribute.impl.AttributeValueGroupFactoryImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueFactoryTestImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueGroupFactoryTestImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueGroupTestImpl;
import com.elasticpath.domain.attribute.impl.ExtAttributeValueTestImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.catalog.LocaleDependantFields;
import com.elasticpath.domain.localization.LocaleFallbackPolicy;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;

/**
 * Test <code>CategoryImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.GodClass" })
@RunWith(MockitoJUnitRunner.class)
public class CategoryImplTest  {

	private static final int ORDERING_NUMBER_2 = 4;

	private static final int ORDERING_NUMBER_1 = 3;

	private static final String TEST_CATEGORY_GUID_1 = "test category 1";

	private static final String TEST_CATEGORY_GUID_2 = "test category 2";

	private static final String TEST_CATEGORY_GUID = "test category";

	private Category categoryImpl;

	private Category childImpl;

	private CategoryType categoryType;

	/**
	 * Master catalog code.
	 */
	protected static final String TEST_MASTER_CATALOG_CODE = "a master catalog code that no one would ever think of";

	private Catalog masterCatalog;

	@Mock
	private BeanFactory beanFactory;

	/**
	 * Prepare for tests.
	 */
	@Before
	public void setUp() {
		setupCategoryType();
		setupCategory();
		setupChildCategory();
	}

	/**
	 * Test class for the localeFallbackPolicy.
	 *
	 */
	private class PredefinedLocaleFallbackPolicy implements LocaleFallbackPolicy {
		@Override
		public void addLocale(final Locale locale) {
			// not needed
		}
		@Override
		public List<Locale> getLocales() {
			return ImmutableList.of(new Locale("en"), new Locale("fr"));
		}

		@Override
		public Locale getPrimaryLocale() {
			return new Locale("en");
		}
		@Override
		public void setPreferredLocales(final Locale... locales) {
			//not needed
		}
	}

	private void setupChildCategory() {
		this.childImpl = getCategory();
	}

	private void setupCategory() {
		this.categoryImpl = getCategory();
	}

	private void setupCategoryType() {
		this.categoryType = new CategoryTypeImpl();
		this.categoryType.setAttributeGroup(new AttributeGroupImpl());

		final AttributeGroupAttribute caAttr1 = new AttributeGroupAttributeImpl();
		final AttributeImpl attr1 = new AttributeImpl();
		attr1.setLocalizedProperties(mock(LocalizedProperties.class));
		attr1.setAttributeType(AttributeType.INTEGER);
		caAttr1.setAttribute(attr1);
		categoryType.getAttributeGroup().addAttributeGroupAttribute(caAttr1);

		final AttributeGroupAttribute caAttr2 = new AttributeGroupAttributeImpl();
		final AttributeImpl attr2 = new AttributeImpl();
		attr2.setLocalizedProperties(mock(LocalizedProperties.class));
		attr2.setAttributeType(AttributeType.SHORT_TEXT);
		caAttr2.setAttribute(attr2);
		categoryType.getAttributeGroup().addAttributeGroupAttribute(caAttr2);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getStartDate()'.
	 */
	@Test
	public void testGetStartDate() {
		assertThat(categoryImpl.getStartDate()).isNotNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setStartDate(Date)'.
	 */
	@Test
	public void testSetStartDate() {
		final Date date = new Date();
		categoryImpl.setStartDate(date);
		assertThat(categoryImpl.getStartDate()).isEqualTo(date);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getEndDate()'.
	 */
	@Test
	public void testGetEndDate() {
		assertThat(categoryImpl.getEndDate()).isNull();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setEndDate(Date)'.
	 */
	@Test
	public void testSetEndDate() {
		final Date date = new Date();
		categoryImpl.setEndDate(date);
		assertThat(categoryImpl.getEndDate()).isEqualTo(date);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getOrdering()'.
	 */
	@Test
	public void testGetOrdering() {
		assertThat(categoryImpl.getOrdering()).isEqualTo(0);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setOrdering(int)'.
	 */
	@Test
	public void testSetOrdering() {
		final int ordering = 999;
		categoryImpl.setOrdering(ordering);
		assertThat(categoryImpl.getOrdering()).isEqualTo(ordering);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.compareTo()'.
	 */
	@Test
	public void testCompareToAndEquals() {

		// New categories are different
		new EqualsTester()
			.addEqualityGroup(categoryImpl)
			.addEqualityGroup(childImpl)
			.testEquals();


		// compare by ordering number
		categoryImpl.setOrdering(ORDERING_NUMBER_1);
		childImpl.setOrdering(ORDERING_NUMBER_2);
		assertThat(categoryImpl).isLessThan(childImpl);
		new EqualsTester()
			.addEqualityGroup(categoryImpl)
			.addEqualityGroup(childImpl)
			.testEquals();

		// compare by guid
		categoryImpl.setOrdering(ORDERING_NUMBER_1);
		childImpl.setOrdering(ORDERING_NUMBER_1);
		categoryImpl.setGuid(TEST_CATEGORY_GUID_1);
		childImpl.setGuid(TEST_CATEGORY_GUID_2);
		assertThat(categoryImpl).isLessThan(childImpl);
		new EqualsTester()
			.addEqualityGroup(categoryImpl)
			.addEqualityGroup(childImpl)
			.testEquals();

		// compare the same one
		categoryImpl.setOrdering(ORDERING_NUMBER_1);
		childImpl.setOrdering(ORDERING_NUMBER_1);
		categoryImpl.setGuid(TEST_CATEGORY_GUID);
		childImpl.setGuid(TEST_CATEGORY_GUID);
		new EqualsTester()
			.addEqualityGroup(categoryImpl, childImpl)
			.testEquals();
	}

	/**
	 * Test for isAvailable().
	 */
	@Test
	public void testIsAvailable() {
		final long timeUnit = 20000;
		Date beforeNow = new Date();
		beforeNow.setTime(beforeNow.getTime() - timeUnit);

		Date muchBeforeNow = new Date();
		beforeNow.setTime(beforeNow.getTime() - timeUnit * 2);

		Date afterNow = new Date();
		afterNow.setTime(afterNow.getTime() + timeUnit);

		categoryImpl.setStartDate(beforeNow);
		assertThat(categoryImpl.isAvailable()).isTrue();

		categoryImpl.setStartDate(afterNow);
		assertThat(categoryImpl.isAvailable()).isFalse();

		categoryImpl.setStartDate(beforeNow);
		categoryImpl.setEndDate(afterNow);
		assertThat(categoryImpl.isAvailable()).isTrue();

		categoryImpl.setStartDate(muchBeforeNow);
		categoryImpl.setEndDate(beforeNow);
		assertThat(categoryImpl.isAvailable()).isFalse();
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setDefaultValues()'.
	 */
	@Test
	public void testInitialize() {

		final Date oldDate = categoryImpl.getStartDate();
		final String oldGuid = categoryImpl.getGuid();

		// set default values again, no value should be changed.
		categoryImpl.initialize();
		assertThat(categoryImpl.getGuid()).isEqualTo(oldGuid);
		assertThat(categoryImpl.getStartDate()).isEqualTo(oldDate);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getFullAttributeValues()'.
	 */
	@Test
	public void testGetFullAttributeValues() {
		this.categoryImpl.setCategoryType(this.categoryType);
		assertThat(this.categoryImpl.getCategoryType()).isEqualTo(this.categoryType);
	}

	/**
	 * Test falling back to empty ldf.
	 */
	@Test
	public void testGetLocaleDependantFieldsFallbackToEmptyLDF() {
		final LocaleFallbackPolicy policy = new PredefinedLocaleFallbackPolicy();

		categoryImpl.setLocaleDependantFieldsMap(new HashMap<>());
		LocaleDependantFields result;

		result = categoryImpl.getLocaleDependantFields(policy);
		assertThat(result.getLocale())
			.as("If no LDF has been set, should get an empty LDF in the primary locale")
			.isEqualTo(new Locale("en"));
	}

	/**
	 * Test matching correct locales in policy.
	 */
	@Test
	public void testMatchCorrectLocaleInLocaleFallbackPolicy() {
		final LocaleFallbackPolicy policy = new PredefinedLocaleFallbackPolicy();
		final Map<Locale, LocaleDependantFields> ldfMap = new HashMap<>();

		categoryImpl.setLocaleDependantFieldsMap(ldfMap);
		LocaleDependantFields frLdf = mock(LocaleDependantFields.class, "fr");
		LocaleDependantFields enLdf = mock(LocaleDependantFields.class, "en");

		//put only the secondary ldf into the map
		ldfMap.put(new Locale("fr"), frLdf);

		LocaleDependantFields result = categoryImpl.getLocaleDependantFields(policy);
		assertThat(result)
			.as("LDF should match second locale in policy")
			.isEqualTo(frLdf);

		//put the primary ldf into the map
		ldfMap.put(new Locale("en"), enLdf);

		result = categoryImpl.getLocaleDependantFields(policy);
		assertThat(result)
			.as("LDF should match first locale in policy")
			.isEqualTo(enLdf);

	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getLastModifiedDate()'.
	 */
	@Test
	public void testGetLastModifiedDate() {
		assertThat(categoryImpl.getLastModifiedDate()).isNull();
	}

	/**
	 * Test method for {@link CategoryImpl#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		final CategoryImpl first = new CategoryImpl();
		final CategoryImpl second = new CategoryImpl();
		final CategoryImpl third = new CategoryImpl();
		final String guid = "some code";
		new EqualsTester()
			.addEqualityGroup(first, second)
			.testEquals();

		first.setGuid(guid);
		second.setGuid(guid);
		new EqualsTester()
			.addEqualityGroup(first, second)
			.testEquals();

		third.setGuid(guid);
		new EqualsTester()
			.addEqualityGroup(first, second, third)
			.testEquals();
	}

	/**
	 * Test that extension classes can override the AttributeValueGroup and ProductAttributeValue implementation classes.
	 */
	@Test
	public void testThatExtensionClassesCanOverrideAttributeValueImplementations() {
		AttributeImpl attribute = new AttributeImpl();
		attribute.setLocalizedProperties(mock(LocalizedProperties.class));
		attribute.setAttributeType(AttributeType.SHORT_TEXT);
		attribute.setDisplayName("name", Locale.ENGLISH);
		attribute.setKey("name");

		ExtCategoryImpl category = new ExtCategoryImpl();
		category.getAttributeValueGroup().setStringAttributeValue(attribute, null, "beanie-weenie");

		assertThat(category.getAttributeValueGroup())
			.as("AttributeValueGroup implementation class should have been overridden")
			.isInstanceOf(ExtAttributeValueGroupTestImpl.class);
		assertThat(category.getAttributeValueGroup().getAttributeValue("name", null))
			.as("AttributeValueImpl implementation class should have been overridden")
			.isInstanceOf(ExtAttributeValueTestImpl.class);
	}

	/**
	 * Tests that sub-classes can override the LocaleDependantFields implementation class.
	 */
	@Test
	public void testExtensionClassesCanOverrideLocaleDependantFieldsFactory() {
		final CatalogLocaleFallbackPolicyFactory factory = new CatalogLocaleFallbackPolicyFactory();

		when(beanFactory.getBean(ContextIdNames.LOCALE_FALLBACK_POLICY_FACTORY)).thenReturn(factory);
		ExtCategoryImpl category = new ExtCategoryImpl();
		LocaleDependantFields ldf = category.getLocaleDependantFieldsWithoutFallBack(Locale.ENGLISH);

		assertThat(ldf)
			.as("Extension classes should be able to override LocaleDependantFields impl")
			.isInstanceOf(ExtCategoryLocaleDependantFieldsImpl.class);
	}

	/**
	 * Faux category domain extension class.
	 */
	private class ExtCategoryImpl extends CategoryImpl {
		private static final long serialVersionUID = 4347049078506080278L;

		/**
		 * Factory factory method override.
		 * @return the factory
		 */
		@Override
		protected AttributeValueGroupFactoryImpl getAttributeValueGroupFactory() {
			return new ExtAttributeValueGroupFactoryTestImpl(new ExtAttributeValueFactoryTestImpl());
		}

		/**
		 * Creates an appropriate LocaleDependantFields value object for the given locale.  Override this method
		 * if you need to change the implementation class in an extension project.
		 *
		 * @param locale the Locale this LDF applies to
		 * @return a LocaleDependentFields object
		 */
		@Override
		protected LocaleDependantFields createLocaleDependantFields(final Locale locale) {
			final LocaleDependantFields ldf = new ExtCategoryLocaleDependantFieldsImpl();
			ldf.setLocale(locale);

			return ldf;
		}

		@Override
		public Utility getUtility() {
			return new UtilityImpl();
		}

		@Override
		protected <T> T getBean(final String beanName) {
			return beanFactory.getBean(beanName);
		}
	}

	/**
	 * Faux categoryLDF extension class.
	 */
	private static class ExtCategoryLocaleDependantFieldsImpl extends CategoryLocaleDependantFieldsImpl {
		private static final long serialVersionUID = -2035724548443157583L;
	}

	/**
	 * Returns a new <code>Category</code> instance.
	 *
	 * @return a new <code>Category</code> instance.
	 */
	private Category getCategory() {
		final Category category = new CategoryImpl();
		category.initialize();
		category.setCode(new RandomGuidImpl().toString());
		category.setCatalog(getCatalog());

		return category;
	}

	/**
	 * Gets the master catalog singleton.
	 *
	 * @return the master catalog singleton
	 */
	private Catalog getCatalog() {
		if (masterCatalog == null) {
			masterCatalog = new CatalogImpl();
			masterCatalog.setMaster(true);
			masterCatalog.setCode(TEST_MASTER_CATALOG_CODE);
		}
		return masterCatalog;
	}

}
