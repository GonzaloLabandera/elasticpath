/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.shopper;

import java.util.Locale;

/**
 * Provides a {@link Locale}.
 */
public interface LocaleProvider  {
	
	/**
	 * Returns a {@link Locale}.
	 *
	 * @return a {@link Locale}.
	 */
	Locale getLocale();

}
