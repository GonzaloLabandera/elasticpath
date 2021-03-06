/*
 * Copyright (c) Elastic Path Software Inc., 2006
 */
package com.elasticpath.persistence.support.impl;

import static org.junit.Assert.assertTrue;

import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.attribute.Attribute;
import com.elasticpath.domain.attribute.AttributeType;
import com.elasticpath.domain.attribute.AttributeUsage;
import com.elasticpath.domain.attribute.impl.AttributeImpl;
import com.elasticpath.domain.attribute.impl.AttributeUsageImpl;
import com.elasticpath.test.BeanFactoryExpectationsFactory;

/** Test cases for DistinctAttributeValueCriterionImpl. */
public class DistinctAttributeValueCriterionImplTest {

	private static final long ATTRIBUTE_UID = 123456L;
	private DistinctAttributeValueCriterionImpl distinctAttributeValueCriterionImpl;
	@Rule
	public final JUnitRuleMockery context = new JUnitRuleMockery();
	private BeanFactoryExpectationsFactory expectationsFactory;
	private BeanFactory beanFactory;

	/**
	 * Prepares for tests.
	 *
	 * @throws Exception -- in case of any errors.
	 */
	@Before
	public void setUp() throws Exception {
		distinctAttributeValueCriterionImpl = new DistinctAttributeValueCriterionImpl();
		beanFactory = context.mock(BeanFactory.class);
		expectationsFactory = new BeanFactoryExpectationsFactory(context, beanFactory);
		expectationsFactory.allowingBeanFactoryGetPrototypeBean(ContextIdNames.ATTRIBUTE_USAGE, AttributeUsage.class, AttributeUsageImpl.class);
	}

	@After
	public void tearDown() {
		expectationsFactory.close();
	}

	/**
	 * Test method for 'com.elasticpath.persistence.support.impl.DistinctAttributeValueCriterionImpl.getDistinctAttributeValueCriterion(Attribute)'.
	 */
	@Test
	public void testGetDistinctAttributeValueCriterion() {
		Attribute attribute = new AttributeImpl();
		attribute.setAttributeType(AttributeType.INTEGER);
		attribute.setUidPk(ATTRIBUTE_UID);
		AttributeUsage attributeUsage = new AttributeUsageImpl();
		attributeUsage.setValue(AttributeUsage.PRODUCT);
		attribute.setAttributeUsage(attributeUsage);

		String result = distinctAttributeValueCriterionImpl.getDistinctAttributeValueCriterion(attribute);
		assertTrue(result.contains("select"));
		assertTrue(result.contains(String.valueOf(ATTRIBUTE_UID)));
		assertTrue(result.contains("integerValue"));
		assertTrue(result.contains("Product"));

	}

	/**
	 * Test method for 'com.elasticpath.persistence.support.impl.DistinctAttributeValueCriterionImpl.getDistinctAttributeValueCriterion(Attribute)'.
	 */
	@Test
	public void testGetDistinctAttributeValueCriterion2() {
		Attribute attribute = new AttributeImpl();
		attribute.setAttributeType(AttributeType.LONG_TEXT);
		attribute.setUidPk(ATTRIBUTE_UID);
		AttributeUsage attributeUsage = new AttributeUsageImpl();
		attributeUsage.setValue(AttributeUsage.CATEGORY);
		attribute.setAttributeUsage(attributeUsage);

		String result = distinctAttributeValueCriterionImpl.getDistinctAttributeValueCriterion(attribute);
		assertTrue(result.contains("select"));
		assertTrue(result.contains(String.valueOf(ATTRIBUTE_UID)));
		assertTrue(result.contains("longTextValue"));
		assertTrue(result.contains("Category"));

	}

	/**
	 * Test method for 'com.elasticpath.persistence.support.impl.DistinctAttributeValueCriterionImpl.getDistinctAttributeValueCriterion(Attribute)'.
	 */
	@Test
	public void testGetDistinctAttributeValueCriterion3() {
		Attribute attribute = new AttributeImpl();
		attribute.setAttributeType(AttributeType.DATE);
		attribute.setUidPk(ATTRIBUTE_UID);
		AttributeUsage attributeUsage = new AttributeUsageImpl();
		attributeUsage.setValue(AttributeUsage.SKU);
		attribute.setAttributeUsage(attributeUsage);

		String result = distinctAttributeValueCriterionImpl.getDistinctAttributeValueCriterion(attribute);
		assertTrue(result.contains("select"));
		assertTrue(result.contains(String.valueOf(ATTRIBUTE_UID)));
		assertTrue(result.contains("dateValue"));
		assertTrue(result.contains("ProductSku"));

	}

}
