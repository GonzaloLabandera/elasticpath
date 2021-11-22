/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.itests.example.impl;

import org.pf4j.Extension;

import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.itests.example.TestExtensionPoint;

/**
 * Implementation of an Extension for use in integration tests.
 * 
 * This implementation is an @Extension but NOT an @XPFEmbedded
 */
@Extension
public class TestExtension extends XPFExtensionPointImpl implements TestExtensionPoint {

	@Override
	public String getText() {
		return "Hello World";
	}

}