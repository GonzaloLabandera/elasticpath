/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */
package com.elasticpath.domain.modifier;

import com.elasticpath.persistence.api.Persistable;


/**
 * Filter for modifier group.
 */
public interface ModifierGroupFilter extends Persistable {


	/**
	 * Gets the reference guid.
	 * @return the reference guid.
	 */
	String getReferenceGuid();

	/**
	 * Sets the reference guid.
	 * @param referenceGuid the reference guid.
	 */
	void setReferenceGuid(String referenceGuid);

	/**
	 * Gets the modifier code.
	 * @return the modifier code.
	 */
	String getModifierCode();

	/**
	 * Sets the modifier code.
	 * @param modifierCode the modifier code.
	 */
	void setModifierCode(String modifierCode);

	/**
	 * Gets the type.
	 * @return the type.
	 */
	String getType();

	/**
	 * Sets the type.
	 * @param type the type.
	 */
	void setType(String type);
}
