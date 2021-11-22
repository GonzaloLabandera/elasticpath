/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo;

import java.sql.Connection;
import java.sql.Statement;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Before running each test the H2 db has to be cleaned to ensure consistent measurement.
 * This mojo will create an H2 snapshot before starting the applications.
 * The snapshot will be used by the <code>com.elasticpath.performance.common.H2DbClient</code> for resetting the H2 db.
 */
@Mojo(name = "create-h2-snapshot", threadSafe = true)
public class CreateH2SnapshotMojo extends AbstractMetabaseMojo {
	@Parameter(required = true)
	private String dbSnapshotFolder;

	/**
	 * Create a snapshot of the existing H2 database. The snapshot will be used for recreating a db before starting each test.
	 *
	 * @throws MojoExecutionException the exception
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		getLog().info("Creating an H2 snapshot...");

		try (Connection connection = getConnection();
			 Statement exportStatement = connection.createStatement()) {

			exportStatement.execute("SCRIPT TO '" + dbSnapshotFolder + "/snapshot.sql'");

		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}
}
