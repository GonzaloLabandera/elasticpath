/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.itests.example.impl;

import org.pf4j.Extension;

import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.itests.example.TestExtensionPoint;

/**
 * Implementation of an Embedded Extension for use in integration tests.
 */
@Extension
@XPFEmbedded
public class TestEmbeddedExtension1 extends XPFExtensionPointImpl implements TestExtensionPoint {

	@Override
	public String getText() {
		return "Hello EP";
	}
}