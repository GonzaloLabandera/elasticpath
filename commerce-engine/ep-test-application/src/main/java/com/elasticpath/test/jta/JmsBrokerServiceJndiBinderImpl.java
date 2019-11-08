/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.test.jta;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.Logger;

import com.elasticpath.test.persister.TestConfig;
import com.elasticpath.test.persister.TestConfigurationFactory;
import com.elasticpath.test.support.jndi.JndiContextManager;
import com.elasticpath.test.util.Utils;

/**
 * Binds an ActiveMQXAConnectionFactory to the default JNDI name for the EP JMS ConnectionFactory.
 */
public class JmsBrokerServiceJndiBinderImpl {

	/**
	 * The JNDI name at which to bind the connection factory.
	 */
	private static final String JNDI_NAME = "java:comp/env/jms/test-broker";

	/**
	 * Name of property for JMS broker data directory location.
	 */
	private static final String JMS_BROKER_DATA_DIRECTORY = "jms.broker.data.directory";

	private static final Logger LOGGER = Logger.getLogger(JmsBrokerServiceJndiBinderImpl.class);

	private final JndiContextManager jndiContextManager;
	private final TestConfig testConfig = new TestConfig(new TestConfigurationFactory.ClassPathResourceProvider());
	private BrokerService jmsBrokerService;

	/**
	 * Constructor.
	 *
	 * @param jndiContextManager the JNDI context manager
	 */
	public JmsBrokerServiceJndiBinderImpl(final JndiContextManager jndiContextManager) {
		this.jndiContextManager = jndiContextManager;
	}

	/**
	 * Binds the JmsBroker into JNDI.
	 *
	 * @param url JMS broker url.
	 */
	public void bindJmsBrokerService(final String url) {
		jndiContextManager.bind(JNDI_NAME, createJmsBrokerService(url));
	}

	/**
	 * Unbinds the JmsBroker from the JNDI context.
	 */
	public void unbindJmsBrokerService() throws Exception {
		LOGGER.debug("JMS broker service " + jmsBrokerService.getBrokerName() + " stopped...");

		if (jmsBrokerService.isStopped()) {
			jmsBrokerService.start(true);
		}

		jndiContextManager.unbind(JNDI_NAME);
	}

	private BrokerService createJmsBrokerService(final String url) {
		jmsBrokerService = new BrokerService();
		final String brokerName = Utils.uniqueCode("broker");
		jmsBrokerService.setBrokerName(brokerName);

		try {
			LOGGER.debug("JMS broker service " + brokerName + " on url " + url + " starting...");
			jmsBrokerService.addConnector(url);
			jmsBrokerService.setDataDirectory(testConfig.getProperty(JMS_BROKER_DATA_DIRECTORY));
			jmsBrokerService.start();
		} catch (Exception e) {
			LOGGER.error("JmsBrokerServiceJndiBinderImpl error", e);
		}

		return jmsBrokerService;
	}

}
