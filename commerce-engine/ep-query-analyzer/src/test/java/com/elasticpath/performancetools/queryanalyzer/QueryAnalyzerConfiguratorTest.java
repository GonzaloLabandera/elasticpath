/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performancetools.queryanalyzer;

import static com.elasticpath.performancetools.queryanalyzer.utils.Defaults.NOT_AVAILABLE;
import static com.elasticpath.performancetools.queryanalyzer.utils.SystemProperties.RESULT_STATS_FOLDER_PATH_SYSTEM_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link QueryAnalyzerConfigurator}.
 */
public class QueryAnalyzerConfiguratorTest {
	@Before
	public void clean() {
		System.getProperties().remove(RESULT_STATS_FOLDER_PATH_SYSTEM_PROPERTY);
		QueryAnalyzerConfigurator.INSTANCE.clean();
	}

	@Test
	public void shouldReturnResultFolderPathSetAsSysProperty() {
		String classLoaderPath = getClass().getClassLoader().getResource(".").getPath();
		System.setProperty(RESULT_STATS_FOLDER_PATH_SYSTEM_PROPERTY, classLoaderPath);

		String actualResultFolderPath = QueryAnalyzerConfigurator.INSTANCE.getResultFolderPath();

		assertThat(actualResultFolderPath)
				.isEqualTo(classLoaderPath);
	}

	@Test
	public void shouldReturnUserHomeAsResultFolderPathy() {
		String userHome = System.getProperty("user.home");

		String actualResultFolderPath = QueryAnalyzerConfigurator.INSTANCE.getResultFolderPath();

		assertThat(actualResultFolderPath)
				.isEqualTo(userHome);
	}

	@Test
	public void shouldPrintDefaultConfiguration() {
		String expectedConfiguraiton = "Query analyzer configuration:\nTest ID:" + NOT_AVAILABLE + "\n"
				+ "Test name:" + NOT_AVAILABLE + "\n"
				+ "Application name:cortex\n"
				+ "Result folder path:" + System.getProperty("user.home") + "\n"
				+ "Are all Ehcache caches cleaned?:false\n"
				+ "Output file extensions:[]\n"
				+ "Input log file:null\n"
				+ "JMX Client:null\n";

		String actualDefaultConfiguration = QueryAnalyzerConfigurator.INSTANCE.printConfiguration();

		assertThat(actualDefaultConfiguration)
				.isEqualTo(expectedConfiguraiton);

	}

	//not all fields could be set in the test
	@Test
	public void shouldPrintPartialConfiguration() {
		String expectedConfiguration = "Query analyzer configuration:\nTest ID:1\n"
				+ "Test name:Zoom test\n"
				+ "Application name:cortex\n"
				+ "Result folder path:" + System.getProperty("user.home") + "\n"
				+ "Are all Ehcache caches cleaned?:false\n"
				+ "Output file extensions:[csv, json]\n"
				+ "Input log file:null\n"
				+ "JMX Client:null\n";

		QueryAnalyzerConfigurator instance = QueryAnalyzerConfigurator.INSTANCE;
		instance.setTestId("1");
		instance.setTestName("Zoom test");
		instance.setOutputFileExtensions(Arrays.asList("csv", "json"));

		String actualDefaultConfiguration = instance.printConfiguration();

		assertThat(actualDefaultConfiguration)
				.isEqualTo(expectedConfiguration);

	}
}
