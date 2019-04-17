/**
 * Copyright (c) Elastic Path Software Inc., 2009
 */
package com.elasticpath.tools.sync.beanfactory;

import javax.sql.DataSource;

import org.springframework.beans.factory.BeanFactory;

import com.elasticpath.tools.sync.configuration.ConnectionConfiguration;

/**
 * A context initializer for setting up a Spring context.
 */
public interface ContextInitializer {

	/**
	 * Initializes a context by providing a connection configuration.
	 * 
	 * @param config the connection configuration
	 * @return a bean factory representing the initialized application context
	 */
	BeanFactory initializeContext(ConnectionConfiguration config);
	/**
	 * Initializes a context by providing an injected datasource.
	 * @param dataSource the data source
	 * @return the bean factory for this context
	 */

	BeanFactory initializeContext(DataSource dataSource);

	/**
	 * Destroy context.
	 *
	 * @param beanFactory the bean factory
	 */
	void destroyContext(BeanFactory beanFactory);
}
