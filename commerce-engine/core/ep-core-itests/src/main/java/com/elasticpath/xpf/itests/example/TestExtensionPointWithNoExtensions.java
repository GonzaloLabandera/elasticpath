/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.itests.example;

import org.pf4j.ExtensionPoint;

/**
 * An extension point with no extensions for use in integration tests.
 */
public interface TestExtensionPointWithNoExtensions extends ExtensionPoint {

	/**
	 * Do nothing.
	 */
	void doNothing();
}
