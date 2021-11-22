/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.beans;

import static com.elasticpath.performance.mojo.utils.Constants.APPLICATION_COL;
import static com.elasticpath.performance.mojo.utils.Constants.HEAVY_PLUS_SIGN_EMOJI;
import static com.elasticpath.performance.mojo.utils.Constants.HUNDRED;
import static com.elasticpath.performance.mojo.utils.Constants.ID_COL;
import static com.elasticpath.performance.mojo.utils.Constants.PIPE_WITH_SPACES;
import static com.elasticpath.performance.mojo.utils.Constants.PR_LABEL;
import static com.elasticpath.performance.mojo.utils.Constants.TEST_NAME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_DELETES_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_INSERTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_SELECTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_TIME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_UPDATES_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TWLEVE;
import static com.elasticpath.performance.mojo.utils.Constants.X_EMOJI;
import static com.elasticpath.performance.mojo.utils.Constants.getRandomMotivationalEmoji;
import static java.lang.String.format;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * The bean class representing a CSV line or a db record and containing all relevant performance indicators.
 */
public class ResultBean implements Comparable<ResultBean> {
	/** CI PR comparison type.*/
	public static final int CI_PR_COMPARISON = 0;
	/** Local developer comparison type.*/
	public static final int LOCAL_DEV_COMPARISON = 1;
	/** Customer developer comparison type.*/
	public static final int CUSTOMER_DEV_COMPARISON = 2;

	private final String testId;
	private final String application;
	private final String testName;

	private final int totalDbSelects;
	private final int totalDbUpdates;
	private final int totalDbInserts;
	private final int totalDbDeletes;
	private final int totalDbTimeMs;

	private String epcVersion;
	private String localReportBaseUrl;
	private String remoteReportBaseUrl;
	private int maxAllowedDeviation;
	private boolean isFailure;

	private Map<String, Object> diffData;
	private ResultBean releaseResult;
	private int comparisonType;

	/**
	 * Custom constructor.
	 *
	 * @param epcVersion the epc version
	 * @param localReportBaseUrl the local report base url
	 * @param remoteReportBaseUrl the remote report base url
	 * @param maxAllowedDeviation the max allowed deviation
	 * @param csvLineBean csv line wrapper bean {@link CSVLineBean}
	 */
	public ResultBean(final String epcVersion, final String localReportBaseUrl, final String remoteReportBaseUrl,
					  final int maxAllowedDeviation, final CSVLineBean csvLineBean) {

		this.diffData = new HashMap<>(TWLEVE);

		this.testId = csvLineBean.getTestId();
		this.application = csvLineBean.getApplication();
		this.testName = csvLineBean.getTestName();
		this.totalDbSelects = csvLineBean.getTotalDbSelects();
		this.totalDbInserts = csvLineBean.getTotalDbInserts();
		this.totalDbUpdates = csvLineBean.getTotalDbUpdates();
		this.totalDbDeletes = csvLineBean.getTotalDbDeletes();
		this.totalDbTimeMs = csvLineBean.getTotalDbTime();

		this.epcVersion = epcVersion;
		this.localReportBaseUrl = localReportBaseUrl;
		this.remoteReportBaseUrl = remoteReportBaseUrl;
		this.maxAllowedDeviation = maxAllowedDeviation;
	}

	/**
	 * The constructor that creates an instance using a db row.
	 *
	 * @param dbRow a db row
	 * @throws SQLException the SQL exception
	 */
	public ResultBean(final ResultSet dbRow) throws SQLException {
		this.testId = dbRow.getString(ID_COL);
		this.application = dbRow.getString(APPLICATION_COL);
		this.testName = dbRow.getString(TEST_NAME_COL);
		this.totalDbSelects = dbRow.getInt(TOTAL_DB_SELECTS_COL);
		this.totalDbInserts = dbRow.getInt(TOTAL_DB_INSERTS_COL);
		this.totalDbUpdates = dbRow.getInt(TOTAL_DB_UPDATES_COL);
		this.totalDbDeletes = dbRow.getInt(TOTAL_DB_DELETES_COL);
		this.totalDbTimeMs = dbRow.getInt(TOTAL_DB_TIME_COL);
	}

	public String getTestId() {
		return testId;
	}

	public String getTestName() {
		return testName;
	}

	public String getApplication() {
		return application;
	}

	public ResultBean getReleaseResult() {
		return releaseResult;
	}

	public Map<String, Object> getDiffData() {
		return diffData;
	}

	public int getComparisonType() {
		return comparisonType;
	}

	public String getRemoteReportBaseUrl() {
		return remoteReportBaseUrl;
	}

	public String getLocalReportBaseUrl() {
		return localReportBaseUrl;
	}

	public String getEpcVersion() {
		return epcVersion;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	/**
	 * Compares local and remote results and formats the differences per counter.
	 *
	 * @param releaseResult release result.
	 * @param comparisonType comparison type
	 * @throws MojoExecutionException the exception
	 */
	public void diff(final int comparisonType, final ResultBean releaseResult) throws MojoExecutionException {
		this.releaseResult = releaseResult;
		this.comparisonType = comparisonType;

		formatCounter(totalDbSelects, "Selects");
		formatCounter(totalDbInserts, "Inserts");
		formatCounter(totalDbUpdates, "Updates");
		formatCounter(totalDbDeletes, "Deletes");

		int dbTimeDelta = totalDbTimeMs;
		if (releaseResult != null) {
			dbTimeDelta -= releaseResult.totalDbTimeMs;
		}
		diffData.put("DBTimeDelta", dbTimeDelta);
	}

	/**
	 * The results will be sorted by failures first and then by application name.
	 *
	 * @param otherResult the other result
	 * @return comparison result
	 */
	@Override
	public int compareTo(final ResultBean otherResult) {
		return new CompareToBuilder()
				.append(otherResult.isFailure, this.isFailure)
				.append(this.application, otherResult.getApplication())
				.build();
	}

	/**
	 * The combination of application and test names used as a map key.
	 *
	 * @return key
	 */
	public String mapKey() {
		return application + "_" + testName;
	}

	/**
	 * Get local report type, used in tooltips, based on comparison type.
	 *
	 * @return "PR" for CI PR workflow, "local" for customer/EP developer workflow
	 */
	public String getLocalReportType() {
		return this.comparisonType == CI_PR_COMPARISON
				? "PR"
				: "local";
	}

	private String getRemoteReportType() {
		return this.comparisonType == CUSTOMER_DEV_COMPARISON
				? "OOTB"
				: epcVersion;
	}

	private void formatCounter(final int counterTotal, final String counterPrefix) throws MojoExecutionException {
		String tooltip = PR_LABEL + counterTotal;
		int calculatedDelta = counterTotal;
		String status = "";
		String deviationTooltip = "";

		if (releaseResult != null) {
			int releaseCounterTotal = getReleaseCounterTotal(counterPrefix);
			calculatedDelta = counterTotal - releaseCounterTotal;

			if (calculatedDelta > 0) { //performance degradation
				double deviation = calculateDeviation(calculatedDelta, releaseCounterTotal);
				status = setDeviationEmojiStatus(deviation);
				deviationTooltip = PIPE_WITH_SPACES + "+" + deviation + "%%";

			} else if (calculatedDelta < 0) { //performance improvement
				status = getRandomMotivationalEmoji();
				double delta = calculateDeviation(calculatedDelta, releaseCounterTotal);
				deviationTooltip = PIPE_WITH_SPACES + delta + "%%";
			}

			tooltip += PIPE_WITH_SPACES + PR_LABEL + releaseCounterTotal;
			tooltip = format(tooltip, getLocalReportType(), getRemoteReportType()) + deviationTooltip;
		}

		diffData.put(counterPrefix + "Tooltip", format(tooltip, getLocalReportType()));
		diffData.put(counterPrefix + "Delta", calculatedDelta);
		diffData.put(counterPrefix + "Status", status);
	}

	/**
	 * Visible for testing.
	 * @param delta the delta
	 * @param releaseTotal the release total
	 * @return the deviation
	 */
	protected double calculateDeviation(final int delta, final int releaseTotal) {
		if (releaseTotal == 0) {
			//can't calculate % if the total is 0; instead, returning delta multiplied by 100
			// although the number will not make any sense, it will result in showing correct counter status
			return delta * HUNDRED;
		}

		double deviationInPercents = (double) delta / releaseTotal * HUNDRED;
		return new BigDecimal(String.valueOf(deviationInPercents)).setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
	}

	private String setDeviationEmojiStatus(final double deviation) {
		if (deviation > maxAllowedDeviation) {
			isFailure = true;
			return X_EMOJI;
		}
		return HEAVY_PLUS_SIGN_EMOJI;
	}

	private int getReleaseCounterTotal(final String counterPrefix) throws MojoExecutionException {
		try {
			Field field = getClass().getDeclaredField("totalDb" + counterPrefix);

			return (Integer) field.get(this.releaseResult);
		} catch (Exception ex) {
			throw new MojoExecutionException(ex.getMessage(), ex);
		}
	}
}
