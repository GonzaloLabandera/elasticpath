/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.domain.modifier;

import com.elasticpath.persistence.api.Persistable;

/**
 * Localized ModifierGroup.
 */
public interface ModifierGroupLdf extends Persistable {

	/**
	 * Returns the locale.
	 *
	 * @return the locale.
	 */
	String getLocale();

	/**
	 * Set the locale.
	 *
	 * @param locale the locale
	 */
	void setLocale(String locale);

	/**
	 * Returns the display name.
	 *
	 * @return the display name.
	 */
	String getDisplayName();

	/**
	 * Set the display name.
	 *
	 * @param displayName the display name.
	 */
	void setDisplayName(String displayName);

}
