/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo.utils;

import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * The data source wrapper class, used for handling metabase connections.
 */
public class MetabaseDataSource extends BasicDataSource {
	private static final int MAVENDB_MAX_ACTIVE = 5;
	private static final int MAVENDB_MAX_WAIT = 30000;

	private final String jdbcUrl;
	private final String dbUsername;
	private final String dbPassword;
	private final String dbDriverClassName;

	/**
	 * Configure JDBC settings.
	 *
	 * @param url JDBC-style URL
	 * @param username passed to the driver
	 * @param password passed to the driver
	 * @param driverClass The JDBC driver to use
	 */
	public MetabaseDataSource(final String url, final String username, final String password, final String driverClass) {
		this.jdbcUrl = url;
		this.dbUsername = username;
		this.dbPassword = password;
		this.dbDriverClassName = driverClass;
	}

	@Override
	public DataSource createDataSource() throws SQLException {
		setUrl(jdbcUrl);
		setUsername(dbUsername);
		setPassword(dbPassword);
		setDriverClassName(dbDriverClassName);

		setMaxActive(MAVENDB_MAX_ACTIVE);
		setMaxWait(MAVENDB_MAX_WAIT);

		setTestOnBorrow(true);
		return super.createDataSource();
	}
}
