/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer.utils;

import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.CSV_OUTPUT_FILE_EXTENSION;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.DEFAULT_OUTPUT_FILE_NAME_PREFIX;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.DOT_DELIMITER;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.JSON_OUTPUT_FILE_EXTENSION;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.TIMESTAMP_FORMAT_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.Patterns.TIMESTAMP_PATTERN;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.PRINT_JSON_TO_CONSOLE_ONLY_SYSTEM_PROPERTY;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.RESULT_STATS_FILE_NAME_SYSTEM_PROPERTY;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;

import com.elasticpath.performancetools.queryanalyzer.QueryAnalyzerConfigurator;
import com.elasticpath.performancetools.queryanalyzer.beans.JPAQuery;
import com.elasticpath.performancetools.queryanalyzer.beans.Operation;

/**
 * Util class for performing various operations.
 */
@SuppressWarnings("PMD.UnsynchronizedStaticDateFormatter")
public final class Utils {

	private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat(TIMESTAMP_FORMAT_PATTERN, Locale.getDefault());

	private Utils() {
		//Utility class
	}

	/**
	 * Return the reference to an output JSON file, if not overridden by
	 * -Dprint.json.to.console.only system property.
	 *
	 * @param outputFileExtension  the output file extension
	 * @return null if JSON output is redirected to console;
	 * otherwise reference to JSON output file.
	 */
	public static File getOutputFileIfEnabled(final String outputFileExtension) {
		//system property has a precedence over input param
		if (isNotBlank(System.getProperty(PRINT_JSON_TO_CONSOLE_ONLY_SYSTEM_PROPERTY))) {
			return null;
		}

		final File outputFile = getOutputResultFile(outputFileExtension);
		final File parent = outputFile.getParentFile();

		if (!parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Folder [" + parent.getAbsolutePath() + "] couldn't be created");
		}

		return outputFile;
	}

	/**
	 * Set operation thread name.
	 *
	 * @param threadNameMatcher the thread name matcher.
	 * @param operation         the operation to set thread name to.
	 */
	public static void setOperationThreadName(final Matcher threadNameMatcher, final Operation operation) {
		if (threadNameMatcher.find()) {
			final String threadName = threadNameMatcher.group(1);
			operation.setThread(threadName);
		}
	}

	/**
	 * Set operation timestamp.
	 *
	 * @param timestampMatcher    the timestamp matcher.
	 * @param operation           the operation to set timestamp to.
	 * @param isPreviousOperation flag indicating whether given operation is new or old.
	 * @throws Exception the exception.
	 */
	public static void setOperationTimestamp(final Matcher timestampMatcher, final Operation operation,
											 final boolean isPreviousOperation) throws Exception {
		if (timestampMatcher.find()) {
			final String timestamp = timestampMatcher.group(1);
			final Date date = SIMPLE_DATE_FORMAT.parse(timestamp);
			if (isPreviousOperation) {
				operation.setFinishedAt(date);
			} else {
				operation.setStartedAt(date);
			}
		}
	}

	/**
	 * Set operation start/stop timestamps.
	 *
	 * @param timestampMatcher the timestamp matcher
	 * @param operation        the operation
	 * @param isStop           a flag indicating whether start or stop time should be set
	 * @throws Exception the exception
	 */
	public static void setOperationJpaStartStopTimestamp(final Matcher timestampMatcher, final Operation operation,
														 final boolean isStop) throws Exception {
		if (timestampMatcher.find()) {
			final String timestamp = timestampMatcher.group(1);
			final Date date = SIMPLE_DATE_FORMAT.parse(timestamp);
			if (isStop) {
				operation.setJpaFinishedOfAt(date);
			} else {
				operation.setJpaKickedInAt(date);
			}
		}
	}

	/**
	 * Set a starting timestamp of a JPA query being executed.
	 *
	 * @param timestampMatcher the matcher
	 * @param jpaQuery         the JPAQuery instance
	 * @throws Exception the exception
	 */
	public static void setJpaQueryStartTimestamp(final Matcher timestampMatcher, final JPAQuery jpaQuery) throws Exception {
		if (timestampMatcher.find()) {
			final String timestamp = timestampMatcher.group(1);
			final Date date = SIMPLE_DATE_FORMAT.parse(timestamp);
			jpaQuery.setStartedAt(date);
		}
	}

	/**
	 * Set found eager relations to a JPA query instance.
	 *
	 * @param eagerRelationsMatcher the matcher for eager relations string.
	 * @param jpaQuery              the JPA query instance to set eager relations to.
	 */
	public static void setJPAQueryEagerRelations(final Matcher eagerRelationsMatcher, final JPAQuery jpaQuery) {
		if (eagerRelationsMatcher.find()) {
			final String eagerRelations = eagerRelationsMatcher.group(1);
			jpaQuery.addEagerRelations(eagerRelations);
		}
	}

	/**
	 * JPA and SQL queries are usually broken into several lines. This method aggregates all query lines
	 * into a single buffer, until next line, starting with timestamp, is found.
	 *
	 * @param reader      the reader to read from.
	 * @param queryBuffer the buffer for storing query lines.
	 * @return the last line starting with timestamp.
	 * @throws Exception the exception.
	 */
	public static String processMultiLineQueries(final BufferedReader reader, final StringBuilder queryBuffer) throws Exception {
		queryBuffer.append('\n');

		String line;
		while ((line = reader.readLine()) != null) {
			final Matcher timestampMatcher = TIMESTAMP_PATTERN.matcher(line);
			if (timestampMatcher.find()) {
				queryBuffer.append(line);
				break;
			}
			queryBuffer.append(line).append('\n');
		}

		if (line == null) { //we probably reached the end of file
			line = "";
		}
		return line;
	}

	/**
	 * Removes TAB and replace CR chars with a single space.
	 *
	 * @param input string to be processed.
	 * @return clean string.
	 */
	public static String removeTabAndCRChars(final String input) {
		return input.replaceAll("\\t", "")
				.replaceAll("\\n", " ");
	}

	/**
	 * Get the output file extension from the "result.file.name" system property or default to "json".
	 * @return the output file extension
	 */
	public static String getOutputFileExtension() {
		return System.getProperty(RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY, JSON_OUTPUT_FILE_EXTENSION);
	}
	/*
	 * Build the full path to the output file, using all available system properties and defaults.
	 *
	 * @return the full path of the output file for storing statistics
	 * @param outputFileExtension the output file extension
	 */
	private static File getOutputResultFile(final String outputFileExtension) {
		QueryAnalyzerConfigurator qaConfiguratorInstance = QueryAnalyzerConfigurator.INSTANCE;

		String outputFolderPath = qaConfiguratorInstance.getResultFolderPath();

		String testIdPrefix = qaConfiguratorInstance.getTestId();
		if (isNotEmpty(testIdPrefix)) {
			testIdPrefix += "_";
		}
		String applicationNamePrefix = qaConfiguratorInstance.getApplicationName() + "_";

		String outputFileName = System.getProperty(RESULT_STATS_FILE_NAME_SYSTEM_PROPERTY, getFullOutputFileName(outputFileExtension));

		if (CSV_OUTPUT_FILE_EXTENSION.equalsIgnoreCase(outputFileExtension)) {
			return new File(outputFolderPath, applicationNamePrefix + outputFileName);
		}
		return new File(outputFolderPath, testIdPrefix + applicationNamePrefix + outputFileName);
	}

	private static String getFullOutputFileName(final String overridingOutputFileExtension) {
		String outputFileExtension;

		if (StringUtils.isEmpty(overridingOutputFileExtension)) {
			outputFileExtension = System.getProperty(RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY, JSON_OUTPUT_FILE_EXTENSION);
		} else {
			outputFileExtension = overridingOutputFileExtension;
		}

		return join(DOT_DELIMITER, DEFAULT_OUTPUT_FILE_NAME_PREFIX, outputFileExtension);
	}
}
