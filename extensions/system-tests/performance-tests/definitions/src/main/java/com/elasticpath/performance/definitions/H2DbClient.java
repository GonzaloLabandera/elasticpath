/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.definitions;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.elasticpath.cortex.dce.PropertyManager;

/**
 * H2 DB client for resetting the H2 db.
 */
public final class H2DbClient {

	private static final PropertyManager PROPERTY_MANAGER = PropertyManager.getInstance();
	private static final String DB_CONNECTION_URL;
	private static final String DB_USERNAME;
	private static final String DB_PASSWORD;

	private static final String DB_CONNECTION_WITH_INIT_FROM_SNAPSHOT_URL;

	static {
		try {
			Class.forName(PROPERTY_MANAGER.getProperty("db.connection.driver.class"));

			DB_CONNECTION_URL = PROPERTY_MANAGER.getProperty("db.connection.url");
			DB_USERNAME = PROPERTY_MANAGER.getProperty("db.connection.username");
			DB_PASSWORD = PROPERTY_MANAGER.getProperty("db.connection.password");

			String snapshotFile = PROPERTY_MANAGER.getProperty("project.build.directory") + "/database/snapshot.sql";
			DB_CONNECTION_WITH_INIT_FROM_SNAPSHOT_URL = DB_CONNECTION_URL + ";INIT=runscript from '" + snapshotFile + "'";

		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	private H2DbClient() {
		//empty constructor
	}

	/**
	 * Reset H2 db using snapshot.
	 */
	public static void resetH2Database() {
		try {
			dropAllObjects();
			loadSnapshot();
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	private static Connection getConnection(final String connectionUrl) throws SQLException {
		return DriverManager.getConnection(connectionUrl, DB_USERNAME, DB_PASSWORD);
	}

	private static void dropAllObjects() throws SQLException {
		try (Connection connection = getConnection(DB_CONNECTION_URL);
			Statement statement = connection.createStatement()) {

			statement.execute("DROP ALL OBJECTS");
		}
	}

	@SuppressWarnings("checkstyle:emptyblock")
	private static void loadSnapshot() throws SQLException {
		try (Connection connection = getConnection(DB_CONNECTION_WITH_INIT_FROM_SNAPSHOT_URL)) {
			//just getting a connection will load the snapshot file
		}
	}
}
