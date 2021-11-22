/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import static com.elasticpath.performance.mojo.beans.ResultBean.CUSTOMER_DEV_COMPARISON;
import static com.elasticpath.performance.mojo.utils.Constants.MAX_TEST_NAME_LEN_CHARS;
import static com.elasticpath.performance.mojo.utils.Constants.PIPE_WITH_SPACES;
import static com.elasticpath.performance.mojo.utils.Constants.QUESTION_EMOJI;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.FULL_HTML_REPORT_FOOTER;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.FULL_HTML_REPORT_HEADER;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.FULL_REPORT_FILE_NAME;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.HTML_RESULT_LINE_TEMPLATE;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.PARTIAL_HTML_REPORT_FOOTER_TEMPLATE;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.PARTIAL_HTML_REPORT_HEADER_TEMPLATE;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.PARTIAL_REPORT_FILE_NAME;
import static com.elasticpath.performance.mojo.utils.HTMLUtils.replaceGitHubEmojisWithIcons;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.abbreviate;
import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.wrap;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.elasticpath.performance.mojo.beans.ResultBean;

/**
 * Formats comparison result beans into HTML.
 */
public class ReportUtils {
	private final int maxAllowedDeviation;
	private final int maxResultsToShow;
	private final String reportFolderPath;
	private final String localReportBaseURL;

	/**
	 * Custom constructor.
	 *
	 * @param maxAllowedDeviation the maximum allowed error deviation
	 * @param maxResultsToShow the maximum number of results to show
	 * @param reportFolderPath the report folder path
	 * @param localReportBaseURL the URL of the local folder containing report
	 */
	public ReportUtils(final int maxAllowedDeviation, final int maxResultsToShow, final String reportFolderPath, final String localReportBaseURL) {
		this.maxAllowedDeviation = maxAllowedDeviation;
		this.maxResultsToShow = maxResultsToShow;
		this.reportFolderPath = reportFolderPath;
		this.localReportBaseURL = localReportBaseURL;
	}

	public String getReportFolderPath() {
		return reportFolderPath;
	}

	/**
	 * Create HTML report for CI PR profile.
	 *
	 * @param epcVersion the epc version
	 * @param comparisonResults the list of results
	 * @param logger the logger
	 * @throws MojoExecutionException the exception
	 */
	public void createPRReport(final String epcVersion, final List<ResultBean> comparisonResults, final Log logger)
			throws MojoExecutionException {

		StringBuilder fullHtmlReport = new StringBuilder();
		StringBuilder partialHtmlReport = new StringBuilder(format(PARTIAL_HTML_REPORT_HEADER_TEMPLATE.toString(), epcVersion, maxAllowedDeviation));

		for (int i = 0; i < comparisonResults.size(); i++) {
			ResultBean result = comparisonResults.get(i);

			if (i < maxResultsToShow) {
				partialHtmlReport.append(formatResultAsHTMLTableRow(result));
			} else if (i == maxResultsToShow) {
				fullHtmlReport.append(FULL_HTML_REPORT_HEADER);
				fullHtmlReport.append(partialHtmlReport);

				partialHtmlReport.append(format(PARTIAL_HTML_REPORT_FOOTER_TEMPLATE.toString(), maxResultsToShow, localReportBaseURL));

				fullHtmlReport.append(formatResultAsHTMLTableRow(result));
			} else {
				fullHtmlReport.append(formatResultAsHTMLTableRow(result));
			}
		}

		if (fullHtmlReport.length() > 0) {
			fullHtmlReport.append(FULL_HTML_REPORT_FOOTER);

			String fullHtmlContent = replaceGitHubEmojisWithIcons(fullHtmlReport);

			saveReport(fullHtmlContent, FULL_REPORT_FILE_NAME, logger);
		} else {
			partialHtmlReport.append("</table>");
		}

		saveReport(partialHtmlReport.toString(), PARTIAL_REPORT_FILE_NAME, logger);
	}

	/**
	 * Create HTML report for EP/customer developer profile.
	 *
	 * @param epcVersion the epc version
	 * @param comparisonResults the list of results
	 * @param logger the logger
	 * @throws MojoExecutionException the exception
	 */
	public void createLocalReport(final String epcVersion, final List<ResultBean> comparisonResults, final Log logger)
			throws MojoExecutionException {

		String formattedPartialHtmlReportHeader = format(PARTIAL_HTML_REPORT_HEADER_TEMPLATE.toString(), epcVersion, maxAllowedDeviation);
		StringBuilder fullHtmlReport = new StringBuilder(FULL_HTML_REPORT_HEADER)
				.append(formattedPartialHtmlReportHeader);

		for (ResultBean result : comparisonResults) {
			fullHtmlReport.append(formatResultAsHTMLTableRow(result));
		}

		fullHtmlReport.append("</table></body></html>");

		String localReportContent = replaceGitHubEmojisWithIcons(fullHtmlReport);

		saveReport(localReportContent, FULL_REPORT_FILE_NAME, logger);
	}

	private void saveReport(final String contentToWrite, final String reportFileName, final Log logger)
			throws MojoExecutionException {

		boolean isJson = reportFileName.endsWith(".json");

		File reportFile = new File(reportFolderPath, reportFileName);

		logger.info("Saving content to " + reportFile.getAbsolutePath());
		if (logger.isDebugEnabled()) {
			logger.debug("Content\n" + contentToWrite);
		}

		File parentFolder = reportFile.getParentFile();

		if (parentFolder.exists() || reportFile.getParentFile().mkdirs()) {
			try (Writer writer = Files.newBufferedWriter(reportFile.toPath(), UTF_8)) {
				//PR comments are sent in JSON format
				if (isJson) {
					writer.write("{\"body\":\"" + contentToWrite + "\"}");
				} else {
					writer.write(contentToWrite);
				}
				writer.flush();
			} catch (Exception ex) {
				throw new MojoExecutionException(ex.getMessage(), ex);
			}
		} else {
			throw new MojoExecutionException("'" + reportFile.getParentFile().getAbsolutePath() + "' folder couldn't be created");
		}
	}

	private String formatResultAsHTMLTableRow(final ResultBean resultToShow) {
		Map<String, Object> diffData = resultToShow.getDiffData();
		String fullTestName = resultToShow.getTestName();

		String shortTestName = abbreviate(fullTestName, MAX_TEST_NAME_LEN_CHARS);
		boolean isNewTest = resultToShow.getReleaseResult() == null;
		String htmlTableRow = prepareHTMLTableRow(resultToShow);

		return format(htmlTableRow, capitalize(resultToShow.getApplication()), fullTestName, shortTestName,
				getQuestionEmoji(isNewTest),
				diffData.get("SelectsTooltip"), diffData.get("SelectsDelta"), diffData.get("SelectsStatus"),
				diffData.get("InsertsTooltip"), diffData.get("InsertsDelta"), diffData.get("InsertsStatus"),
				diffData.get("UpdatesTooltip"), diffData.get("UpdatesDelta"), diffData.get("UpdatesStatus"),
				diffData.get("DeletesTooltip"), diffData.get("DeletesDelta"), diffData.get("DeletesStatus"),
				diffData.get("DBTimeDelta"),
				getPerformanceReportURL(resultToShow.getLocalReportBaseUrl(), resultToShow),
				wrap(resultToShow.getLocalReportType(), " ")
		);
	}

	private String getQuestionEmoji(final boolean isNewTest) {
		//the emoji status for new tests is always "?"
		if (isNewTest) {
			return QUESTION_EMOJI;
		}
		return "";
	}

	private String prepareHTMLTableRow(final ResultBean resultToShow) {
		ResultBean releaseResult = resultToShow.getReleaseResult();

		StringBuilder resultLineBuilder = new StringBuilder(HTML_RESULT_LINE_TEMPLATE);

		if (resultToShow.getComparisonType() != CUSTOMER_DEV_COMPARISON && releaseResult != null) {
			String releasePerformanceReportUrl = getPerformanceReportURL(resultToShow.getRemoteReportBaseUrl(), releaseResult);
			String htmlReleaseReportAnchor = "<a target='_blank' href='%s' title='Full *%s* performance report'>:chart_with_downwards_trend:</a>";
			htmlReleaseReportAnchor = format(htmlReleaseReportAnchor, releasePerformanceReportUrl, resultToShow.getEpcVersion());

			resultLineBuilder
					.append(PIPE_WITH_SPACES)
					.append(htmlReleaseReportAnchor);
		}

		resultLineBuilder.append("</td></tr>");
		return resultLineBuilder.toString();
	}

	private String getPerformanceReportURL(final String reportBaseUrl, final ResultBean result) {
		return reportBaseUrl + result.getTestId() + "_" + result.getApplication() + "_db_statistics.json";
	}
}
