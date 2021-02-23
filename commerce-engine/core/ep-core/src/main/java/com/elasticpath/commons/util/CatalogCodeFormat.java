/**
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.commons.util;

import com.elasticpath.commons.enums.InvalidCatalogCodeMessage;

/**
 * Provides Catalog Code Format methods used throughout the application.
 */
public interface CatalogCodeFormat {

	/**
	 * @return a int value that defines the Max Lenght accepted in this Catalog Code Format
	 */
	int getMaxLength();


	/**
	 * @return <code>true</code> if the format is set to use spaces. Otherwise, <code>false</code>
	 */
	boolean isSpacesAllowed();

	/**
	 * @return a regular expression that defines the string accepted in this Catalog Code Format
	 */
	String getRegex();

	/**
	 * @return <code>invalidCatalogCodeMessage</code> which defines the error message
	 */
	InvalidCatalogCodeMessage getInvalidCatalogCodeMessage();

}
