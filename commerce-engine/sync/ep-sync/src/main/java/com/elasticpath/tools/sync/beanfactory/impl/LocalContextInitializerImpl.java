/**
 * Copyright (c) Elastic Path Software Inc., 2009
 */
package com.elasticpath.tools.sync.beanfactory.impl;

import java.util.Properties;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import com.elasticpath.tools.sync.beanfactory.ContextInitializer;
import com.elasticpath.tools.sync.beanfactory.local.DataSourceProperties;
import com.elasticpath.tools.sync.configuration.ConnectionConfiguration;

/**
 * Initializes a local context using Spring with the injected {@code pathToXmlFile} value.
 */
public class LocalContextInitializerImpl implements ContextInitializer {

	private static final String BEAN_NAME_DATA_SOURCE_PROPERTIES = "dataSourceProperties";
	private static final String BEAN_NAME_DATA_SOURCE = "injectedDataSource";

	private static final Logger LOG = Logger.getLogger(LocalContextInitializerImpl.class);
	
	private String pathToXmlFile;

	private String springProfile;


	/**
	 * Initialize a context with the requested connection configuration.
	 * and passes the {@code dataSourceProperties} bean to the underlying context.
	 * 
	 * @param config the configuration
	 * @return the bean factory for this context
	 */
	@Override
	public BeanFactory initializeContext(final ConnectionConfiguration config) {
		LOG.info("Initializing context using  connection configuration: " + config);
		ProxyBeanFactoryImpl parentBeanFactory = new ProxyBeanFactoryImpl();
		parentBeanFactory.addProxyBean(BEAN_NAME_DATA_SOURCE_PROPERTIES, createDataSourceProperties(config));

		ApplicationContext appContext = refreshApplicationContext(parentBeanFactory);
		LOG.info("Local context has been read and initalized");
		return appContext;
	}

	/**
	 * Initialize a context with the injected datasource.
	 * and passes the {@code dataSourceProperties} bean to the underlying context.
	 *
	 * @param dataSource the datasource
	 * @return the bean factory for this context
	 */
	@Override
	public BeanFactory initializeContext(final DataSource dataSource) {
		LOG.info("Initializing context using  datasource : " + dataSource);
		ProxyBeanFactoryImpl parentBeanFactory = new ProxyBeanFactoryImpl();
		parentBeanFactory.addProxyBean(BEAN_NAME_DATA_SOURCE, dataSource);

		ApplicationContext appContext = refreshApplicationContext(parentBeanFactory);
		LOG.info("Local context has been read and initalized");
		return appContext;
	}

	/**
	 * Creates the data source properties bean out of a {@link ConnectionConfiguration}.
	 *
	 * @param connectionConfiguration the connection configuration
	 * @return data source properties
	 */
	protected DataSourceProperties createDataSourceProperties(final ConnectionConfiguration connectionConfiguration) {
		Properties properties = new Properties();
		properties.setProperty(DataSourceProperties.DB_URL, connectionConfiguration.getUrl());
		properties.setProperty(DataSourceProperties.DB_USERNAME, connectionConfiguration.getLogin());
		properties.setProperty(DataSourceProperties.DB_PASSWORD, connectionConfiguration.getPwd());
		properties.setProperty(DataSourceProperties.DB_CLASS_NAME, connectionConfiguration.getDriver());
		
		return new DataSourceProperties(properties);
	}

	/**
	 * Gets the spring profile.
	 * @return the spring profile.
	 */
	public String getSpringProfile() {
		return springProfile;
	}

	/**
	 * Sets the spring profile.
	 * @param springProfile the spring profile.
	 */
	public void setSpringProfile(final String springProfile) {
		this.springProfile = springProfile;
	}

	/**
	 *
	 * @return the pathToXmlFile
	 */
	public String getPathToXmlFile() {
		return pathToXmlFile;
	}

	/**
	 *
	 * @param pathToXmlFile the pathToXmlFile to set
	 */
	public void setPathToXmlFile(final String pathToXmlFile) {
		this.pathToXmlFile = pathToXmlFile;
	}
	
	/**
	 * Close the spring context on destroy.
	 *
	 * @param beanFactory the bean factory
	 */
	@Override
	public void destroyContext(final BeanFactory beanFactory) {
		LOG.info("Closing local bean application factory");
		((ConfigurableApplicationContext) beanFactory).close();
	}

	private ApplicationContext refreshApplicationContext(final BeanFactory beanFactory) {
		GenericApplicationContext genericAppContext = new GenericApplicationContext(new DefaultListableBeanFactory(beanFactory));

		String profileToAdd = getSpringProfile();
		if (profileToAdd != null) {
			genericAppContext.getEnvironment().addActiveProfile(profileToAdd);
		}
		genericAppContext.refresh();
		// refresh of child context is implied by the type of constructor, but parent must be refreshed prior
		// to passing into constructor
		return new ClassPathXmlApplicationContext(new String[]{pathToXmlFile}, genericAppContext);
	}


}
