/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.tools.sync.merge.configuration.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Tests for {@link ModifierGroupDaoAdapterImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupDaoAdapterImplTest {

	private static final String CART_ITEM_MODIFIER_GROUP_GUID = "testModifierGroupGuid";
	private static final String NON_EXIST_CART_ITEM_MODIFIER_GROUP_GUID = "testNonExistModifierGroupGuid";

	@Mock
	private ModifierGroup mockModifierGroup;

	@Mock
	private ModifierService mockModifierService;

	@Mock
	private BeanFactory mockBeanFactory;

	private ModifierGroupDaoAdapterImpl target;

	@Before
	public void setUp() {

		target = new ModifierGroupDaoAdapterImpl();
		target.setBeanFactory(mockBeanFactory);
		target.setModifierService(mockModifierService);

		given(mockModifierService.findModifierGroupByCode(CART_ITEM_MODIFIER_GROUP_GUID)).willReturn(mockModifierGroup);
		given(mockModifierService.findModifierGroupByCode(NON_EXIST_CART_ITEM_MODIFIER_GROUP_GUID)).willReturn(null);
		given(mockBeanFactory.getPrototypeBean(ContextIdNames.MODIFIER_GROUP, ModifierGroup.class)).willReturn(mockModifierGroup);

	}

	@Test
	public void testGet() {

		// when
		ModifierGroup resultWithExistingGuid = target.get(CART_ITEM_MODIFIER_GROUP_GUID);
		ModifierGroup resultWithNonExistingGuid = target.get(NON_EXIST_CART_ITEM_MODIFIER_GROUP_GUID);

		// verify
		assertEquals(mockModifierGroup, resultWithExistingGuid);
		assertNull(resultWithNonExistingGuid);

	}

	@Test
	public void testAdd() {

		// when
		target.add(mockModifierGroup);

		// verify
		verify(mockModifierService, times(1)).saveOrUpdate(mockModifierGroup);

	}

	@Test
	public void testUpdate() {

		// when
		target.update(mockModifierGroup);

		// verify
		verify(mockModifierService, times(1)).saveOrUpdate(mockModifierGroup);

	}

	@Test
	public void testRemove() {

		// when
		boolean resultWithExistingGuid = target.remove(CART_ITEM_MODIFIER_GROUP_GUID);
		boolean resultWithNonExistingGuid = target.remove(NON_EXIST_CART_ITEM_MODIFIER_GROUP_GUID);

		// verify
		assertTrue(resultWithExistingGuid);
		assertFalse(resultWithNonExistingGuid);
		verify(mockModifierService, times(1)).remove(mockModifierGroup);
	}

	@Test
	public void testCreateBean() {

		// when
		ModifierGroup result = target.createBean(null);

		// verify
		assertEquals(mockModifierGroup, result);
	}

}
