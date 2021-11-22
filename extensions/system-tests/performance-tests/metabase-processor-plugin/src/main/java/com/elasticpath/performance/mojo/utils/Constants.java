/*
 * Copyright (c) Elastic Path Software Inc., 2021
 */

package com.elasticpath.performance.mojo.utils;

import java.util.Random;

/**
 * All constants required by mojos.
 */
public final class Constants {
	/** CSV header header row index. */
	public static final int CSV_HEADER_ROW_INDEX = 0;
	/** Single-quote char. */
	public static final char SINGLE_QUOTE = '\'';
	//magic numbers - required for PMD/CS
	/** .*/
	public static final int ZERO = 0;
	/** .*/
	public static final int ONE = 1;
	/** .*/
	public static final int TWO = 2;
	/** .*/
	public static final int THREE = 3;
	/** .*/
	public static final int FOUR = 4;
	/** .*/
	public static final int FIVE = 5;
	/** .*/
	public static final int SIX = 6;
	/** .*/
	public static final int SEVEN = 7;
	/** .*/
	public static final int EIGHT = 8;
	/** .*/
	public static final int NINE = 9;
	/** .*/
	public static final int TEN = 10;
	/** .*/
	public static final int ELEVEN = 11;
	/** .*/
	public static final int TWLEVE = 12;
	/** .*/
	public static final int HUNDRED = 100;
	/** Max allowed length of the test name that can be displayed in results.*/
	public static final int MAX_TEST_NAME_LEN_CHARS = 20;
	/** Pipe character wrapped with one space.*/
	public static final String PIPE_WITH_SPACES = " | ";
	/** PR label template.*/
	public static final String PR_LABEL = "%s: ";

	//DB column names
	/** .*/
	public static final String DATE_EXECUTED_COL = "date_executed";
	/** .*/
	public static final String EPC_VERSION_COL = "epc_version";
	/** .*/
	public static final String APPLICATION_COL = "application";
	/** .*/
	public static final String ID_COL = "id";
	/** .*/
	public static final String TEST_NAME_COL = "cuke_scenario";
	/** .*/
	public static final String TOTAL_DB_SELECTS_COL = "total_db_selects";
	/** .*/
	public static final String TOTAL_DB_INSERTS_COL = "total_db_inserts";
	/** .*/
	public static final String TOTAL_DB_UPDATES_COL = "total_db_updates";
	/** .*/
	public static final String TOTAL_DB_DELETES_COL = "total_db_deletes";
	/** .*/
	public static final String TOTAL_DB_TIME_COL = "total_db_time";
	/** .*/
	public static final String COMMIT_HASH_COL = "commit_hash";

	//all txt emojis must be backed-up by real images in /extensions/system-tests/performance-tests/cucumber/src/test/resources/reports/icons

	/** Emoji for failed tests. */
	public static final String X_EMOJI = ":x:";
	/** Emoji for the tests with increased number of db calls but within given deviation. */
	public static final String HEAVY_PLUS_SIGN_EMOJI = ":heavy_plus_sign:";
	/** Emoji for new tests. */
	public static final String QUESTION_EMOJI = ":question:";

	/** Motivational emojis. Randomly selected in case of perf improvements. */
	public static final String[] MOTIVATIONAL_EMOJIS =
			{":heart_eyes:", ":star2:", ":boom:", ":fire:", ":raised_hands:", ":shipit:", ":racehorse:"};

	private static final Random RANDOM = new Random();

	public static String getRandomMotivationalEmoji() {
		return MOTIVATIONAL_EMOJIS[RANDOM.nextInt(MOTIVATIONAL_EMOJIS.length)];
	}

	private Constants() {
		//noop
	}
}
