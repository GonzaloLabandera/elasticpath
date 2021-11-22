/**
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.service.sysinfo;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.xpf.XPFExtensionPointEnum;
import com.elasticpath.xpf.annotations.XPFEmbedded;
import com.elasticpath.xpf.connectivity.annontation.XPFAssignment;
import com.elasticpath.xpf.connectivity.extension.XPFExtensionPointImpl;
import com.elasticpath.xpf.connectivity.extensionpoint.SystemInformation;

/**
 * System metric extension for returning the data source URL.
 */
@SuppressWarnings("checkstyle:magicnumber")
@Extension
@XPFEmbedded
@XPFAssignment(extensionPoint = XPFExtensionPointEnum.SYSTEM_INFORMATION, priority = 1100)
public class DataSourceInfoImpl extends XPFExtensionPointImpl implements SystemInformation {
	@Autowired(required = false)
	private DataSource dataSource;

	@Override
	public String getName() {
		return "Data Source URL";
	}

	@Override
	public String getSimpleValue() {
		if (dataSource != null) {
			try (Connection connection = dataSource.getConnection()) {
				return connection.getMetaData().getURL();
			} catch (SQLException throwables) {
				// Ignore
			}
		}
		return "UNKNOWN";
	}
}
