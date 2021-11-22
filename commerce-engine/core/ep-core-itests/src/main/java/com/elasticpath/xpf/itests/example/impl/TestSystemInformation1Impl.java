/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.itests.example.impl;

import org.pf4j.Extension;

import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;

/**
 * Implementation of an Embedded Extension for use in integration tests.
 */
@Extension
@XPFEmbedded
public class TestSystemInformation1Impl extends XPFExtensionPointImpl implements SystemInformation {
	@Override
	public String getName() {
		return "TestSystemInformation1Impl";
	}

	@Override
	public String getSimpleValue() {
		return "TestSystemInformation1Impl";
	}
}
