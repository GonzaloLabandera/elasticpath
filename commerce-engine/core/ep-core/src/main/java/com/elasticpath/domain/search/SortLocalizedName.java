/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.domain.search;

import com.elasticpath.persistence.api.Persistable;

/**
 * Localized name of sort attribute.
 */
public interface SortLocalizedName extends Persistable {

	/**
	 * Get the locale code.
	 * @return locale code
	 */
	String getLocaleCode();

	/**
	 * Set the locale code.
	 * @param localeCode locale code
	 */
	void setLocaleCode(String localeCode);

	/**
	 * Get the name.
	 * @return name
	 */
	String getName();

	/**
	 * Sets the name.
	 * @param name name
	 */
	void setName(String name);
}
