/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import static com.elasticpath.performance.mojo.beans.ResultBean.LOCAL_DEV_COMPARISON;
import static com.elasticpath.performance.mojo.utils.FileUtils.readFile;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.FULL_REPORT_FILE_NAME;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.PARTIAL_REPORT_FILE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.performance.mojo.beans.ResultBean;

/** Test class for {@link ReportUtils}. */
@RunWith(MockitoJUnitRunner.class)
public class ReportUtilsTest {
	private static final String EPC_VERSION = "master";
	private static final int MAX_ALLOWED_DEVIATION = 5;
	private static final int MAX_RESULTS_TO_SHOW = 1; //for testing; otherwise is 15
	private static final String LOCAL_REPORT_BASE_URL = "http://local";

	private static final int MAGIC_10 = 10;
	private static final int MAGIC_20 = 20;
	private static final int MAGIC_30 = 30;
	private static final int MAGIC_40 = 40;
	private static final String LOCAL_LABEL = "local:";
	
	@SuppressWarnings("PMD.ProperLogger")
	@Mock private Log logger;

	private ReportUtils reportUtils;
	private String samplesFolderPath;
	private String reportFolderPath;

	@Before
	public void init() {
		String baseOutputFolderPath = getClass().getClassLoader().getResource(".").getPath();
		this.samplesFolderPath = baseOutputFolderPath + "samples";
		this.reportFolderPath = baseOutputFolderPath + "htmlReportFolder";

		reportUtils = new ReportUtils(MAX_ALLOWED_DEVIATION, MAX_RESULTS_TO_SHOW, reportFolderPath, LOCAL_REPORT_BASE_URL);
	}

	@Test
	public void shouldCreateLocalReport() throws MojoExecutionException, IOException {
		String expectedLocalReport = readFile(samplesFolderPath, "sampleLocalReport.html");

		ResultBean result = createResult();

		reportUtils.createLocalReport(EPC_VERSION, Collections.singletonList(result), logger);

		String actualLocalReport = readFile(reportUtils.getReportFolderPath(), FULL_REPORT_FILE_NAME);

		assertThat(actualLocalReport)
				.isEqualTo(expectedLocalReport);
	}

	//when number of results exceeds the limit, there should be 2 PR reports: partial with LIMIT number of results and the full report
	@Test
	public void shouldCreatePartialAndFullPRReportsWhenNumberOfResultsExceedsLimit() throws MojoExecutionException, IOException {
		ResultBean result1 = createResult();
		ResultBean result2 = createResult();

		reportUtils.createPRReport(EPC_VERSION, Arrays.asList(result1, result2), logger);

		String expectedPartialPRReport = readFile(samplesFolderPath, "samplePartialPRReportWithOneResult.html");
		String actualPartialPRReport = readFile(reportUtils.getReportFolderPath(), PARTIAL_REPORT_FILE_NAME);

		assertThat(actualPartialPRReport)
				.isEqualTo(expectedPartialPRReport);

		String expectedFullPRReport = readFile(samplesFolderPath, "sampleFullPRReport.html");
		String actualFullPRReport = readFile(reportUtils.getReportFolderPath(), FULL_REPORT_FILE_NAME);

		assertThat(actualFullPRReport)
				.isEqualTo(expectedFullPRReport);
	}

	@Test
	public void shouldCreatePartialPRReportsWhenNumberOfResultsIsWithinLimit() throws MojoExecutionException, IOException {
		reportUtils = new ReportUtils(MAX_ALLOWED_DEVIATION, 2, reportFolderPath, LOCAL_REPORT_BASE_URL);

		ResultBean result1 = createResult();
		ResultBean result2 = createResult();

		reportUtils.createPRReport(EPC_VERSION, Arrays.asList(result1, result2), logger);

		String expectedPartialPRReport = readFile(samplesFolderPath, "samplePartialPRReportWithTwoResults.html");
		String actualPartialPRReport = readFile(reportUtils.getReportFolderPath(), PARTIAL_REPORT_FILE_NAME);

		assertThat(actualPartialPRReport)
				.isEqualTo(expectedPartialPRReport);

		File actualFullPRReport = new File(reportUtils.getReportFolderPath(), FULL_REPORT_FILE_NAME);

		assertThat(actualFullPRReport)
				.doesNotExist();
	}

	private ResultBean createResult() {
		ResultBean resultWithNewTest = mock(ResultBean.class);

		Map<String, Object> diffDataNewTest = new HashMap<>();
		diffDataNewTest.put("SelectsTooltip", LOCAL_LABEL + MAGIC_10);
		diffDataNewTest.put("SelectsDelta", MAGIC_10);
		diffDataNewTest.put("SelectsStatus", "");
		diffDataNewTest.put("InsertsTooltip", LOCAL_LABEL + MAGIC_20);
		diffDataNewTest.put("InsertsDelta", MAGIC_20);
		diffDataNewTest.put("InsertsStatus", "");
		diffDataNewTest.put("UpdatesTooltip", LOCAL_LABEL + MAGIC_30);
		diffDataNewTest.put("UpdatesDelta", MAGIC_30);
		diffDataNewTest.put("UpdatesStatus", "");
		diffDataNewTest.put("DeletesTooltip", LOCAL_LABEL + MAGIC_40);
		diffDataNewTest.put("DeletesDelta", MAGIC_40);
		diffDataNewTest.put("DeletesStatus", "");
		diffDataNewTest.put("DBTimeDelta", 0);


		when(resultWithNewTest.getTestName()).thenReturn("Test name");
		when(resultWithNewTest.getTestId()).thenReturn("1");
		when(resultWithNewTest.getApplication()).thenReturn("Application");
		when(resultWithNewTest.getLocalReportBaseUrl()).thenReturn(LOCAL_REPORT_BASE_URL);
		when(resultWithNewTest.getLocalReportType()).thenReturn("local");
		when(resultWithNewTest.getDiffData()).thenReturn(diffDataNewTest);
		when(resultWithNewTest.getReleaseResult()).thenReturn(null);
		when(resultWithNewTest.getComparisonType()).thenReturn(LOCAL_DEV_COMPARISON);

		return resultWithNewTest;
	}
}
