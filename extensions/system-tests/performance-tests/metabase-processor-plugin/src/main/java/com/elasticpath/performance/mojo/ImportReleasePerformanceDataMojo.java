/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.utils.DbQueryUtils.CUCUMBER_PERFORMANCE_TABLE;
import static java.lang.String.format;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The mojo is used in the "compare-with-imported-metabase<" workflow/profile, for importing the latest OOTB performance results to a local H2
 * database.
 */
@SuppressWarnings("PMD.AvoidStringBufferField")
@Mojo(name = "import-release-performance-data", threadSafe = true)
public class ImportReleasePerformanceDataMojo extends AbstractMetabaseMojo {

	/**
	 * Read the file with exported release performance results and import the results to the local H2 db (LOCAL_PERFORMANCE_METABASE).
	 * @throws MojoExecutionException the exception
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		getLog().info("Importing OOTB performance data to  [" + getJdbcUrl() + "] and [" + CUCUMBER_PERFORMANCE_TABLE + "] table..");

		importReleasePerformanceRecords();
	}

	private void importReleasePerformanceRecords() throws MojoExecutionException {
		File releasePerformanceDataDump = new File(getPerformanceMetabaseDumpFilePath());

		try (Reader sqlDumpFileReader = Files.newBufferedReader(releasePerformanceDataDump.toPath());
			 Connection connection = getConnection();
			 Statement importReleaseDataStatement = connection.createStatement()) {

			String sqlDumpFileContent = IOUtils.toString(sqlDumpFileReader);
			if (getLog().isDebugEnabled()) {
				getLog().debug("Metabase SQL init script\n\n" + sqlDumpFileContent);
			}
			importReleaseDataStatement.execute(sqlDumpFileContent);

			getLog().info("Metabase creation SQL script successfully executed");
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}

		validateImport();
	}

	private void validateImport() throws MojoExecutionException {
		getLog().info("Validating imported release data...");

		String selectQuery = format("SELECT COUNT(*) FROM \"PUBLIC\".\"%s\"", CUCUMBER_PERFORMANCE_TABLE);

		try (Connection connection = getConnection();
			 Statement checkRecordCountStatement = connection.createStatement();
			 ResultSet resultSet = checkRecordCountStatement.executeQuery(selectQuery)) {

			if (resultSet.next()) {
				long numOfImportedRecords = resultSet.getLong(1);

				if (numOfImportedRecords > 0L) {
					getLog().info("OOTB release performance data are successfully imported to [" + CUCUMBER_PERFORMANCE_TABLE
							+ "] table. Found [" + numOfImportedRecords + "] records");
					return;
				}
			}

			throw new MojoExecutionException("The import of the OOTB release performance results has failed. The database ["
					+ getJdbcUrl() + "] is empty");
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}
}
