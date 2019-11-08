/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.jta.atomikos;

import javax.jms.Connection;
import javax.jms.JMSException;

import com.atomikos.jms.AtomikosConnectionFactoryBean;

/**
 * Interceptor of AtomikosConnectionFactoryBean refreshes connection pool in cause of JMSException.
 */
public class AtomikosConnectionFactoryInterceptor extends AtomikosConnectionFactoryBean {

	private static final long serialVersionUID = 5000000001L;

	@Override
	public Connection createConnection() throws JMSException {
		Connection connection = super.createConnection();

		try {
			connection.start();
		} catch (JMSException e) {
			refreshPool();

			connection = super.createConnection();

			connection.start();
		}

		return connection;
	}

}
