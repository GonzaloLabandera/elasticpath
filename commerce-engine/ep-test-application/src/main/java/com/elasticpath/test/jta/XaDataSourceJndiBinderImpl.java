/*
 * Copyright (c) Elastic Path Software Inc., 2018
 */
package com.elasticpath.test.jta;

import org.h2.jdbcx.JdbcDataSource;

import com.elasticpath.test.support.jndi.JndiContextManager;

/**
 * Binds a XaDataSource to the default JNDI name for the EP JMS ConnectionFactory.
 */
public class XaDataSourceJndiBinderImpl {

	/**
	 * The JNDI name at which to bind the connection factory.
	 */
	private static final String JNDI_NAME = "java:comp/env/jdbc/epjndi-xa";

	private final JndiContextManager jndiContextManager;

	/**
	 * Constructor.
	 *
	 * @param jndiContextManager the JNDI context manager
	 */
	public XaDataSourceJndiBinderImpl(final JndiContextManager jndiContextManager) {
		this.jndiContextManager = jndiContextManager;
	}

	/**
	 * Binds the Xa DataSource into JNDI.
	 */
	public void bindXaDataSource() {
		jndiContextManager.bind(JNDI_NAME, createXaDataSource());
	}

	/**
	 * Unbinds the Xa DataSource from the JNDI context.
	 */
	public void unbindXaDataSource() {
		jndiContextManager.unbind(JNDI_NAME);
	}

	private JdbcDataSource createXaDataSource() {
		final JdbcDataSource jdbcDataSource = new JdbcDataSource();

		jdbcDataSource.setUser("sa");
		jdbcDataSource.setPassword("sa");
		jdbcDataSource.setURL("jdbc:h2:mem:test_db");

		return jdbcDataSource;
	}

}
