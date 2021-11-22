/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import static java.lang.String.format;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML-related methods.
 */
@SuppressWarnings({"PMD.ConsecutiveLiteralAppends", "PMD.AvoidStringBufferField"})
public final class HTMLUtils {

	/**Regex patther for matching either " :EMOJI_CODE:" or ">:EMOJI_CODE:" .*/
	public static final Pattern EMOJI_TXT_PATTERN = Pattern.compile("[\\s|>]:(.+?):");
	/** Partial report (PR comment) HTML header template. */
	public static final StringBuilder PARTIAL_HTML_REPORT_HEADER_TEMPLATE = new StringBuilder();
	/** Partial report (PR comment) HTML footer template. */
	public static final StringBuilder PARTIAL_HTML_REPORT_FOOTER_TEMPLATE = new StringBuilder();
	/** Partial report (PR comment) file name. */
	public static final String PARTIAL_REPORT_FILE_NAME = "partialPerformanceReport.html.json";
	/** Full report file name. */
	public static final String FULL_REPORT_FILE_NAME = "fullPerformanceReport.html";
	/** Full report HTML header template. */
	public static final String FULL_HTML_REPORT_HEADER =
			"<html><head><style>code {background-color: #0f0}</style></head><body style='font-family: Helvetica'>";
	/** Full report HTML footer template. */
	public static final String FULL_HTML_REPORT_FOOTER = "</table></body></html>";

	/** HTML row template used for each test.*/
	public static final StringBuilder HTML_RESULT_LINE_TEMPLATE = new StringBuilder();

	static {
		HTML_RESULT_LINE_TEMPLATE
				.append("<tr><th><a name=app>%s</a></th><td><a name=test title='%s'>%s %s</a></td>")
				.append("<td align=right><a name=selects-results title='%s'>%d %s</a></td>")
				.append("<td align=right><a name=inserts-results title='%s'>%d %s</a></td>")
				.append("<td align=right><a name=updates-results title='%s'>%d %s</a></td>")
				.append("<td align=right><a name=deletes-results title='%s'>%d %s</a></td>")
				.append("<td align=right>%d</td>")
				.append("<td align=center><a target='_blank' href='%s' title='Full%sperformance report'>:chart_with_upwards_trend:</a>");

		PARTIAL_HTML_REPORT_HEADER_TEMPLATE
				.append("<table border='1' width='70%%' align='center'>")
				.append("<tr><th colspan=8 width=1500>DB performance test results PR vs *%s* - allowed deviation is <code>%d%%</code>")
				.append("<br/><i>Tip: Mouseover test name, numbers, and icons for additional info</i></th></tr>")
				.append("<tr><th>App</th><th>Test</th><th>Selects</th><th>Inserts</th><th>Updates</th><th>Deletes</th><th>DB Time (ms)</th>")
				.append("<th>Reports</th></tr>");

		PARTIAL_HTML_REPORT_FOOTER_TEMPLATE
				.append("<tr><th colspan=8>The number of tests exceeds the limit of <code>%d</code>.")
				.append(" To see all results click <a target='_blank' href='%s")
				.append(FULL_REPORT_FILE_NAME)
				.append("' title='All PR performance results'>here</a></th></tr></table>");
	}

	private HTMLUtils() {
		//noop
	}

	/**
	 * Replace txt emojis with image counterparts.
	 *
	 * @param htmlContent the html content where txt emojis should be replaced
	 * @return the html content with emoji images
	 */
	public static String replaceGitHubEmojisWithIcons(final StringBuilder htmlContent) {
		Matcher emojiMatcher = EMOJI_TXT_PATTERN.matcher(htmlContent);
		String htmlIconReplacement = "<img src='%s.png' width=20 height=20/>";

		StringBuffer buffer = new StringBuffer();

		while (emojiMatcher.find()) {
			char fullMatchFirstChar = emojiMatcher.group(0).charAt(0);
			String emojiTxt = emojiMatcher.group(1);
			emojiMatcher.appendReplacement(buffer, fullMatchFirstChar + format(htmlIconReplacement, emojiTxt));
		}
		emojiMatcher.appendTail(buffer);
		return buffer.toString();
	}
}
