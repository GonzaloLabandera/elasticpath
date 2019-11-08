/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.domain.modifier;

import java.util.Set;

import com.elasticpath.persistence.api.Entity;

/**
 * Groups Modifiers.
 */
public interface ModifierGroup extends Entity {

	/**
	 * Get the code.
	 *
	 * @return the code
	 */
	String getCode();

	/**
	 * Set the code.
	 *
	 * @param code the code
	 */
	void setCode(String code);

	/**
	 * Get an unmodifiable set of ModifierGroupLdf.
	 * <p/>
	 * You can use addModifierGroupLdf, removeModifierGroupLdf below to manage the collection.
	 *
	 * @return an unmodifiable set of ModifierGroupLdf
	 */
	Set<ModifierGroupLdf> getModifierGroupLdf();

	/**
	 * Get an unmodifiable set of ModifierField.
	 * <p/>
	 * You can use addModifierField, removeModifierField below to manage the collection.
	 *
	 * @return an unmodifiable set of ModifierGroupLdf
	 */
	Set<ModifierField> getModifierFields();

	/**
	 * Add a modifierField and enforce some business rules.
	 *
	 * @param modifierField the cart item modifier field to add
	 */
	void addModifierField(ModifierField modifierField);

	/**
	 * Remove a modifierField.
	 *
	 * @param modifierField the cart item modifier field to add
	 */
	void removeModifierField(ModifierField modifierField);

	/**
	 * Add a modifierGroupLdf and enforce some business rules.
	 *
	 * @param modifierGroupLdf the cart item modifier group ldf to add
	 */
	void addModifierGroupLdf(ModifierGroupLdf modifierGroupLdf);

	/**
	 * Remove a modifierGroupLdf.
	 *
	 * @param modifierGroupLdf the cart item modifier group ldf to add
	 */
	void removeModifierGroupLdf(ModifierGroupLdf modifierGroupLdf);

	/**
	 * Remove all cart item modifier groups LDF.
	 */
	void removeAllModifierGroupLdf();

	/**
	 * Remove all cart item modifier fields.
	 */
	void removeAllModifierFields();

	/**
	 * Gets the ModifierGroupLdf by locale.
	 *
	 * @param language the language
	 * @return the ModifierGroupLdf
	 */
	ModifierGroupLdf getModifierGroupLdfByLocale(String language);

	/**
	 * Gets the ModifierField by code.
	 *
	 * @param code the code
	 * @return the ModifierField
	 */
	ModifierField getModifierFieldByCode(String code);



}
