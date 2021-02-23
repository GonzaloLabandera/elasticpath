/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.service.rules.impl;

import org.drools.core.SessionConfiguration;

/**
 * Factory for {@link AbstractRuleEngineImpl} class to handle session creation.
 */
public class RuleEngineSessionFactory {

	private static final SessionConfiguration SESSION_CONFIGURATION = SessionConfiguration.newInstance();

	/**
	 * Gets session configuration.
	 *
	 * @return sessionConfiguration.
	 */
	public SessionConfiguration getSessionConfiguration() {
		return SESSION_CONFIGURATION;
	}

}
