/**
 * Copyright (c) Elastic Path Software Inc., 2015
 */
package com.elasticpath.test.integration.junit;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.test.context.TestContext;

import com.elasticpath.test.persister.DatasourceInitializerFactory;
import com.elasticpath.test.persister.TestConfig;
import com.elasticpath.test.persister.TestConfigurationFactory;
import com.elasticpath.test.persister.database.DataSourceInitializer;
import com.elasticpath.test.support.jndi.JndiContextManager;

/**
 * A helper class for test execution listeners that manage databases within the test context.
 */
public class DatabaseTestExecutionListenerHelper {
	private static final String JNDI_NAME = "java:comp/env/jdbc/epjndi";


    private DatabaseTestExecutionListenerHelper() {
        // prohibit instances of this class being created
    }

    /**
     * Re-initializes a new database for a TestContext.
     *
     * @param testContext the test context to re-initiliaze the database for
     */
    public static void resetDatabase(final TestContext testContext, final JndiContextManager jndiContextManager) {
        testContext.markApplicationContextDirty();

        final TestConfig testConfig = new TestConfig(new TestConfigurationFactory.ClassPathResourceProvider());

        final DataSourceInitializer initializer = new DatasourceInitializerFactory().getInstance(testConfig.getDatabaseProperties());
        initializer.dropAndCreateDatabase();

        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(initializer.getDriverClass().getName());
        dataSource.setUrl(initializer.getConnectionUrl());
        dataSource.setUsername(initializer.getUsername());
        dataSource.setPassword(initializer.getPassword());

		jndiContextManager.unbind(JNDI_NAME);
		jndiContextManager.bind(JNDI_NAME, dataSource);
    }
}
