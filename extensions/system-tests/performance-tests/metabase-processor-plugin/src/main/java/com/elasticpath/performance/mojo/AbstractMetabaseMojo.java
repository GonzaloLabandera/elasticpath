/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.utils.Constants.CSV_HEADER_ROW_INDEX;
import static com.elasticpath.performance.mojo.utils.Constants.EIGHT;
import static com.elasticpath.performance.mojo.utils.Constants.SINGLE_QUOTE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import com.elasticpath.performance.mojo.beans.CSVLineBean;
import com.elasticpath.performance.mojo.utils.MetabaseDataSource;

/**
 * The base class for all other mojos. Provides common settings and methods.
 */
public abstract class AbstractMetabaseMojo extends AbstractMojo {
	// this lock is used to prevent concurrency when using -T option in maven.
	// this is to workaround the issue having multiple ep-cores running
	// at the same time is unsupported
	private static final Object JVM_LOCK = new Object();

	@Parameter(required = true, defaultValue = "false")
	private boolean skip;

	@Parameter(property = "jdbc.url", required = true)
	private String jdbcUrl;

	@Parameter(property = "metabase.username", required = true)
	private String jdbcUsername;

	@Parameter(property = "metabase.password", required = true)
	private String jdbcPassword;

	@Parameter(property = "metabase.jdbc.driver", required = true)
	private String jdbcDriverClass;

	//the folder used for storing/reading CSV/JSON files
	@Parameter(property = "result.stats.folder.path")
	private String dbStatsFolderPath;

	@Parameter(property = "performance.metabase.dump.file")
	private String performanceMetabaseDumpFilePath;

	private MetabaseDataSource metabaseDataSource;
	private DataSource dataSource;

	protected void setPerformanceMetabaseDumpFilePath(final String performanceMetabaseDumpFilePath) {
		this.performanceMetabaseDumpFilePath = performanceMetabaseDumpFilePath;
	}

	/**
	 * Returns the path of the folder holding performance statistic files (CSV and JSON).
	 *
	 * @return the folder path
	 * @throws MojoExecutionException the exception
	 */
	public String getDbStatsFolderPath() throws MojoExecutionException {
		if (isEmpty(dbStatsFolderPath)) {
			throw new MojoExecutionException("result.stats.folder.path' property is not set. Use Maven or system properties");
		}
		return dbStatsFolderPath;
	}

	/**
	 * Returns the path of the folder where a file with the latest performance records is stored to/read from.
	 *
	 * @return the folder path
	 * @throws MojoExecutionException the exception
	 */
	public String getPerformanceMetabaseDumpFilePath() throws MojoExecutionException {
		if (isEmpty(performanceMetabaseDumpFilePath)) {
			throw new MojoExecutionException("'performance.metabase.dump.file' property is not set. Use Maven or system properties");
		}
		return performanceMetabaseDumpFilePath;
	}

	/**
	 * Initialize datasource. Initialize and execute a mojo.
	 *
	 * @throws MojoExecutionException the mojo execution exception
	 * @throws MojoFailureException the mojo failure exception
	 */
	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info("Skipping metabase-processor execution");
			return;
		}
		synchronized (JVM_LOCK) {
			try {
				initDataSource();
				executeMojo();
			} finally {
				if (metabaseDataSource != null) {
					try {
						metabaseDataSource.close();
					} catch (SQLException sqlException) {
						//noop
					}
				}
			}
		}
	}

	/**
	 * Implemented and executed by the target mojo.
	 * 
	 * @throws MojoExecutionException the mojo execution exception
	 * @throws MojoFailureException the mojo failure exception
	 */
	abstract void executeMojo() throws MojoExecutionException, MojoFailureException;

	private void initDataSource() throws MojoExecutionException {
		getLog().info("Connecting to database using following db params:\nJdbc URL: " + jdbcUrl
				+ "\nUsername: " + jdbcUsername + "\nPassword length: " + jdbcPassword.length() + "\nJdbc driver: "
				+ jdbcDriverClass);

		try {
			this.metabaseDataSource = new MetabaseDataSource(jdbcUrl, jdbcUsername, jdbcPassword, jdbcDriverClass);
			this.dataSource = metabaseDataSource.createDataSource();

			getLog().info("Successfully connected to the database");
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}

	/**
	 * Get JDBC url.
	 *
	 * @return the jdbc url
	 */
	protected String getJdbcUrl() {
		return jdbcUrl;
	}

	/**
	 * Get JDBC driver class.
	 *
	 * @return JDBC driver class
	 */
	protected String getJdbcDriverClass() {
		return jdbcDriverClass;
	}

	/**
	 * Get JDBC connection.
	 *
	 * @return the JDBC connection
	 * @throws SQLException the SQL exception
	 */
	protected Connection getConnection() throws SQLException {
		return this.dataSource.getConnection();
	}

	/**
	 * Helper method for wrapping a string with single quotes.
	 *
	 * @param stringToWrap the string to wrap
	 * @return string the wrapped string
	 */
	protected String wrapWithSinglQuote(final String stringToWrap) {
		return StringUtils.wrap(stringToWrap, SINGLE_QUOTE);
	}

	/**
	 * Read the content of all CSV files found under the {@link #dbStatsFolderPath} folder.
	 *
	 * @return list of CVS lines
	 * @throws MojoExecutionException exception
	 */
	protected List<CSVLineBean> readCSVLines() throws MojoExecutionException {
		getLog().info("Reading CSV files from [" + getDbStatsFolderPath() + "] ...");

		File[] csvFiles = new File(getDbStatsFolderPath()).listFiles((dir, name) -> name.endsWith(".csv"));

		if (csvFiles == null || csvFiles.length == 0) {
			throw new MojoExecutionException("CSV files with performance results are not found. Can't proceed further");
		}

		List<CSVLineBean> csvLines = new ArrayList<>();
		for (File csvFile : csvFiles) {
			try (Reader csvFileReader = Files.newBufferedReader(csvFile.toPath(), UTF_8)) {
				List<String> applicationCsvLines = IOUtils.readLines(csvFileReader);
				applicationCsvLines.remove(CSV_HEADER_ROW_INDEX);

				applicationCsvLines.forEach(csvLine -> {
					String trimmedCsvLine = csvLine.trim();
					if (!trimmedCsvLine.isEmpty() && trimmedCsvLine.split(",").length == EIGHT) {
						csvLines.add(new CSVLineBean(csvLine));
					}
				});

			} catch (Exception ex) {
				throw new MojoExecutionException(ex.getMessage(), ex);
			}
		}

		getLog().info("Found [" + csvLines.size() + "] tests");
		return csvLines;
	}
}
