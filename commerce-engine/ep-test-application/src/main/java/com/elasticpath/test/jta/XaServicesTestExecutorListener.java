/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.test.jta;

import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.elasticpath.test.support.jndi.JndiContextManager;

/**
 * Runs JMS broker and registers in JNDI an ActiveMQ XA connection factory, H2 XA DataSource before each test class.
 */
public class XaServicesTestExecutorListener extends AbstractTestExecutionListener {

	private final JmsBrokerServiceJndiBinderImpl jmsBrokerServiceJndiBinder;
	private final ActiveMQXAConnectionFactoryJndiBinderImpl activeMqConnectionFactoryJndiBinder;
	private final XaDataSourceJndiBinderImpl xaDataSourceJndiBinder;

	/**
	 * Constructor.
	 */
	public XaServicesTestExecutorListener() {
		final JndiContextManager jndiContextManager = JndiContextManager.createJndiContextManager();

		activeMqConnectionFactoryJndiBinder = new ActiveMQXAConnectionFactoryJndiBinderImpl(jndiContextManager);
		jmsBrokerServiceJndiBinder = new JmsBrokerServiceJndiBinderImpl(jndiContextManager);
		xaDataSourceJndiBinder = new XaDataSourceJndiBinderImpl(jndiContextManager);
	}

	@Override
	public void beforeTestClass(final TestContext testContext) throws Exception {
		if (isBrokerConfiguratorPresent(testContext)) {
			final String url = getBrokerUrl(testContext);

			bindJmsBroker(url);
			bindActiveMQXAConnectionFactory(url);
			bindXaDataSource();
		}
	}

	@Override
	public void afterTestClass(final TestContext testContext) throws Exception {
		if (isBrokerConfiguratorPresent(testContext)) {
			unbindJmsBrokerService();
			unbindActiveMQXAConnectionFactory();
			unbindXaDataSource();
		}
	}

	private boolean isBrokerConfiguratorPresent(final TestContext testContext) {
		return testContext.getTestClass().isAnnotationPresent(JmsBrokerConfigurator.class);
	}

	private String getBrokerUrl(final TestContext testContext) {
		return testContext.getTestClass().getAnnotation(JmsBrokerConfigurator.class).url();
	}

	private void bindJmsBroker(final String url) {
		jmsBrokerServiceJndiBinder.bindJmsBrokerService(url);
	}

	private void unbindJmsBrokerService() throws Exception {
		jmsBrokerServiceJndiBinder.unbindJmsBrokerService();
	}

	private void bindActiveMQXAConnectionFactory(final String url) {
		activeMqConnectionFactoryJndiBinder.bindActiveMQXAConnectionFactory(url);
	}

	private void unbindActiveMQXAConnectionFactory() {
		activeMqConnectionFactoryJndiBinder.unbindActiveMQXAConnectionFactory();
	}

	private void bindXaDataSource() {
		xaDataSourceJndiBinder.bindXaDataSource();
	}

	private void unbindXaDataSource() {
		xaDataSourceJndiBinder.unbindXaDataSource();
	}

}
