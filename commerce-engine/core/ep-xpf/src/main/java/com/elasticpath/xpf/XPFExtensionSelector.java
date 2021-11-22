/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf;

import java.util.List;

/**
 * Checks the selectors matches.
 */
public interface XPFExtensionSelector {

	/**
	 * Checks if two selectors are matches.
	 *
	 * @param other the selector
	 * @return is selectors are matches
	 */
	boolean matches(XPFExtensionSelector other);

	/**
	 * Checks if the selector matches any selector in the list.
	 *
	 * @param others the list of selectors
	 * @return is selectors are matches
	 */
	boolean matches(List<XPFExtensionSelector> others);

	/**
	 * Gets the selector value.
	 *
	 * @return the value of selector
	 */
	Object getValue();
}
