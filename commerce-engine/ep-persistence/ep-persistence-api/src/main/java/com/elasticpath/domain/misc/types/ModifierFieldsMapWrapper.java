/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *
 */
package com.elasticpath.domain.misc.types;

import java.util.Map;

/**
 * This extension is used for managing cart(item) and order(item) modifier fields.
 */
public class ModifierFieldsMapWrapper extends AbstractJPAManageableMapWrapper<String, String> {

	/**
	 * Default constructor.
	 */
	public ModifierFieldsMapWrapper() {
		//empty
	}

	/**
	 * Custom constructor.
	 *
	 * @param newMap the map to construct a new instance with.
	 */
	public ModifierFieldsMapWrapper(final Map<String, String> newMap) {
		super(newMap);
	}

	@Override
	public Object copy(final Object original) {
		Map<String, String> originalMap = ((ModifierFieldsMapWrapper) original).getMap();
		return new ModifierFieldsMapWrapper(originalMap);
	}
}
