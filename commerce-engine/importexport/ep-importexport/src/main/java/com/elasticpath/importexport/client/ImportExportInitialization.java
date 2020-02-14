/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */
package com.elasticpath.importexport.client;

import java.util.Properties;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.persistence.api.PersistenceSessionFactory;

/**
 * Initialize the import/export client. This will be called by spring as a dependency
 * of the persistence engine to ensure the datasource is changed before anything
 * can use the persistence engine. Initialize {@link PooledConnectionFactory} that
 * needed to create pool of connections for ActiveMQ.
 */
public class ImportExportInitialization {

	private BeanFactory beanFactory;

	private Properties appProperties;

	/**
	 * Initialize the datasource, PooledConnectionFactory, ActiveMQConnectionFactory from the set properties.
	 */
	public void init() {
		final DataSourceProperties dataSourceProperties = DataSourceProperties.getInstance();
		dataSourceProperties.setProperties(appProperties);
		final PersistenceSessionFactory sessionFactory = getBeanFactory().getSingletonBean("sessionFactory", PersistenceSessionFactory.class);

		sessionFactory.setConnectionFactoryName(null);
		sessionFactory.setConnectionDriverName(JPADataSource.class.getName());

		final ActiveMQConnectionFactory jmsConnectionFactory = getBeanFactory().getSingletonBean(ContextIdNames.JMS_CONNECTION_FACTORY,
				ActiveMQConnectionFactory.class);
		jmsConnectionFactory.setBrokerURL(dataSourceProperties.getJmsUrl());

		final PooledConnectionFactory pooledConnectionFactory = getBeanFactory().getSingletonBean(ContextIdNames.POOLED_CONNECTION_FACTORY,
				PooledConnectionFactory.class);
		pooledConnectionFactory.setConnectionFactory(jmsConnectionFactory);
		pooledConnectionFactory.setMaxConnections(Integer.parseInt(dataSourceProperties.getJmsMaxConnections()));
		pooledConnectionFactory.setMaximumActiveSessionPerConnection(Integer.parseInt(dataSourceProperties.getJmsMaxConnections()));
		pooledConnectionFactory.setIdleTimeout(Integer.parseInt(dataSourceProperties.getJmsIdleTimeout()));

		pooledConnectionFactory.start();
	}

	/**
	 * Get the Bean Factory to use for gettings beans.
	 *
	 * @return the beanFactory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * Set the bean factory to be used for getting beans. This is normally injected by Spring.
	 *
	 * @param beanFactory the beanFactory to set
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Get the application properties collection.
	 *
	 * @return the appProperties
	 */
	public Properties getAppProperties() {
		return appProperties;
	}

	/**
	 * Set the application properties, this can be injected by Spring using the PropertiesFactoryBean class
	 * to read from a file.
	 *
	 * @param appProperties the appProperties to set
	 */
	public void setAppProperties(final Properties appProperties) {
		this.appProperties = appProperties;
	}


}
