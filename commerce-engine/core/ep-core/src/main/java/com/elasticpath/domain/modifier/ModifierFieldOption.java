/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.domain.modifier;

import java.util.Set;

import com.elasticpath.persistence.api.Persistable;

/**
 * Options for ModifierField.
 */
public interface ModifierFieldOption extends Persistable, Comparable<ModifierFieldOption> {

	/**
	 * Get the value.
	 *
	 * @return the value
	 */
	String getValue();

	/**
	 * Set the value.
	 *
	 * @param value the value
	 */
	void setValue(String value);

	/**
	 * Get the ordering.
	 *
	 * @return the ordering
	 */
	int getOrdering();

	/**
	 * Set the ordering.
	 *
	 * @param ordering the ordering
	 */
	void setOrdering(int ordering);

	/**
	 * Get the cart item modifier field options localized fields.
	 *
	 * @return the cart item modifier field options localized fields
	 */
	Set<ModifierFieldOptionLdf> getModifierFieldOptionsLdf();

	/**
	 * Add a modifierFieldOptionLdf and enforce some business rules.
	 *
	 * @param modifierFieldOptionLdf the cart item modifier field option LDF
	 */
	void addModifierFieldOptionLdf(ModifierFieldOptionLdf modifierFieldOptionLdf);

	/**
	 * Remove a modifierFieldOptionLdf.
	 *
	 * @param modifierFieldOptionLdf the cart item modifier field option LDF
	 */
	void removeModifierFieldOptionLdf(ModifierFieldOptionLdf modifierFieldOptionLdf);

	/**
	 * Gets the ModifierFieldOptionLdf by locale.
	 *
	 * @param locale the locale
	 * @return the ModifierFieldOptionLdf
	 */
	ModifierFieldOptionLdf getModifierFieldOptionsLdfByLocale(String locale);
}
