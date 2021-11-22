/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.utils.Constants.EIGHT;
import static com.elasticpath.performance.mojo.utils.Constants.ELEVEN;
import static com.elasticpath.performance.mojo.utils.Constants.FIVE;
import static com.elasticpath.performance.mojo.utils.Constants.FOUR;
import static com.elasticpath.performance.mojo.utils.Constants.NINE;
import static com.elasticpath.performance.mojo.utils.Constants.ONE;
import static com.elasticpath.performance.mojo.utils.Constants.SEVEN;
import static com.elasticpath.performance.mojo.utils.Constants.SIX;
import static com.elasticpath.performance.mojo.utils.Constants.TEN;
import static com.elasticpath.performance.mojo.utils.Constants.THREE;
import static com.elasticpath.performance.mojo.utils.Constants.TWO;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.INSERT_RELEASE_DATA_INTO_METABASE_STATEMENT;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.elasticpath.performance.mojo.beans.CSVLineBean;

/**
 * The mojo is used in the "ci-release-performance-data-processing" workflow/profile for saving performance test results to the metabase.
 */
@SuppressWarnings("PMD.AvoidStringBufferField")
@Mojo(name = "process-release-performance-data", threadSafe = true)
public class ProcessReleasePerformanceDataMojo extends AbstractMetabaseMojo {
	@Parameter(property = "epc.version", required = true, defaultValue = "master")
	private String epcVersion;

	@Parameter(property = "jenkins.job.id", required = true)
	private String jenkinsJobId;

	@Parameter(property = "commit.hash", required = true)
	private String commitHash;

	/**
	 * Read all CSV lines, create insert statements and execute a batch of statements against the remote metabase.
	 *
	 * @throws MojoExecutionException the exception.
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		getLog().info("Processing performance data for release pipeline..");

		saveResultsToMetabase();
	}

	private void saveResultsToMetabase() throws MojoExecutionException {
		getLog().info("Saving results to metabase using Jdbc URL: [" + getJdbcUrl() + "] ...");
		Map<String, String> testNameToTestIdApplication = new HashMap<>();

		if (getLog().isDebugEnabled()) {
			getLog().debug("Insert statement:\n" + INSERT_RELEASE_DATA_INTO_METABASE_STATEMENT);
		}

		Map<String, Long> testNameInsertRowId = new HashMap<>();

		ResultSet insertRowIdsRS = null;
		try (Connection connection = getConnection();
			 PreparedStatement insertStatement = connection.prepareStatement(INSERT_RELEASE_DATA_INTO_METABASE_STATEMENT.toString(),
					 RETURN_GENERATED_KEYS)) {

			prepareInsertStatement(insertStatement, testNameToTestIdApplication);

			//no special batching here - all insert statements are added to a single batch;
			//if the number of tests reaches hundreds/thousands, then the batching should be refactored
			insertStatement.executeBatch();

			insertRowIdsRS = insertStatement.getGeneratedKeys();

			while (insertRowIdsRS.next()) {
				testNameInsertRowId.put(insertRowIdsRS.getString(SIX), insertRowIdsRS.getLong(1));
			}

			getLog().info("The results are successfully saved to metabase");
		} catch (Exception ex) {
			getLog().error("The results couldn't be saved to metabase. The insert statement:\n "
					+ INSERT_RELEASE_DATA_INTO_METABASE_STATEMENT);
			throw new MojoExecutionException(ex.getMessage(), ex);
		} finally {
			if (insertRowIdsRS != null) {
				try {
					insertRowIdsRS.close();
				} catch (SQLException sqlException) {
					//noop
				}
			}
		}

		if (testNameInsertRowId.isEmpty()) {
			throw new MojoExecutionException("Inserted row IDs, required for renaming of JSON files, are missing. "
					+ "The batch insert didn't return required IDs");
		}

		renameJSONFiles(testNameInsertRowId, testNameToTestIdApplication);
	}

	private void prepareInsertStatement(final PreparedStatement insertStatement, final Map<String, String> testNameToTestIdApplication)
			throws MojoExecutionException {

		List<CSVLineBean> csvResultsLines = readCSVLines();

		for (CSVLineBean csvLineBean : csvResultsLines) {
			String testId = csvLineBean.getTestId();
			String application = csvLineBean.getApplication();
			String testName = csvLineBean.getTestName();

			testNameToTestIdApplication.put(testName, testId + "_" + application);
			//insert columns: jenkins_job_id, date_executed,epc_version,application,cuke_scenario,total_db_selects,total_db_inserts,total_db_updates
			try {
				insertStatement.setInt(ONE, Integer.parseInt(jenkinsJobId));
				insertStatement.setTimestamp(TWO, new Timestamp(System.currentTimeMillis()));
				insertStatement.setString(THREE, epcVersion);
				insertStatement.setString(FOUR, application);
				insertStatement.setString(FIVE, testName);
				insertStatement.setInt(SIX, csvLineBean.getTotalDbSelects());
				insertStatement.setInt(SEVEN, csvLineBean.getTotalDbInserts());
				insertStatement.setInt(EIGHT, csvLineBean.getTotalDbUpdates());
				insertStatement.setInt(NINE, csvLineBean.getTotalDbDeletes());
				insertStatement.setInt(TEN, csvLineBean.getTotalDbTime());
				insertStatement.setString(ELEVEN, commitHash);

				insertStatement.addBatch();
			} catch (Exception ex) {
				throw new MojoExecutionException(ex.getMessage(), ex);
			}
		}
	}

	//need to rename JSON files so they can be properly accessed from the PR
	private void renameJSONFiles(final Map<String, Long> testNameInsertRowId, final Map<String, String> testNameToTestIdApplication)
			throws MojoExecutionException {

		getLog().info("Renaming JSON files...");
		File dbStatsFolder = new File(getDbStatsFolderPath());

		try {
			for (Map.Entry<String, String> entry : testNameToTestIdApplication.entrySet()) {
				String testName = entry.getKey();
				//e.g. 5_db_statistics.json where 5 represents the test id in the CSV file
				String originalJsonFilePrefix = entry.getValue();
				String applicationName = originalJsonFilePrefix.split("_")[1];

				//renaming to e.g. 435_db_statistics.json where 435 corresponds to row id in the metabase table
				String newJsonFilePrefix = testNameInsertRowId.get(testName) + "_" + applicationName;

				File destinationFile = new File(dbStatsFolder, newJsonFilePrefix + "_db_statistics.json");

				if (!destinationFile.exists()) {
					FileUtils.moveFile(new File(dbStatsFolder, originalJsonFilePrefix + "_db_statistics.json"), destinationFile);
				}
			}

			getLog().info("Renaming JSON files is done");
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}
}
