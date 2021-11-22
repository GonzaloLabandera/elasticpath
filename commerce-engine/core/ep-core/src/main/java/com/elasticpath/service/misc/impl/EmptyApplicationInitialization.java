/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.service.misc.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Empty application initialization. Does nothing other than log the fact it was called.
 */
public class EmptyApplicationInitialization {
	
	private static final Logger LOG = LogManager.getLogger(EmptyApplicationInitialization.class);
	
	/**
	 * Do nothing other than log.
	 */
	public void init() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Empty initializer called");
		}
	}

}
