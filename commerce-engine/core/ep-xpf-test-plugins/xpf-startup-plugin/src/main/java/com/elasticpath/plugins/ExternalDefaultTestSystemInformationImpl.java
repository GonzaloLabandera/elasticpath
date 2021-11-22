/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.plugins;

import org.pf4j.Extension;
import org.pf4j.PluginWrapper;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;
import com.elasticpath.xpf.connectivity.plugin.XPFExternalPlugin;

/**
 * External plugin for testing
 */
public class ExternalDefaultTestSystemInformationImpl extends XPFExternalPlugin {

	/**
	 * Constructor.
	 *
	 * @param wrapper a wrapper over plugin instance.
	 */
	public ExternalDefaultTestSystemInformationImpl(final PluginWrapper wrapper) {
		super(wrapper);
	}

	/**
	 * Test extension.
	 */
	@Extension
	@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = 1070)
	public static class TestSystemInformation extends XPFExtensionPointImpl implements SystemInformation {
		@Override
		public String getName() {
			return "Operating System";
		}

		@Override
		public String getSimpleValue() {
			return "External startup test plugin!";
		}

	}

}
