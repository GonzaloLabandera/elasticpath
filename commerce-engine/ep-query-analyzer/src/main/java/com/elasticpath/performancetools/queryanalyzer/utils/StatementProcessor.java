/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.performancetools.queryanalyzer.utils;

import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.performancetools.queryanalyzer.beans.JPAQuery;

/**
 * JPA/SQL query processor.
 */
public final class StatementProcessor {

	private static final int ONE_SECOND_MILLIS = 1000;
	private static final Logger LOG = LoggerFactory.getLogger(StatementProcessor.class);

	private StatementProcessor() {
		//util class
	}

	/**
	 * Parses SQL query lines.
	 *
	 * @param lastProcessedLine     First line with SQL query.
	 * @param jpaQuery JPA query parent.
	 * @param statementBuffer multi-line string buffer
	 */
	public static void processSQLStatementLine(final String lastProcessedLine, final JPAQuery jpaQuery, final StringBuilder statementBuffer)  {

		Matcher matcher = getSQLPatternMatcher(lastProcessedLine, statementBuffer);

		if (matcher.find()) {

			final String sqlStatement = reformatSQLStatement(matcher.group(2));

			long sqlStatementExeTimeMs = getSQLStatementExeTimeMillis(lastProcessedLine);

			//JPA query always precedes SQL one and it's never null
			jpaQuery.addStatement(sqlStatement, sqlStatementExeTimeMs);
		}
	}

	private static long getSQLStatementExeTimeMillis(final String processedLine) {
		if (processedLine == null) {
			return 0L;
		}

		final Matcher matcher = Patterns.SQL_STATEMENT_EXE_TIME_PATTERN.matcher(processedLine);

		if (!matcher.find()) {
			return 0L;
		}

		final String sqlStatementExeTimeStr = matcher.group(1);
		final String[] exeTimeTokens = sqlStatementExeTimeStr.split(" ");

		if ("ms".equalsIgnoreCase(exeTimeTokens[1])) {
			return Long.parseLong(exeTimeTokens[0]);
		}
		//to simplify things, it is assumed that the time unit is in seconds (everything else would be horrible)

		return Long.parseLong(exeTimeTokens[0]) * ONE_SECOND_MILLIS;
	}

	private static Matcher getSQLPatternMatcher(final String processedLine, final StringBuilder statementBuffer) {
		if (processedLine == null) {
			return Patterns.SQL_PATTERN_DATE_OPTIONAL.matcher(statementBuffer.toString());
		}

		return Patterns.SQL_PATTERN.matcher(statementBuffer.toString());
	}

	/**
	 * Removes CRLF and TAB chars from SQL query and embeds parameters into
	 * query.
	 *
	 * @param sqlQuery SQL query to be reformatted.
	 * @return reformatted SQL query.
	 */
	public static String reformatSQLStatement(final String sqlQuery) {
		final String[] statementParts = sqlQuery.split("\n");
		if (statementParts.length == 1) {
			return sqlQuery;
		}

		final StringBuilder buffer = new StringBuilder();

		String params = "";
		for (String statementPart : statementParts) {
			if (statementPart.contains("params")) {
				//remove []
				params = statementPart.replaceAll("[\\[\\]]", "").trim();
			} else {
				buffer.append(statementPart.trim()).append(' ');
			}
		}

		/*
			params=(String) 80E0B57C-EEF0-646D-105E-056DD9C1D25D, (String) MOBEE

			 first split gives
			 params
			 (String) 80E0B57C-EEF0-646D-105E-056DD9C1D25D, (String) MOBEE

			 second split on second token gives
			 (String) 80E0B57C-EEF0-646D-105E-056DD9C1D25D
			 (String) MOBEE

		 */
		if (StringUtils.isBlank(params)) {
			return buffer.toString();
		}

		try {
			processParameters(params, buffer);
		} catch (Exception e) {
			LOG.error("Error occurred while processing statement: {}", sqlQuery);
			throw new IllegalStateException(e);
		}

		return buffer.toString();
	}

	/*
		Given a db statement with positional parameters, like
		SELECT * FROM TABLE where FIELD = ?1
		and extrapolated param value, like params=(String) 80E0B57C-EEF0-646D-105E-056DD9C1D25D

		the method will replace ?1 with 'PARAM_VALUE1, like
		SELECT * FROM TABLE where FIELD = '80E0B57C-EEF0-646D-105E-056DD9C1D25D'
	 */
	private static void processParameters(final String params, final StringBuilder buffer) {
		String[] paramsTokens = params.split("=")[1].split(", \\(");

		for (String paramToken : paramsTokens) {
			String[] tokens = paramToken.split(" "); //Param_Type - Param_Value
			//(String)
			// MOBEE
			paramToken = tokens.length == 2 ? tokens[1] : ""; //actual param value
			if (tokens[0].contains("String")) {
				paramToken = "\'" + paramToken + "\'";
			}

			int questionMarkPos = buffer.indexOf("?");
			buffer.replace(questionMarkPos, questionMarkPos + 1, paramToken);
		}
	}
}
