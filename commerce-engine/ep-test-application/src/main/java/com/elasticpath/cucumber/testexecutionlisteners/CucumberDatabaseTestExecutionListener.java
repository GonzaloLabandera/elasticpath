/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.cucumber.testexecutionlisteners;

import org.apache.log4j.Logger;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.elasticpath.test.integration.junit.DatabaseTestExecutionListenerHelper;
import com.elasticpath.test.support.jndi.JndiContextManager;

/**
 * Cucumber specific test execution listener to manage database used in test context.
 */
public class CucumberDatabaseTestExecutionListener extends AbstractTestExecutionListener {

	private static final Logger LOG = Logger.getLogger(CucumberDatabaseTestExecutionListener.class);

	private final JndiContextManager jndiContextManager;

	public CucumberDatabaseTestExecutionListener() {
		jndiContextManager = JndiContextManager.createJndiContextManager();
	}

    @Override
    public void prepareTestInstance(final TestContext testContext) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("performing database reset for test context [" + testContext + "].");
        }

        DatabaseTestExecutionListenerHelper.resetDatabase(testContext, jndiContextManager);
    }

}
