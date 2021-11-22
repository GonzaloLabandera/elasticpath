/*
 *
 *  * Copyright (c) Elastic Path Software Inc., 2021
 *
 */

package com.elasticpath.domain.misc.types;

/**
 * This interface indicates that implementing entity supports modifier fields.
 */
public interface Modifiable {

	/**
	 * Check if entity has modifier fields.
	 *
	 * @return true, if modifiers exist
	 */
	Boolean getHasModifiers();

	/**
	 * Get modifiers.
	 * @return the map wrapper with modifiers.
	 */
	ModifierFieldsMapWrapper getModifierFields();
}
