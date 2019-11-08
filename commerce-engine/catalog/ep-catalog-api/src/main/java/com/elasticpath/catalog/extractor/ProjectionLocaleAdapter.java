/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */

package com.elasticpath.catalog.extractor;

import java.util.Locale;
import java.util.Map;

/**
 * Extract locale from specific domain object property.
 */
public interface ProjectionLocaleAdapter {

	/**
	 * @return map, that contains locale of specific property as key, and value for property as value.
	 */
	Map<Locale, String> getCatalogLocaleByValue();

	/**
	 * @return default locale.
	 */
	Locale getDefaultCatalogLocale();
}
