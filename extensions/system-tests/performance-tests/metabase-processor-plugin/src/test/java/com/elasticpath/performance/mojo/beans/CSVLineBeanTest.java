/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/** Test class for {@link CSVLineBean}. */
public class CSVLineBeanTest {

	private static final int MAGIC_10 = 10;
	private static final int MAGIC_20 = 20;
	private static final int MAGIC_30 = 30;
	private static final int MAGIC_100 = 100;
	private static final int MAGIC_143 = 143;

	@Test
	public void shouldInitializeBeanFromCSVLine() {
		String csvLine = "1,cortex,Performance Test,100,10,20,30,143";
		CSVLineBean csvLineBean = new CSVLineBean(csvLine);

		assertThat(csvLineBean.getTestId())
				.isEqualTo("1");
		assertThat(csvLineBean.getTestName())
				.isEqualTo("Performance Test");
		assertThat(csvLineBean.getApplication())
				.isEqualTo("cortex");
		assertThat(csvLineBean.getTotalDbSelects())
				.isEqualTo(MAGIC_100);
		assertThat(csvLineBean.getTotalDbInserts())
				.isEqualTo(MAGIC_10);
		assertThat(csvLineBean.getTotalDbUpdates())
				.isEqualTo(MAGIC_20);
		assertThat(csvLineBean.getTotalDbDeletes())
				.isEqualTo(MAGIC_30);
		assertThat(csvLineBean.getTotalDbTime())
				.isEqualTo(MAGIC_143);
	}
}
