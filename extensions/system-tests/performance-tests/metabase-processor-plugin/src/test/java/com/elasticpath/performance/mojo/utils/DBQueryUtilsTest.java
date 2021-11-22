/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import static com.elasticpath.performance.mojo.beans.ResultBean.CI_PR_COMPARISON;
import static com.elasticpath.performance.mojo.beans.ResultBean.CUSTOMER_DEV_COMPARISON;
import static com.elasticpath.performance.mojo.beans.ResultBean.LOCAL_DEV_COMPARISON;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/** Test class for {@link DbQueryUtils}.*/
public class DBQueryUtilsTest {
	private static final String EPC_VERSION = "master";

	@Test
	public void shouldReturnFormattedLatestReleaseDataQueryForCIPRAndLocalWorkflows() {
		String expected = "SELECT id,application,cuke_scenario,total_db_selects,total_db_inserts,total_db_updates,total_db_deletes,total_db_time"
				+ " FROM \"public\".\"cucumber_performance_results\" WHERE  jenkins_job_id = "
				+ "(SELECT max(jenkins_job_id) FROM cucumber_performance_results WHERE epc_version='master') and epc_version='master'";

		String actual = DbQueryUtils.getFormattedLatestReleaseDataQuery(CI_PR_COMPARISON, EPC_VERSION);

		assertThat(actual)
				.isEqualTo(expected);

		actual = DbQueryUtils.getFormattedLatestReleaseDataQuery(LOCAL_DEV_COMPARISON, EPC_VERSION);

		assertThat(actual)
				.isEqualTo(expected);
	}

	@Test
	public void shouldReturnFormattedLatestReleaseDataQueryForCustomerDevWorkflow() {
		String expected = "SELECT id,application,cuke_scenario,total_db_selects,total_db_inserts,total_db_updates,total_db_deletes,total_db_time"
				+ " FROM \"PUBLIC\".\"cucumber_performance_results\" WHERE  epc_version='master'";

		String actual = DbQueryUtils.getFormattedLatestReleaseDataQuery(CUSTOMER_DEV_COMPARISON, EPC_VERSION);

		assertThat(actual)
				.isEqualTo(expected);
	}

	@Test
	public void shouldReturnFormattedExportQuery() {
		String expected = "SELECT date_executed,epc_version,application,cuke_scenario,total_db_selects,total_db_inserts,total_db_updates,"
				+ "total_db_deletes,total_db_time,commit_hash FROM cucumber_performance_results"
				+ " WHERE jenkins_job_id = (SELECT max(jenkins_job_id) FROM cucumber_performance_results WHERE epc_version='master')"
				+  " and epc_version='master'";

		String actual = DbQueryUtils.getFormattedExportQuery(EPC_VERSION);

		assertThat(actual)
				.isEqualTo(expected);
	}
}
