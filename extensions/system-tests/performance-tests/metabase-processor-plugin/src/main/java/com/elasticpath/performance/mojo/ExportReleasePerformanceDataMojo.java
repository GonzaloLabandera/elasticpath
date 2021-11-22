/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.utils.Constants.APPLICATION_COL;
import static com.elasticpath.performance.mojo.utils.Constants.COMMIT_HASH_COL;
import static com.elasticpath.performance.mojo.utils.Constants.DATE_EXECUTED_COL;
import static com.elasticpath.performance.mojo.utils.Constants.EPC_VERSION_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TEST_NAME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_DELETES_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_INSERTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_SELECTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_TIME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_UPDATES_COL;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.CUCUMBER_PERFORMANCE_TABLE;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.H2_INSERT_STATEMENT_TEMPLATE;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.UNIQUE_EPC_VERSIONS_QUERY;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.getFormattedExportQuery;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The mojo is used in the "ci-release-performance-data-processing" workflow/profile, for exporting the latest performance results for all
 * supported releases.
 */
@SuppressWarnings("PMD.AvoidStringBufferField")
@Mojo(name = "export-release-performance-data", threadSafe = true)
public class ExportReleasePerformanceDataMojo extends AbstractMetabaseMojo {
	private static final StringBuilder EXPORT_RELEASE_DATA_BUILDER = new StringBuilder();

	/**
	 * Find all supported releases and the latest performance results.
	 * The results are saved into a file, published to EP Nexus and later on used by customer (or EP) developers.
	 *
	 * @throws MojoExecutionException the exception
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		getLog().info("Exporting the last set of results for all releases from [" + CUCUMBER_PERFORMANCE_TABLE + "] table..");

		try (Connection connection = getConnection();
			 Statement exportStatement = connection.createStatement()) {

			export(exportStatement);

		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}

		saveDump();
	}

	private void export(final Statement exportStatement) throws MojoExecutionException {
		String exportQuery = getExportQuery(exportStatement);

		if (getLog().isDebugEnabled()) {
			getLog().debug("Running db export statement:\n" + exportQuery);
		}

		try (ResultSet queryResult = exportStatement.executeQuery(exportQuery)) {
			while (queryResult.next()) {
				EXPORT_RELEASE_DATA_BUILDER
						.append(H2_INSERT_STATEMENT_TEMPLATE)
						.append('(')
						.append(join(",",
								wrapWithSinglQuote(String.valueOf(queryResult.getTimestamp(DATE_EXECUTED_COL))),
								wrapWithSinglQuote(queryResult.getString(EPC_VERSION_COL)),
								wrapWithSinglQuote(queryResult.getString(APPLICATION_COL)),
								wrapWithSinglQuote(queryResult.getString(TEST_NAME_COL)),
								queryResult.getString(TOTAL_DB_SELECTS_COL),
								queryResult.getString(TOTAL_DB_INSERTS_COL),
								queryResult.getString(TOTAL_DB_UPDATES_COL),
								queryResult.getString(TOTAL_DB_DELETES_COL),
								queryResult.getString(TOTAL_DB_TIME_COL),
								wrapWithSinglQuote(queryResult.getString(COMMIT_HASH_COL))))
						.append(");");
				if (!queryResult.isLast()) {
					EXPORT_RELEASE_DATA_BUILDER.append('\n');
				}
			}

			if (EXPORT_RELEASE_DATA_BUILDER.length() == 0) {
				throw new MojoExecutionException("No release performance results are found. The export failed");
			}

			getLog().info("The release performance results are successfully exported");
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}

	private String getExportQuery(final Statement exportStatement) throws MojoExecutionException {
		StringBuilder exportQueryBuilder = new StringBuilder();

		try (ResultSet queryResult = exportStatement.executeQuery(UNIQUE_EPC_VERSIONS_QUERY)) {

			while (queryResult.next()) {
				String epcVersion = queryResult.getString(1);

				String formattedQuery = getFormattedExportQuery(epcVersion);
				if (!queryResult.isLast()) {
					formattedQuery += "\nUNION\n"; //must UNIONize all queries
				}

				exportQueryBuilder.append(formattedQuery);
			}

			if (exportQueryBuilder.length() == 0) {
				throw new MojoExecutionException("No releases found. Verify that 'ci-release-performance-data-processing' is running properly");
			}
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}

		return exportQueryBuilder.toString();
	}

	private void saveDump() throws MojoExecutionException {
		File performanceMetabaseDumpFile = new File(getPerformanceMetabaseDumpFilePath());
		File dumpFileFolder = performanceMetabaseDumpFile.getParentFile();

		if (dumpFileFolder.exists() || performanceMetabaseDumpFile.getParentFile().mkdirs()) {

			getLog().info("Saving exported records to [" + performanceMetabaseDumpFile + "]");

			try (Writer writer = Files.newBufferedWriter(performanceMetabaseDumpFile.toPath(), UTF_8)) {
				writer.write(EXPORT_RELEASE_DATA_BUILDER.toString());
				writer.flush();
			} catch (Exception ex) {
				throw new MojoExecutionException(ex.getMessage(), ex);
			}

			getLog().info("The records have been successfully exported");
		} else {
			throw new MojoExecutionException("'" + performanceMetabaseDumpFile.getParentFile().getAbsolutePath() + "' folder couldn't be created");
		}
	}
}
