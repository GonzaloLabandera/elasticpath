/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.sysinfo;

import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;

/**
 * System metric extension for returning number of processors.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = 1100)
public class ProcessorsInfoImpl extends XPFExtensionPointImpl implements SystemInformation {
	@Override
	public String getName() {
		return "Processors";
	}

	@Override
	public String getSimpleValue() {
		return String.valueOf(Runtime.getRuntime().availableProcessors());
	}
}
