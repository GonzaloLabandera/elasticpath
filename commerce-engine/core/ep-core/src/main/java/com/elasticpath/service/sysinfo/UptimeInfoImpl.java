/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.sysinfo;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.pf4j.Extension;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;

/**
 * System metric extension for returning service uptime.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = 1100)
public class UptimeInfoImpl extends XPFExtensionPointImpl implements SystemInformation {

	private static final int MICROSECONDS_PER_SECOND = 1000;
	private static final int SECONDS_PER_MINUTE = 60;

	@Override
	public String getName() {
		return "Uptime (Minutes)";
	}

	@Override
	public String getSimpleValue() {
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		return String.valueOf(runtimeMXBean.getUptime() / MICROSECONDS_PER_SECOND / SECONDS_PER_MINUTE);
	}
}
