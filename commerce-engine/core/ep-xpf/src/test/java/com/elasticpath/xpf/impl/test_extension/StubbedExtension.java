/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.xpf.impl.test_extension;

import java.util.Map;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

import com.elasticpath.xpf.connectivity.context.XPFExtensionInitializationContext;
import com.elasticpath.xpf.connectivity.entity.XPFPluginSetting;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;

/**
 * Test extension.
 */
@Extension
public class StubbedExtension extends XPFExtensionPointImpl implements ExtensionPoint {
	private Map<String, XPFPluginSetting> settings;

	@Override
	public void initialize(final XPFExtensionInitializationContext context) {
		this.settings = context.getSettings();
	}

	public Map<String, XPFPluginSetting> getSettings() {
		return settings;
	}
}
