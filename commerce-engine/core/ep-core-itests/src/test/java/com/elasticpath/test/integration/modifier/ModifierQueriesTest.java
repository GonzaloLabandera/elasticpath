/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.test.integration.modifier;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.elasticpath.domain.modifier.ModifierField;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.domain.catalog.ProductType;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test that the cart item modifier is persisted correctly.
 */
public class ModifierQueriesTest extends AbstractModifierTest {

	/**
	 * Tests all find by GUID methods.
	 */
	@DirtiesDatabase
	@Test
	public void testFindGroupByCode() {
		final ProductType productType = persistSimpleProductTypeWithModifierGroup();

		//Retrieve ModifierGroup
		final Set<ModifierGroup> modifierGroups = productType.getModifierGroups();
		final ModifierGroup modifierGroup = modifierGroups.iterator().next();

		List<ModifierGroup> result = getPersistenceEngine()
				.retrieveByNamedQuery("MODIFIER_GROUP_BY_CODE", modifierGroup.getGuid());
		assertNotNull("The query should not return a null", result);
		assertThat(result, contains(modifierGroup));
	}

	/**
	 * Tests all find by GUID methods.
	 */
	@DirtiesDatabase
	@Test
	public void testFindFieldByCode() {
		final ProductType productType = persistSimpleProductTypeWithModifierGroup();

		//Retrieve ModifierGroup
		final Set<ModifierGroup> cartItemModifierGroups = productType.getModifierGroups();
		final ModifierGroup modifierGroup = cartItemModifierGroups.iterator().next();

		//Retrieve ModifierField
		final ModifierField modifierField = modifierGroup.getModifierFields().iterator().next();

		List<ModifierField> result = getPersistenceEngine().retrieveByNamedQuery("MODIFIER_FIELD_BY_CODE",
				modifierField.getGuid());
		assertNotNull("The query should not return a null", result);
		assertThat(result, contains(modifierField));
	}

}
