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
 * System metric extension for returning max memory limit.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = 1100)
public class MaxMemoryInfoImpl extends XPFExtensionPointImpl implements SystemInformation {
	private static final int BYTES_PER_KILOBYTE = 1024;
	private static final int KILOBYTES_PER_MEGABYTE = 1024;

	@Override
	public String getName() {
		return "Max Memory (Mb)";
	}

	@Override
	public String getSimpleValue() {
		return String.valueOf(Runtime.getRuntime().maxMemory() / BYTES_PER_KILOBYTE / KILOBYTES_PER_MEGABYTE);
	}
}
