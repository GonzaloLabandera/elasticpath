/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.itests.example;

import org.pf4j.ExtensionPoint;

/**
 * An Extension Point for use in integration tests.
 */
public interface TestExtensionPoint extends ExtensionPoint {
	/**
	 * @return some test text
	 */
	String getText();
}
