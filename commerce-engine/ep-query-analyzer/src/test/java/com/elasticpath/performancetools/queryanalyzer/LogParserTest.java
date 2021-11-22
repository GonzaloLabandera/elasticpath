/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer;

import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.CSV_OUTPUT_FILE_EXTENSION;
import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.JSON_OUTPUT_FILE_EXTENSION;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.LOG_FILE_PATH_SYSTEM_PROPERTY;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY;
import static com.elasticpath.performancetools.queryanalyzer.utils.Utils.getOutputFileExtension;
import static com.elasticpath.performancetools.queryanalyzer.utils.Utils.getOutputFileIfEnabled;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Test;

import com.elasticpath.performancetools.queryanalyzer.beans.QueryStatistics;
import com.elasticpath.performancetools.queryanalyzer.exceptions.QueryAnalyzerException;

/**
 * Test class for {@link LogParser}.
 */
public class LogParserTest {
	private static final String PARTIAL_STATISTICS_FILE_NAME = "partial_statistics.ser";

	@After
	public void clean() {
		QueryAnalyzerConfigurator.INSTANCE.setOutputFileExtensions(new ArrayList<>());
		System.getProperties().remove(RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY);
		System.getProperties().remove(LOG_FILE_PATH_SYSTEM_PROPERTY);
	}

	@Test
	public void shouldParseLogFileAndCreateIdenticalStatistics() throws Exception {
		final File testCortexLog = new File(getResourcePathFromClassLoader("TestCortex.log"));
		final QueryStatistics expectedStatistics = restoreQueryStatisticsFromFile(PARTIAL_STATISTICS_FILE_NAME);

		System.setProperty(LOG_FILE_PATH_SYSTEM_PROPERTY, testCortexLog.getAbsolutePath());

		QueryAnalyzerConfigurator.INSTANCE.setLogFile();
		final QueryStatistics actualStatistics = LogParser.INSTANCE.parse();

		// Remove timestamps, as they are irrelevant.
		actualStatistics.getOperations().stream()
				.flatMap(operation -> operation.getJpaQueries().stream())
				.forEach(jpaQuery -> jpaQuery.setStartedAt(null));
		/*
			If any of objects, constituents of QueryStatistics, is changed then partial_statistics.ser must be recreated with
			The generated file will be saved under this project base folder.

			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("partial_statistics.ser"));
			os.writeObject(actualStatistics);
		 */
		//after parsing, QueryStatistics contains only operations
		assertThat(actualStatistics.getOperations())
				.as("Expected and actual operations must be the same")
				.isEqualTo(expectedStatistics.getOperations());

	}

	@Test
	public void shouldGenerateStatisticsAndSaveToJSON() throws Exception {
		final QueryStatistics actualStatistics = restoreQueryStatisticsFromFile(PARTIAL_STATISTICS_FILE_NAME);
		actualStatistics.generateStatistics();

		/*
			If any of objects, constituents of QueryStatistics, is changed then full_statistics.ser must be recreated with
			The generated file will be saved under this project base folder.

			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("full_statistics.ser"));
			os.writeObject(actualStatistics);
		 */

		final File outputJSONStatistics = getOutputFileIfEnabled(getOutputFileExtension());

		assertThat(outputJSONStatistics.getName().endsWith(JSON_OUTPUT_FILE_EXTENSION))
				.isTrue();
		assertThat(outputJSONStatistics)
				.exists();

		final QueryStatistics expectedStatistics = restoreQueryStatisticsFromFile("full_statistics.ser");

		assertThat(actualStatistics)
				.isEqualTo(expectedStatistics);
	}

	@Test
	public void shouldGenerateStatisticsAndSaveToCSV() throws Exception {
		final QueryStatistics actualStatistics = restoreQueryStatisticsFromFile(PARTIAL_STATISTICS_FILE_NAME);
		QueryAnalyzerConfigurator.INSTANCE.setOutputFileExtensions(Collections.singletonList(CSV_OUTPUT_FILE_EXTENSION));
		actualStatistics.generateStatistics();

		final File outputCSVStatistics = getOutputFileIfEnabled(QueryAnalyzerConfigurator.INSTANCE.getOutputFileExtensions().get(0));

		assertThat(outputCSVStatistics.getName().endsWith(CSV_OUTPUT_FILE_EXTENSION))
				.isTrue();
		assertThat(outputCSVStatistics)
				.exists();
	}

	@Test
	public void shouldGenerateStatisticsAndSaveToJSONAndCSVFormats() throws Exception {
		final QueryStatistics actualStatistics = restoreQueryStatisticsFromFile(PARTIAL_STATISTICS_FILE_NAME);
		List<String> supportedExtensions = Lists.newArrayList(CSV_OUTPUT_FILE_EXTENSION, JSON_OUTPUT_FILE_EXTENSION);

		QueryAnalyzerConfigurator.INSTANCE.setOutputFileExtensions(supportedExtensions);

		actualStatistics.generateStatistics();

		for (String supportedExtension : supportedExtensions) {
			final File outputFile = getOutputFileIfEnabled(supportedExtension);

			assertThat(outputFile.getName().endsWith(supportedExtension))
					.isTrue();
			assertThat(outputFile)
					.exists();
		}
	}

	@Test
	public void shouldThrowExceptionWhenUnsupportedOutputFormatIsUsed() throws Exception {
		final QueryStatistics actualStatistics = restoreQueryStatisticsFromFile(PARTIAL_STATISTICS_FILE_NAME);
		String invalidOutputFormat = "invalid";

		QueryAnalyzerConfigurator.INSTANCE.setOutputFileExtensions(Collections.singletonList(invalidOutputFormat));

		assertThatThrownBy(actualStatistics::generateStatistics)
				.hasMessage("Unsupported output format [" + invalidOutputFormat + "]")
				.isInstanceOf(QueryAnalyzerException.class);

	}

	@Test
	public void shouldSaveStatsFileInFormatSetAsSysProperty() throws Exception {
		System.setProperty(RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY, CSV_OUTPUT_FILE_EXTENSION);

		restoreQueryStatisticsFromFile(PARTIAL_STATISTICS_FILE_NAME)
				.generateStatistics();

		final File outputCSVStatistics = getOutputFileIfEnabled(QueryAnalyzerConfigurator.INSTANCE.getOutputFileExtensions().get(0));

		assertThat(outputCSVStatistics.getName().endsWith(CSV_OUTPUT_FILE_EXTENSION))
				.isTrue();
		assertThat(outputCSVStatistics)
				.exists();
	}

	@Test
	public void shouldThrowExceptionWhenUnsupportedOutputFormatIsSetAsSystemProperty() throws Exception {
		String invalidOutputFormat = "invalid";
		System.setProperty(RESULT_STATS_FILE_FORMAT_SYSTEM_PROPERTY, invalidOutputFormat);

		final QueryStatistics actualStatistics = restoreQueryStatisticsFromFile(PARTIAL_STATISTICS_FILE_NAME);

		assertThatThrownBy(actualStatistics::generateStatistics)
				.hasMessage("Unsupported output format [" + invalidOutputFormat + "]")
				.isInstanceOf(QueryAnalyzerException.class);
	}

	private String getResourcePathFromClassLoader(final String resource) {
		return getClass().getClassLoader().getResource(resource).getPath();
	}

	private QueryStatistics restoreQueryStatisticsFromFile(final String fileName) throws Exception {
		try (InputStream buffer = new BufferedInputStream(new FileInputStream(getResourcePathFromClassLoader(fileName)))) {
			ObjectInput input = new ObjectInputStream(buffer);
			QueryStatistics queryStatistics = (QueryStatistics) input.readObject();
			queryStatistics.init();

			return queryStatistics;
		}
	}
}