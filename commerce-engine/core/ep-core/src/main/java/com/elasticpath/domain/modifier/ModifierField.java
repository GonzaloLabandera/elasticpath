/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */
package com.elasticpath.domain.modifier;

import java.util.Set;

import com.elasticpath.persistence.api.Entity;

/**
 * Fields for Modifier.
 */
public interface ModifierField extends Entity, Comparable<ModifierField> {

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
	 * Get is required.
	 *
	 * @return the required
	 */
	boolean isRequired();

	/**
	 * Set the is required.
	 *
	 * @param required the required
	 */
	void setRequired(boolean required);

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
	 * Get the max size.
	 *
	 * @return the max size
	 */
	Integer getMaxSize();

	/**
	 * Set the max size.
	 *
	 * @param maxSize the max size
	 */
	void setMaxSize(Integer maxSize);

	/**
	 * Get the field type.
	 *
	 * @return the field type
	 */
	ModifierType getFieldType();

	/**
	 * Set the field type.
	 *
	 * @param fieldType the field type
	 */
	void setFieldType(ModifierType fieldType);

	/**
	 * Get an unmodifiable the cart item modifier localized fields.
	 * <p/>
	 * Use the addModifierFieldLdf, removeModifierFieldLdf methods to manage the collection.
	 *
	 * @return the cart item modifier localized fields
	 */
	Set<ModifierFieldLdf> getModifierFieldsLdf();

	/**
	 * Get an unmodifiable the cart item modifier options.
	 * <p/>
	 * Use the addModifierFieldOption, removeModifierFieldOption methods to manage the collection.
	 *
	 * @return the cart item modifier options
	 */
	Set<ModifierFieldOption> getModifierFieldOptions();

	/**
	 * Add a modifierFieldLdf and enforce some business rules.
	 *
	 * @param modifierFieldLdf the cart item modifier field LDF
	 */
	void addModifierFieldLdf(ModifierFieldLdf modifierFieldLdf);

	/**
	 * Remove a modifierFieldLdf.
	 *
	 * @param modifierFieldLdf the cart item modifier field LDF
	 */
	void removeModifierFieldLdf(ModifierFieldLdf modifierFieldLdf);

	/**
	 * Add a modifierFieldOption and enforce some business rules.
	 *
	 * @param modifierFieldOption the cart item modifier field option
	 */
	void addModifierFieldOption(ModifierFieldOption modifierFieldOption);

	/**
	 * Remove a modifierFieldOption.
	 *
	 * @param modifierFieldOption the cart item modifier field option
	 */
	void removeModifierFieldOption(ModifierFieldOption modifierFieldOption);

	/**
	 * Gets ModifierFieldLdf by language.
	 *
	 * @param language the locale
	 * @return the ModifierFieldLdf
	 */
	ModifierFieldLdf findModifierFieldLdfByLocale(String language);

	/**
	 * Gets ModifierFieldOption by value.
	 *
	 * @param value the value
	 * @return the ModifierFieldOption
	 */
	ModifierFieldOption findModifierFieldOptionByValue(String value);
}

