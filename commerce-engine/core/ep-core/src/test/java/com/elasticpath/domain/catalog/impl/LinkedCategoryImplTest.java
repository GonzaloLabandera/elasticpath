/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.domain.catalog.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Locale;

import org.jmock.Expectations;
import org.junit.Test;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.exception.EpUnsupportedOperationException;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeGroupAttribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.impl.AttributeGroupAttributeImpl;
import com.elasticpath.domain.attribute.impl.AttributeGroupImpl;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.catalog.Catalog;
import com.elasticpath.domain.catalog.Category;
import com.elasticpath.domain.catalog.CategoryType;
import com.elasticpath.domain.localization.LocaleFallbackPolicy;
import com.elasticpath.domain.misc.LocalizedProperties;
import com.elasticpath.domain.misc.SupportedLocale;
import com.elasticpath.domain.misc.impl.LocalizedPropertiesImpl;
import com.elasticpath.domain.misc.impl.RandomGuidImpl;
import com.elasticpath.test.jmock.AbstractEPTestCase;

/**
 * Test <code>LinkedCategoryImpl</code>.
 */
@SuppressWarnings({ "PMD.TooManyMethods" })
public class LinkedCategoryImplTest extends AbstractEPTestCase {

	private Category masterCategoryImpl;

	private Category linkedCategoryImpl;

	private CategoryType categoryType;

	private CatalogImpl masterCatalog;


	/**
	 * Prepare for tests.
	 * 
	 * @throws Exception in case of error happens
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();

		stubGetPrototypeBean(ContextIdNames.CATALOG_LOCALE, SupportedLocale.class, CatalogLocaleImpl.class);
		stubGetPrototypeBean(ContextIdNames.LOCALIZED_PROPERTIES, LocalizedProperties.class, LocalizedPropertiesImpl.class);
		
		setupCategoryType();

		setupCategory();

		setupLinkedCategory();

	}

	private void setupCategory() {
		this.masterCategoryImpl = getCategory();
	}

	private void setupLinkedCategory() {
		linkedCategoryImpl = new LinkedCategoryImpl();
		linkedCategoryImpl.initialize();
		linkedCategoryImpl.setMasterCategory(masterCategoryImpl);
		linkedCategoryImpl.setCatalog(getCatalog());
	}

	private void setupCategoryType() {
		this.categoryType = new CategoryTypeImpl();
		this.categoryType.setAttributeGroup(new AttributeGroupImpl());

		final AttributeGroupAttribute caAttr1 = new AttributeGroupAttributeImpl();
		final Attribute attr1 = new AttributeImpl();
		attr1.setAttributeType(AttributeType.INTEGER);
		attr1.setCatalog(getCatalog());
		caAttr1.setAttribute(attr1);
		categoryType.getAttributeGroup().addAttributeGroupAttribute(caAttr1);

		final AttributeGroupAttribute caAttr2 = new AttributeGroupAttributeImpl();
		final Attribute attr2 = new AttributeImpl();
		attr2.setAttributeType(AttributeType.SHORT_TEXT);
		attr2.setCatalog(getCatalog());
		caAttr2.setAttribute(attr2);
		categoryType.getAttributeGroup().addAttributeGroupAttribute(caAttr2);
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getStartDate()'.
	 */
	@Test
	public void testGetStartDate() {
		assertNotNull(linkedCategoryImpl.getStartDate());
		assertSame(masterCategoryImpl.getStartDate(), linkedCategoryImpl.getStartDate());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setStartDate(Date)'.
	 */
	@Test
	public void testSetStartDate() {
		final Date date = new Date();

		try {
			linkedCategoryImpl.setStartDate(date);
		} catch (EpUnsupportedOperationException e) {
			// expecting a EpUnsupportedOperationException
			assertNotNull(e);
		}

		masterCategoryImpl.setStartDate(date);
		assertSame(date, linkedCategoryImpl.getStartDate());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getEndDate()'.
	 */
	@Test
	public void testGetEndDate() {
		assertNull(linkedCategoryImpl.getEndDate());
		assertSame(masterCategoryImpl.getEndDate(), linkedCategoryImpl.getEndDate());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setEndDate(Date)'.
	 */
	@Test
	public void testSetEndDate() {
		final Date date = new Date();

		try {
			linkedCategoryImpl.setEndDate(date);
		} catch (EpUnsupportedOperationException e) {
			// expecting a EpUnsupportedOperationException
			assertNotNull(e);
		}

		masterCategoryImpl.setEndDate(date);
		assertSame(date, linkedCategoryImpl.getEndDate());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getOrdering()'.
	 */
	@Test
	public void testGetOrdering() {
		assertEquals(0, linkedCategoryImpl.getOrdering());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setOrdering(int)'.
	 */
	@Test
	public void testSetOrdering() {
		final int ordering = 999;
		linkedCategoryImpl.setOrdering(ordering);
		assertEquals(ordering, linkedCategoryImpl.getOrdering());
	}

	/**
	 * Test for isAvailable(), builds on conditions in CategoryImplTest.
	 */
	@Test
	public void testIsAvaliable() {
		masterCategoryImpl.setHidden(true);
		linkedCategoryImpl.setIncluded(true);
		assertFalse(linkedCategoryImpl.isAvailable());

		masterCategoryImpl.setHidden(false);
		linkedCategoryImpl.setIncluded(true);
		assertTrue(linkedCategoryImpl.isAvailable());

		linkedCategoryImpl.setIncluded(false);
		assertFalse(linkedCategoryImpl.isAvailable());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.setDefaultValues()'.
	 */
	@Test
	public void testInitialize() {

		final Date oldDate = linkedCategoryImpl.getStartDate();
		final String oldGuid = linkedCategoryImpl.getGuid();

		// set default values again, no value should be changed.
		linkedCategoryImpl.initialize();
		assertEquals(oldGuid, linkedCategoryImpl.getGuid());
		assertSame(oldDate, linkedCategoryImpl.getStartDate());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getFullAttributeValues()'.
	 */
	@Test
	public void testGetFullAttributeValues() {
		this.masterCategoryImpl.setCategoryType(this.categoryType);
		assertSame(this.categoryType, this.masterCategoryImpl.getCategoryType());
		assertSame(this.masterCategoryImpl.getCategoryType(), this.linkedCategoryImpl.getCategoryType());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getLocaleDependantFields(LocaleFallbackPolicy)'.
	 */
	@Test
	public void testGetLocaleDependantFieldsUsingLocaleFallBackPolicy() {
		final LocaleFallbackPolicy policy = context.mock(LocaleFallbackPolicy.class);
		final Category masterCategory = context.mock(Category.class);
		linkedCategoryImpl.setMasterCategory(masterCategory);
		context.checking(new Expectations() {
			{
				oneOf(masterCategory).getLocaleDependantFields(policy);
				ignoring(policy);
			}
		});
		this.linkedCategoryImpl.getLocaleDependantFields(policy);
	}

	/**
	 * Test method for {@link LinkedCategoryImpl#setCode(String)}.
	 */
	@Test
	public void testGetSetCode() {
		assertNotNull(masterCategoryImpl.getCode());
		assertSame(masterCategoryImpl.getCode(), linkedCategoryImpl.getCode());
		assertEquals(linkedCategoryImpl.getCode(), linkedCategoryImpl.getCode());
	}

	/**
	 * Test method for 'com.elasticpath.domain.impl.CategoryImpl.getLastModifiedDate()'.
	 */
	@Test
	public void testGetLastModifiedDate() {
		assertNull(linkedCategoryImpl.getLastModifiedDate());
	}

	/**
	 * Test method for {@link LinkedCategoryImpl#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		final LinkedCategoryImpl first = new LinkedCategoryImpl();
		first.initialize();
		final LinkedCategoryImpl second = new LinkedCategoryImpl();
		second.initialize();
		final Category masterCategory = new CategoryImpl();
		masterCategory.initialize();

		assertFalse(first.equals(second));
		first.setGuid(second.getGuid());

		assertEquals("Guid is the only thing that matters", first, second);
	}

	/**
	 * Test method for {@link LinkedCategoryImpl#hashCode()}.
	 */
	@Test
	public void testHashCode() {
		final LinkedCategoryImpl first = new LinkedCategoryImpl();
		first.initialize();
		final LinkedCategoryImpl second = new LinkedCategoryImpl();
		second.initialize();

		assertEquals("Hash code should be same on different calls", first.hashCode(), first.hashCode());
		assertNotEquals("Different guid should hash to different value", first.hashCode(), second.hashCode());
		second.setGuid(first.getGuid());
		assertEquals("Same guid should hash to same value", first.hashCode(), second.hashCode());
	}
	
	/**
	 * @return a new <code>Category</code> instance.
	 */
	protected Category getCategory() {
		final Category category = new CategoryImpl();
		category.initialize();
		category.setCode(new RandomGuidImpl().toString());
		category.setCatalog(getCatalog());

		return category;
	}

	/**
	 * @return the master catalog singleton
	 */
	protected Catalog getCatalog() {
		if (masterCatalog == null) {
			masterCatalog = new CatalogImpl();
			masterCatalog.setMaster(true);
			masterCatalog.setCode("irrelevant catalog code");
			masterCatalog.setDefaultLocale(Locale.ENGLISH);
		}
		return masterCatalog;
	}
}
