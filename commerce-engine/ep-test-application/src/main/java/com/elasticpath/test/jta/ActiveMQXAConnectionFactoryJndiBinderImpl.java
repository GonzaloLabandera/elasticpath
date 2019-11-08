/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.test.jta;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQXAConnectionFactory;

import com.elasticpath.test.support.jndi.JndiContextManager;

/**
 * Binds an ActiveMQXAConnectionFactory to the default JNDI name for the EP JMS ConnectionFactory.
 */
public class ActiveMQXAConnectionFactoryJndiBinderImpl {

	/**
	 * The JNDI name at which to bind the connection factory.
	 */
	private static final String JNDI_NAME = "java:comp/env/jms/JMSConnectionFactory-xa";

	private final JndiContextManager jndiContextManager;

	/**
	 * Constructor.
	 *
	 * @param jndiContextManager the JNDI context manager
	 */
	public ActiveMQXAConnectionFactoryJndiBinderImpl(final JndiContextManager jndiContextManager) {
		this.jndiContextManager = jndiContextManager;
	}

	/**
	 * Binds the ActiveMQXAConnectionFactory into JNDI.
	 *
	 * @param url JMS broker url.
	 */
	public void bindActiveMQXAConnectionFactory(final String url) {
		jndiContextManager.bind(JNDI_NAME, createConnectionFactory(url));
	}

	/**
	 * Unbinds the ActiveMQConnectionFactory from the JNDI context.
	 */
	public void unbindActiveMQXAConnectionFactory() {
		jndiContextManager.unbind(JNDI_NAME);
	}

	private ConnectionFactory createConnectionFactory(final String url) {
		return new ActiveMQXAConnectionFactory(url);
	}

}
