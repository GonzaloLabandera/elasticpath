/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.beans;

import static com.elasticpath.performance.mojo.beans.ResultBean.CI_PR_COMPARISON;
import static com.elasticpath.performance.mojo.utils.Constants.APPLICATION_COL;
import static com.elasticpath.performance.mojo.utils.Constants.HEAVY_PLUS_SIGN_EMOJI;
import static com.elasticpath.performance.mojo.utils.Constants.ID_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TEST_NAME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_DELETES_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_INSERTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_SELECTS_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_TIME_COL;
import static com.elasticpath.performance.mojo.utils.Constants.TOTAL_DB_UPDATES_COL;
import static com.elasticpath.performance.mojo.utils.Constants.X_EMOJI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Test class for {@link ResultBean}. */
@RunWith(MockitoJUnitRunner.class)
public class ResultBeanTest {
	private static final String TEST_NAME = "Performance Test";
	private static final String APPLICATION_NAME = "cortex";

	private static final String EPC_VERSION = "master";
	private static final String LOCAL_REPORT_BASE_URL = "http://local";
	private static final String REMOTE_REPORT_BASE_URL = "http://remote";
	private static final int MAX_ALLOWED_DEVIATION_PERCENT = 5;

	private static final String REMOTE_TEST_ID = "21";
	private static final int REMOTE_TOTAL_SELECTS = 100;
	private static final int REMOTE_TOTAL_INSERTS = 200;
	private static final int REMOTE_TOTAL_UPDATES = 300;
	private static final int REMOTE_TOTAL_DELETES = 400;
	private static final int REMOTE_TOTAL_DB_TIME = 500;

	private static final String[] COUNTER_PREFIXES = {"Selects", "Inserts", "Updates", "Deletes"};
	private static final String DB_TIME_DELTA = "DBTimeDelta";

	private static final int MAGIC_4 = 4;
	private static final int MAGIC_110 = 110;
	private static final int MAGIC_220 = 220;
	private static final int MAGIC_330 = 330;
	private static final int MAGIC_440 = 440;
	private static final int MAGIC_100 = 100;
	private static final int MAGIC_200 = 200;
	private static final int MAGIC_300 = 300;
	private static final int MAGIC_400 = 400;
	private static final int MAGIC_104 = 104;
	private static final int MAGIC_208 = 208;
	private static final int MAGIC_312 = 312;
	private static final int MAGIC_416 = 416;
	private static final int MAGIC_99 = 99;
	private static final int MAGIC_199 = 199;
	private static final int MAGIC_299 = 299;
	private static final int MAGIC_399 = 399;
	private static final int MAGIC_1104 = 1104;
	private static final int MAGIC_1208 = 1208;
	private static final int MAGIC_1312 = 1312;
	private static final int MAGIC_1416 = 1416;

	private static final String DELETES = "Deletes";
	private static final String INSERTS = "Inserts";
	private static final String SELECTS = "Selects";
	private static final String UPDATES = "Updates";
	private static final String STATUS = "Status";
	private static final String DELTA = "Delta";
	private static final String TOOLTIP = "Tooltip";
	private static final String PIPE_MASTER = " | master: ";
	private static final String PR_LABEL = "PR: ";
	@Mock private ResultSet resultSet;

	@Before
	public void init() throws SQLException {
		when(resultSet.getString(ID_COL)).thenReturn(REMOTE_TEST_ID);
		when(resultSet.getString(APPLICATION_COL)).thenReturn(REMOTE_TEST_ID);
		when(resultSet.getString(TEST_NAME_COL)).thenReturn(TEST_NAME);
		when(resultSet.getInt(TOTAL_DB_SELECTS_COL)).thenReturn(REMOTE_TOTAL_SELECTS);
		when(resultSet.getInt(TOTAL_DB_INSERTS_COL)).thenReturn(REMOTE_TOTAL_INSERTS);
		when(resultSet.getInt(TOTAL_DB_UPDATES_COL)).thenReturn(REMOTE_TOTAL_UPDATES);
		when(resultSet.getInt(TOTAL_DB_DELETES_COL)).thenReturn(REMOTE_TOTAL_DELETES);
		when(resultSet.getInt(TOTAL_DB_TIME_COL)).thenReturn(REMOTE_TOTAL_DB_TIME);
	}

	//testing CR PR workflow when local and remote results are identical (db exe time doesn't need to be)
	@Test
	public void shouldIndicateUnchangedPerformanceWhenPRAndRemoteResultsAreIdentical() throws SQLException, MojoExecutionException {
		Map<String, String> localCounterNameToValue = new HashMap<>(MAGIC_4);
		localCounterNameToValue.put(SELECTS, "100");
		localCounterNameToValue.put(INSERTS, "200");
		localCounterNameToValue.put(UPDATES, "300");
		localCounterNameToValue.put(DELETES, "400");

		Map<String, String> remoteCounterNameToValue = new HashMap<>(MAGIC_4);
		remoteCounterNameToValue.put(SELECTS, "100");
		remoteCounterNameToValue.put(INSERTS, "200");
		remoteCounterNameToValue.put(UPDATES, "300");
		remoteCounterNameToValue.put(DELETES, "400");

		String csvLine = "1," + APPLICATION_NAME + "," + TEST_NAME + ",100,200,300,400,50";
		CSVLineBean localCsvLineBean = new CSVLineBean(csvLine);

		ResultBean localResult = new ResultBean(EPC_VERSION, LOCAL_REPORT_BASE_URL, REMOTE_REPORT_BASE_URL, MAX_ALLOWED_DEVIATION_PERCENT,
				localCsvLineBean);
		ResultBean remoteResult = new ResultBean(resultSet);

		localResult.diff(CI_PR_COMPARISON, remoteResult);
		Map<String, Object> diffData = localResult.getDiffData();

		for (String counterPrefix : COUNTER_PREFIXES) {
			assertThat(diffData.get(counterPrefix + STATUS))
					.isEqualTo("");
			assertThat(diffData.get(counterPrefix + DELTA))
					.isEqualTo(0);
			assertThat(diffData.get(counterPrefix + TOOLTIP))
					.isEqualTo(PR_LABEL + localCounterNameToValue.get(counterPrefix) + PIPE_MASTER
							+ remoteCounterNameToValue.get(counterPrefix));
		}
		assertThat(diffData.get(DB_TIME_DELTA)).isNotNull();
	}

	//testing CI PR workflow and performance degradation above max allowed deviation (5%)
	@Test
	public void shouldIndicateUnacceptablePerformanceDegradationWhenPRResultsAreAboveMaxDeviation() throws SQLException, MojoExecutionException {
		Map<String, Integer> localCounterNameToValue = new HashMap<>(MAGIC_4);
		localCounterNameToValue.put(SELECTS, MAGIC_110);
		localCounterNameToValue.put(INSERTS, MAGIC_220);
		localCounterNameToValue.put(UPDATES, MAGIC_330);
		localCounterNameToValue.put(DELETES, MAGIC_440);

		Map<String, Integer> remoteCounterNameToValue = new HashMap<>(MAGIC_4);
		remoteCounterNameToValue.put(SELECTS, MAGIC_100);
		remoteCounterNameToValue.put(INSERTS, MAGIC_200);
		remoteCounterNameToValue.put(UPDATES, MAGIC_300);
		remoteCounterNameToValue.put(DELETES, MAGIC_400);

		String csvLine = "1," + APPLICATION_NAME + "," + TEST_NAME + ",110,220,330,440,50";
		CSVLineBean localCsvLineBean = new CSVLineBean(csvLine);

		ResultBean localResult = new ResultBean(EPC_VERSION, LOCAL_REPORT_BASE_URL, REMOTE_REPORT_BASE_URL, MAX_ALLOWED_DEVIATION_PERCENT,
				localCsvLineBean);
		ResultBean remoteResult = new ResultBean(resultSet);

		localResult.diff(CI_PR_COMPARISON, remoteResult);
		Map<String, Object> diffData = localResult.getDiffData();

		for (String counterPrefix : COUNTER_PREFIXES) {
			assertThat(diffData.get(counterPrefix + STATUS))
					.isEqualTo(X_EMOJI);

			int total = remoteCounterNameToValue.get(counterPrefix);
			int delta = localCounterNameToValue.get(counterPrefix) - remoteCounterNameToValue.get(counterPrefix);
			double deviation = localResult.calculateDeviation(delta, total);

			assertThat((Integer) diffData.get(counterPrefix + DELTA))
					.isEqualTo(delta);

			assertThat(diffData.get(counterPrefix + TOOLTIP))
					.isEqualTo(PR_LABEL + localCounterNameToValue.get(counterPrefix) + PIPE_MASTER
							+ remoteCounterNameToValue.get(counterPrefix) + " | +" + deviation + "%");
		}
		assertThat(diffData.get(DB_TIME_DELTA)).isNotNull();
	}

	//testing CI PR workflow and slight performance degradation but still within max allowed deviation (5%)
	@Test
	public void shouldIndicateAcceptablePerformanceDegradationWhenPRResultsAreWithinMaxDeviation() throws SQLException, MojoExecutionException {
		Map<String, Integer> localCounterNameToValue = new HashMap<>(MAGIC_4);
		localCounterNameToValue.put(SELECTS, MAGIC_104);
		localCounterNameToValue.put(INSERTS, MAGIC_208);
		localCounterNameToValue.put(UPDATES, MAGIC_312);
		localCounterNameToValue.put(DELETES, MAGIC_416);

		Map<String, Integer> remoteCounterNameToValue = new HashMap<>(MAGIC_4);
		remoteCounterNameToValue.put(SELECTS, MAGIC_100);
		remoteCounterNameToValue.put(INSERTS, MAGIC_200);
		remoteCounterNameToValue.put(UPDATES, MAGIC_300);
		remoteCounterNameToValue.put(DELETES, MAGIC_400);

		String csvLine = "1," + APPLICATION_NAME + "," + TEST_NAME + ",104,208,312,416,50";
		CSVLineBean localCsvLineBean = new CSVLineBean(csvLine);

		ResultBean localResult = new ResultBean(EPC_VERSION, LOCAL_REPORT_BASE_URL, REMOTE_REPORT_BASE_URL, MAX_ALLOWED_DEVIATION_PERCENT,
				localCsvLineBean);
		ResultBean remoteResult = new ResultBean(resultSet);

		localResult.diff(CI_PR_COMPARISON, remoteResult);
		Map<String, Object> diffData = localResult.getDiffData();

		for (String counterPrefix : COUNTER_PREFIXES) {
			assertThat(diffData.get(counterPrefix + STATUS))
					.isEqualTo(HEAVY_PLUS_SIGN_EMOJI);

			int total = remoteCounterNameToValue.get(counterPrefix);
			int delta = localCounterNameToValue.get(counterPrefix) - remoteCounterNameToValue.get(counterPrefix);
			double deviation = localResult.calculateDeviation(delta, total);

			assertThat((Integer) diffData.get(counterPrefix + DELTA))
					.isEqualTo(delta);

			assertThat(diffData.get(counterPrefix + TOOLTIP))
					.isEqualTo(PR_LABEL + localCounterNameToValue.get(counterPrefix) + PIPE_MASTER
							+ remoteCounterNameToValue.get(counterPrefix) + " | +" + deviation + "%");
		}
		assertThat(diffData.get(DB_TIME_DELTA)).isNotNull();
	}

	//testing CI PR workflow and performance improvement
	@Test
	public void shouldIndicatePerformanceImprovementWhenPRResultsAreBetterThanRemoteOnes() throws SQLException, MojoExecutionException {
		List<String> invalidEmojis = Arrays.asList("", X_EMOJI, HEAVY_PLUS_SIGN_EMOJI);

		Map<String, Integer> localCounterNameToValue = new HashMap<>(MAGIC_4);
		localCounterNameToValue.put(SELECTS, MAGIC_99);
		localCounterNameToValue.put(INSERTS, MAGIC_199);
		localCounterNameToValue.put(UPDATES, MAGIC_299);
		localCounterNameToValue.put(DELETES, MAGIC_399);

		Map<String, Integer> remoteCounterNameToValue = new HashMap<>(MAGIC_4);
		remoteCounterNameToValue.put(SELECTS, MAGIC_100);
		remoteCounterNameToValue.put(INSERTS, MAGIC_200);
		remoteCounterNameToValue.put(UPDATES, MAGIC_300);
		remoteCounterNameToValue.put(DELETES, MAGIC_400);

		String csvLine = "1," + APPLICATION_NAME + "," + TEST_NAME + ",99,199,299,399,50";
		CSVLineBean localCsvLineBean = new CSVLineBean(csvLine);

		ResultBean localResult = new ResultBean(EPC_VERSION, LOCAL_REPORT_BASE_URL, REMOTE_REPORT_BASE_URL, MAX_ALLOWED_DEVIATION_PERCENT,
				localCsvLineBean);
		ResultBean remoteResult = new ResultBean(resultSet);

		localResult.diff(CI_PR_COMPARISON, remoteResult);
		Map<String, Object> diffData = localResult.getDiffData();

		for (String counterPrefix : COUNTER_PREFIXES) {
			//when perf is improved, a random emoji is used for status;
			//this check verifies that any emojis, except those in the "invalidEmojis" list, is set
			assertThat(invalidEmojis)
					.doesNotContain((String) diffData.get(counterPrefix + STATUS));

			int remoteTotal = remoteCounterNameToValue.get(counterPrefix);
			int expectedDelta = localCounterNameToValue.get(counterPrefix) - remoteCounterNameToValue.get(counterPrefix);
			double deviation = localResult.calculateDeviation(expectedDelta, remoteTotal);

			assertThat((Integer) diffData.get(counterPrefix + DELTA))
					.isEqualTo(expectedDelta);

			assertThat(diffData.get(counterPrefix + TOOLTIP))
					.isEqualTo(PR_LABEL + localCounterNameToValue.get(counterPrefix) + PIPE_MASTER
							+ remoteCounterNameToValue.get(counterPrefix) + " | " + deviation + "%");
		}
		assertThat(diffData.get(DB_TIME_DELTA)).isNotNull();
	}

	//testing CI PR workflow and newly added tests
	@Test
	public void shouldSetQuestionMarkForNewTests() throws MojoExecutionException {
		Map<String, Integer> localCounterNameToValue = new HashMap<>(MAGIC_4);
		localCounterNameToValue.put(SELECTS, MAGIC_1104);
		localCounterNameToValue.put(INSERTS, MAGIC_1208);
		localCounterNameToValue.put(UPDATES, MAGIC_1312);
		localCounterNameToValue.put(DELETES, MAGIC_1416);


		String csvLine = "1," + APPLICATION_NAME + ",NewTest,1104,1208,1312,1416,50";
		CSVLineBean localCsvLineBean = new CSVLineBean(csvLine);

		ResultBean localResult = new ResultBean(EPC_VERSION, LOCAL_REPORT_BASE_URL, REMOTE_REPORT_BASE_URL, MAX_ALLOWED_DEVIATION_PERCENT,
				localCsvLineBean);
		ResultBean remoteResult = null; // there is no matching remote result

		localResult.diff(CI_PR_COMPARISON, remoteResult);
		Map<String, Object> diffData = localResult.getDiffData();

		for (String counterPrefix : COUNTER_PREFIXES) {
			assertThat(diffData.get(counterPrefix + STATUS))
					.isEqualTo("");

			int expectedDelta = localCounterNameToValue.get(counterPrefix);
			assertThat((Integer) diffData.get(counterPrefix + DELTA))
					.isEqualTo(expectedDelta);

			assertThat(diffData.get(counterPrefix + TOOLTIP))
					.isEqualTo(PR_LABEL + localCounterNameToValue.get(counterPrefix));
		}
		assertThat(diffData.get(DB_TIME_DELTA)).isNotNull();
	}

	/* Testing CI PR workflow and edge case when one or more remote counters are zero
	 - the deviation calculation will simply multiply the delta with 100 and always indicate degradation or call for attention
	   (e.g. if remote Inserts counter is 0 and local is 10, the status will indicate degradation, but it should be acceptable.

	   The developer must comment in such cases and discuss with the team leader.
	 */
	@Test
	public void shouldIndicatePerformanceDegradationWhenPRRemoteResultsAreZero() throws SQLException, MojoExecutionException {
		when(resultSet.getInt(TOTAL_DB_INSERTS_COL)).thenReturn(0);
		String insertsCounter = "Inserts";

		Map<String, Integer> localCounterNameToValue = new HashMap<>(1);
		localCounterNameToValue.put(insertsCounter, MAGIC_208);

		Map<String, Integer> remoteCounterNameToValue = new HashMap<>(1);
		remoteCounterNameToValue.put(insertsCounter, 0);

		String csvLine = "1," + APPLICATION_NAME + "," + TEST_NAME + ",104,208,312,416,50";
		CSVLineBean localCsvLineBean = new CSVLineBean(csvLine);

		ResultBean localResult = new ResultBean(EPC_VERSION, LOCAL_REPORT_BASE_URL, REMOTE_REPORT_BASE_URL, MAX_ALLOWED_DEVIATION_PERCENT,
				localCsvLineBean);
		ResultBean remoteResult = new ResultBean(resultSet);

		localResult.diff(CI_PR_COMPARISON, remoteResult);
		Map<String, Object> diffData = localResult.getDiffData();

		assertThat(diffData.get("InsertsStatus"))
				.isEqualTo(X_EMOJI);

		int remoteInsertsCounter = remoteCounterNameToValue.get(insertsCounter);
		int delta = localCounterNameToValue.get(insertsCounter) - remoteInsertsCounter;
		double deviation = localResult.calculateDeviation(delta, remoteInsertsCounter);

		assertThat((Integer) diffData.get("InsertsDelta"))
				.isEqualTo(delta);

		assertThat(diffData.get("InsertsTooltip"))
				.isEqualTo(PR_LABEL + localCounterNameToValue.get(insertsCounter) + PIPE_MASTER
						+ remoteCounterNameToValue.get(insertsCounter) + " | +" + deviation + "%");
		assertThat(diffData.get(DB_TIME_DELTA)).isNotNull();
	}

	//TODO refactor and add more tests for CUSTOMER and EP dev workflows
}
