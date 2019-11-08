/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.service.search.impl;

import static com.elasticpath.service.search.impl.SortAttributeServiceImpl.SORT_ATTRIBUTE_FIND_BY_GUID;
import static com.elasticpath.service.search.impl.SortAttributeServiceImpl.SORT_ATTRIBUTE_FIND_BY_STORE_CODE;
import static com.elasticpath.service.search.impl.SortAttributeServiceImpl.SORT_ATTRIBUTE_FIND_BY_STORE_CODE_AND_LOCALE_CODE;
import static com.elasticpath.service.search.impl.SortAttributeServiceImpl.SORT_ATTRIBUTE_NAME_BY_GUID_AND_LOCALE_CODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collection;
import java.util.LinkedList;

import com.google.common.collect.ImmutableMap;
import org.jmock.Expectations;
import org.junit.Test;

import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.domain.search.SortAttributeGroup;
import com.elasticpath.domain.search.SortValue;
import com.elasticpath.domain.search.impl.SortAttributeImpl;
import com.elasticpath.test.jmock.AbstractEPServiceTestCase;

/**
 * Test for {@link SortAttributeServiceImpl}.
 */
public class SortAttributeServiceImplTest extends AbstractEPServiceTestCase {

	private static final String STORE_CODE = "storeCode";
	private static final String LOCALE_CODE = "en";
	private static final String BUSINESS_OBJECT_ID = "object";
	private static final String GUID = "guid";
	private static final String NAME = "name";

	private SortAttributeServiceImpl sortAttributeService;

	@Override
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		super.setUp();
		sortAttributeService = new SortAttributeServiceImpl();
		sortAttributeService.setPersistenceEngine(getPersistenceEngine());
	}

	@Test
	public void testSaveOrUpdate() {
		SortAttribute sortAttribute = generateTestSortAttribute();
		SortAttribute updatedSortAttribute = new SortAttributeImpl();

		Collection<SortAttribute> sortAttributes = new LinkedList<>();
		sortAttributes.add(updatedSortAttribute);

		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).retrieveByNamedQuery(with(SORT_ATTRIBUTE_FIND_BY_GUID),
						with(any(Object[].class)));
				will(returnValue(sortAttributes));

				oneOf(getMockPersistenceEngine()).saveOrUpdate(with(same(sortAttribute)));
				will(returnValue(updatedSortAttribute));
			}
		});

		SortAttribute returnedSortAttribute = sortAttributeService.saveOrUpdate(sortAttribute);
		assertSame(returnedSortAttribute, updatedSortAttribute);
	}

	@Test
	public void testRemove() {
		final SortAttribute sortAttribute = context.mock(SortAttribute.class);

		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).delete(with(same(sortAttribute)));
			}
		});

		sortAttributeService.remove(sortAttribute);
	}

	@Test
	public void testFindSortAttributesByStoreCode() {
		final Collection<SortAttribute> sortAttributes = new LinkedList<>();
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).retrieveByNamedQuery(with(SORT_ATTRIBUTE_FIND_BY_STORE_CODE), with(any(Object[].class)));
				will(returnValue(sortAttributes));
			}
		});

		assertSame(sortAttributes, sortAttributeService.findSortAttributesByStoreCode(STORE_CODE));
	}

	@Test
	public void testFindSortAttributeGuidsByStoreCodeAndLocaleCode() {
		final Collection<String> sortAttributeGuids = new LinkedList<>();
		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).retrieveByNamedQuery(with(SORT_ATTRIBUTE_FIND_BY_STORE_CODE_AND_LOCALE_CODE),
						with(any(Object[].class)));
				will(returnValue(sortAttributeGuids));
			}
		});

		assertSame(sortAttributeGuids, sortAttributeService.findSortAttributeGuidsByStoreCodeAndLocalCode(STORE_CODE, LOCALE_CODE));
	}

	@Test
	public void testFindSortAttributesByGuidAndLocaleCode() {
		final Collection<Object[]> result = new LinkedList<>();
		result.add(new Object[]{ BUSINESS_OBJECT_ID, Boolean.TRUE, SortAttributeGroup.FIELD_TYPE, NAME});

		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).retrieveByNamedQuery(with(SORT_ATTRIBUTE_NAME_BY_GUID_AND_LOCALE_CODE), with(any(Object[].class)));
				will(returnValue(result));
			}
		});

		SortValue sortValue = sortAttributeService.findSortValueByGuidAndLocaleCode(GUID, LOCALE_CODE);

		assertEquals(BUSINESS_OBJECT_ID, sortValue.getBusinessObjectId());
		assertEquals(SortAttributeGroup.FIELD_TYPE, sortValue.getAttributeType());
		assertEquals(NAME, sortValue.getName());
	}

	@Test
	public void testFindByGuid() {
		SortAttribute sortAttribute = generateTestSortAttribute();
		final Collection<SortAttribute> sortAttributes = new LinkedList<>();
		sortAttributes.add(sortAttribute);

		// expectations
		context.checking(new Expectations() {
			{
				oneOf(getMockPersistenceEngine()).retrieveByNamedQuery(with(SORT_ATTRIBUTE_FIND_BY_GUID), with(any(Object[].class)));
				will(returnValue(sortAttributes));
			}
		});

		assertSame(sortAttribute, sortAttributeService.findByGuid(STORE_CODE));
	}

	private SortAttribute generateTestSortAttribute() {
		SortAttribute sortAttribute = new SortAttributeImpl();
		sortAttribute.setGuid(GUID);
		sortAttribute.setBusinessObjectId(BUSINESS_OBJECT_ID);
		sortAttribute.setStoreCode(STORE_CODE);
		sortAttribute.setDescending(false);
		sortAttribute.setLocalizedNames(ImmutableMap.of());
		sortAttribute.setSortAttributeGroup(SortAttributeGroup.FIELD_TYPE);
		return sortAttribute;
	}
}
