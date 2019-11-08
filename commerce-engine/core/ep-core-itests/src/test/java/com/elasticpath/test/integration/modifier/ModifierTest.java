/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.test.integration.modifier;

import org.junit.Test;

import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Test that the cart item modifier is persisted correctly.
 */
public class ModifierTest extends AbstractModifierTest {

	/**
	 * Test ExtProductType tree saving operation.
	 */
	@DirtiesDatabase
	@Test
	public void testSaveProductTypeWithCartItemModifierTree() {
		persistSimpleProductTypeWithModifierGroup();
	}

}
