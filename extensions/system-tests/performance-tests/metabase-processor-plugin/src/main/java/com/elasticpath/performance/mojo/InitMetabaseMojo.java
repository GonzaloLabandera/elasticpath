/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.utils.DbQueryUtils.CUCUMBER_PERFORMANCE_TABLE;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.SHOW_TABLES_QUERY;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The mojo is used in "ci-release-performance-data-processing" and "compare-with-imported-metabase" workflows/profiles,
 * for creating a metabase table used for performance results.
 */
@Mojo(name = "create-metabase-table", threadSafe = true)
public class InitMetabaseMojo extends AbstractMetabaseMojo {

	@Parameter(required = true)
	private String sqlInitFilePath;

	public void setSqlInitFilePath(final String sqlInitFilePath) {
		this.sqlInitFilePath = sqlInitFilePath;
	}

	/**
	 * Read the init SQL script
	 * (stored under "extensions/system-tests/performance-tests/metabase/src/main/resources/sql" for "ci-release-performance-data-processing" profile
	 * or "extensions/system-tests/performance-tests/cucumber/src/test/resources/sql" for "compare-with-imported-metabase" profile)
	 * and execute it against PostgreSQL/H2 db.
	 *
	 * @throws MojoExecutionException the exception
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		getLog().info("Creating metabase [" + CUCUMBER_PERFORMANCE_TABLE + "] table for storing cucumber performance results...");
		boolean shouldValidateMetabaseTableExistence = false;

		File sqlInitFile = new File(sqlInitFilePath);
		try (Reader sqlFileReader = Files.newBufferedReader(sqlInitFile.toPath());
			 Connection connection = getConnection();
			 Statement sqlScriptStatement = connection.createStatement()) {

			if (tableIsMissing(connection)) {
				String sqlInitFileContent = IOUtils.toString(sqlFileReader);

				if (getLog().isDebugEnabled()) {
					getLog().debug("Metabase SQL init script\n\n" + sqlInitFileContent);
				}

				sqlScriptStatement.execute(sqlInitFileContent);

				getLog().info("Metabase creation SQL script successfully executed");

				shouldValidateMetabaseTableExistence = true;
			}
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}

		if (shouldValidateMetabaseTableExistence) {
			validateMetabaseExistence();
		}
	}

	private boolean tableIsMissing(final Connection connection) throws SQLException {
		try (Statement tableExistsStatement = connection.createStatement();
			 ResultSet tableExistsResultSet = tableExistsStatement.executeQuery(SHOW_TABLES_QUERY)) {

			if (tableExistsResultSet.next()) {
				getLog().info("Table [" + CUCUMBER_PERFORMANCE_TABLE + "] already exists. Skipping metabase initialization");
				return false;
			}
			return true;
		}
	}

	private void validateMetabaseExistence() throws MojoExecutionException {
		getLog().info("Validating the existence of the [" + CUCUMBER_PERFORMANCE_TABLE + "] table...");

		try (Connection connection = getConnection();
			 Statement tableExistsStatement = connection.createStatement();
			 ResultSet resultSet = tableExistsStatement.executeQuery(SHOW_TABLES_QUERY)) {

			if (resultSet.next()) {
				getLog().info("Metabase table [" + CUCUMBER_PERFORMANCE_TABLE + "] is found");
				return;
			}

			throw new MojoExecutionException("Metabase table [" + CUCUMBER_PERFORMANCE_TABLE
					+ "] could not be found in the database referred by the jdbc url: " + getJdbcUrl());
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}
}
