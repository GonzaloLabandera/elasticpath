/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */
package com.elasticpath.performance.mojo;

import static com.elasticpath.performance.mojo.beans.ResultBean.CI_PR_COMPARISON;
import static com.elasticpath.performance.mojo.beans.ResultBean.CUSTOMER_DEV_COMPARISON;
import static com.elasticpath.performance.mojo.beans.ResultBean.LOCAL_DEV_COMPARISON;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.H2_DRIVER_CLASS;
import static com.elasticpath.performance.mojo.utils.DbQueryUtils.getFormattedLatestReleaseDataQuery;
import static org.apache.commons.lang3.StringUtils.appendIfMissing;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.elasticpath.performance.mojo.beans.CSVLineBean;
import com.elasticpath.performance.mojo.beans.ResultBean;
import com.elasticpath.performance.mojo.utils.ReportUtils;

/**
 * This mojo is used in several profiles:
 * - 'compare-with-imported-metabase' (customer dev)
 * - 'compare-with-remote-metabase' (EP dev)
 * - 'ci-pr-compare-performance-results' (CI PR build job)
 *
 * The mojo compares the performance results obtained from the local tests with either remote (EP) or imported (customer) metabase ones.
 *
 * Depending on the profile, the comparison results are handled differently:
 *
 * - 'compare-with-imported-metabase'
 * 		the results are stored to
 * 		'/extensions/system-tests/performance-tests/cucumber/target/performance-html-report/fullPrHtmlPerformanceReport.html'
 *
 * - 'compare-with-remote-metabase'
 * 		the results are stored to
 * 		'/extensions/system-tests/performance-tests/cucumber/target/performance-html-report/fullPrHtmlPerformanceReport.html'
 *
 * - 'ci-pr-compare-performance-results'
 *
 *    if the number of records do not exceed the limit (default: 15; configured with: 'max.results.to.show') the results are stored to
 * 	  '/extensions/system-tests/performance-tests/cucumber/target/performance-html-report/limitedPrHtmlPerformanceReport.html'
 * 	  and sent as GitHub comment back to PR.
 *
 * 	  Otherwise, the 'max.results.to.show' records is saved to
 * 	  '/extensions/system-tests/performance-tests/cucumber/target/performance-html-report/limitedPrHtmlPerformanceReport.html'
 * 	  and sent back to PR, while the full report is saved to
 * 	  '/extensions/system-tests/performance-tests/cucumber/target/performance-html-report/fullPrHtmlPerformanceReport.html'
 * 	  and published as a build artifact so it can be accessed from the PR.
 *
 */
@SuppressWarnings({"PMD.ConsecutiveLiteralAppends", "PMD.AvoidStringBufferField"})
@Mojo(name = "compare-results", threadSafe = true)
public class ResultsComparatorMojo extends AbstractMetabaseMojo {
	@Parameter(property = "epc.version", required = true, defaultValue = "master")
	private String epcVersion;

	//points to the file/network location of the local results
	@Parameter(property = "local.report.base.url", required = true)
	private String localReportBaseURL;

	//S3 location
	@Parameter(property = "release.report.base.url")
	private String releaseReportBaseURL;

	@Parameter(property = "max.results.to.show", defaultValue = "15")
	private int maxResultsToShow;

	//how much variability should be tolerated
	@Parameter(property = "max.allowed.deviation.percent", defaultValue = "5")
	private int maxAllowedDeviation;

	@Parameter(property = "pr.id")
	private String prId;

	//folder where HTMLs with comparison results will be stored
	@Parameter(property = "report.folder.path", required = true)
	private String reportFolderPath;

	private int comparisonType = CI_PR_COMPARISON;

	/**
	 * Read the local results, using generated CSV files, and compare them with the remote/imported ones.
	 * Depending on the comparison type (PR or customer/EP), as well as the number of results per GitHub commit (applicable to PR),
	 * different files will be created (see this mojo's javadoc).
	 *
	 * @throws MojoExecutionException the exception
	 */
	@Override
	public void executeMojo() throws MojoExecutionException {
		getLog().info("Processing PR performance data ...");

		initMojo();

		List<ResultBean> comparisonResults = compareLocalAndReleaseResults();

		ReportUtils reportUtils = new ReportUtils(maxAllowedDeviation, maxResultsToShow, reportFolderPath, localReportBaseURL);

		if (comparisonType == CI_PR_COMPARISON) {
			reportUtils.createPRReport(epcVersion, comparisonResults, getLog());
		} else {
			reportUtils.createLocalReport(epcVersion, comparisonResults, getLog());
		}
	}

	private void initMojo() throws MojoExecutionException {
		determineComparisonType();

		assertRequirementsForCIPRComparison();

		localReportBaseURL = normalizeUrl(localReportBaseURL);
	}

	private void determineComparisonType() {
		// dev profiles use local folder for downloading reports from
		if (!localReportBaseURL.startsWith("http")) {
			//customer dev profile always uses H2 with imported release performance results
			if (getJdbcDriverClass().equals(H2_DRIVER_CLASS)) {
				comparisonType = CUSTOMER_DEV_COMPARISON;
			} else {
				comparisonType = LOCAL_DEV_COMPARISON;
			}
		}
	}

	private void assertRequirementsForCIPRComparison() throws MojoExecutionException {
		if (comparisonType == CI_PR_COMPARISON) {
			if (isEmpty(prId)) {
				throw new MojoExecutionException("'pr.id' parameter is required for CI runs");
			}
			if (isEmpty(releaseReportBaseURL)) {
				throw new MojoExecutionException("'release.full.report.base.url' is required for CI runs");
			}
			releaseReportBaseURL = normalizeUrl(releaseReportBaseURL);
		}
	}

	private String normalizeUrl(final String urlToNormalize) {
		return appendIfMissing(urlToNormalize, "/");
	}

	private List<ResultBean> compareLocalAndReleaseResults() throws MojoExecutionException {
		List<ResultBean> comparisonResults = new ArrayList<>();

		Map<String, ResultBean> releaseResults = getLatestReleaseResults();

		List<CSVLineBean> csvLines = readCSVLines();

		for (CSVLineBean csvLine : csvLines) {
			ResultBean localResult = new ResultBean(epcVersion, localReportBaseURL, releaseReportBaseURL, maxAllowedDeviation, csvLine);
			ResultBean releaseResult = releaseResults.get(localResult.mapKey());

			localResult.diff(comparisonType, releaseResult);

			comparisonResults.add(localResult);
		}

		//sort results by status (failures first) and application name
		Collections.sort(comparisonResults);

		return comparisonResults;
	}

	private Map<String, ResultBean> getLatestReleaseResults() throws MojoExecutionException {
		getLog().info("Getting latest release results from metabase using Jdbc URL: [" + getJdbcUrl() + "] ...");

		String formattedQuery = getFormattedLatestReleaseDataQuery(comparisonType, epcVersion);

		if (getLog().isDebugEnabled()) {
			getLog().debug("Formatted latest release data query:\n" + formattedQuery);
		}

		try (Connection connection = getConnection();
			 Statement queryStatement = connection.createStatement();
			 ResultSet resultSet = queryStatement.executeQuery(formattedQuery)) {

			Map<String, ResultBean> releaseResults = new HashMap<>();
			while (resultSet.next()) {
				ResultBean releaseResult = new ResultBean(resultSet);
				releaseResults.put(releaseResult.mapKey(), releaseResult);
			}

			getLog().info("The latest release results are successfully retrieved from the metabase");
			return releaseResults;
		} catch (Exception ex) {
			getLog().error("The results couldn't be retrieved from the metabase. The query statement:\n " + formattedQuery);
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}


}
