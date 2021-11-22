/**
 * Copyright (c) Elastic Path Software Inc., 2011
 */
package com.elasticpath.commons.util.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to provide the version number, application title and vendor and log them at the INFO level at startup.
 */
public class VersionService {
	private static final String UNDEFINED = "undefined";
	private static final Logger LOG = LoggerFactory.getLogger(VersionService.class);
	private String applicationVersion = UNDEFINED;
	private String applicationName = UNDEFINED;
	private String applicationVendor = UNDEFINED;


	/**
	 * Configured in spring to be called after all the properties are set.
	 */
	public void init() {
		if (!applicationVersion.equals(UNDEFINED)) {
			LOG.info("Commerce Version: {} {} by {}",
					applicationName, applicationVersion, applicationVendor);
		}
	}

	public String getApplicationVersion() {
		return applicationVersion;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getApplicationVendor() {
		return applicationVendor;
	}

	public void setApplicationName(final String applicationName) {
		this.applicationName = applicationName;
	}

	public void setApplicationVersion(final String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}

	public void setApplicationVendor(final String applicationVendor) {
		this.applicationVendor = applicationVendor;
	}
}