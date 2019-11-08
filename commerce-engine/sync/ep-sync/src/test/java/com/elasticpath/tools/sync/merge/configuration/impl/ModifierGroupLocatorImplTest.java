/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.tools.sync.merge.configuration.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.modifier.ModifierService;

/**
 * Tests for {@link ModifierGroupLocatorImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModifierGroupLocatorImplTest {

	private static final String CART_ITEM_MODIFIER_GROUP_GUID = "testGroupGuid";
	private static final String NON_EXIST_CART_ITEM_MODIFIER_GROUP_GUID = "testNonExistGroupGuid";

	@Mock
	private ModifierGroup mockModifierGroup;

	@Mock
	private ModifierService mockModifierService;

	private ModifierGroupLocatorImpl target;

	@Before
	public void setUp() {

		target = new ModifierGroupLocatorImpl();
		target.setModifierService(mockModifierService);

		given(mockModifierService.findModifierGroupByCode(CART_ITEM_MODIFIER_GROUP_GUID))
				.willReturn(mockModifierGroup);
		given(mockModifierService.findModifierGroupByCode(NON_EXIST_CART_ITEM_MODIFIER_GROUP_GUID)).willReturn(null);

	}

	@Test
	public void testLocatePersistence() {

		// when
		Persistable result = target.locatePersistence(CART_ITEM_MODIFIER_GROUP_GUID, ModifierGroup.class);

		// verify
		assertEquals(mockModifierGroup, result);
	}

	@Test
	public void testLocatePersistenceWithNonExistGuid() {

		// when
		Persistable result = target.locatePersistence(NON_EXIST_CART_ITEM_MODIFIER_GROUP_GUID, ModifierGroup.class);

		// verify
		assertNull(result);
	}

	@Test
	public void testIsResponsibleFor() {

		// when
		boolean result1 = target.isResponsibleFor(ModifierGroup.class);
		boolean result2 = target.isResponsibleFor(Object.class);

		// verify
		assertTrue(result1);
		assertFalse(result2);

	}

}
