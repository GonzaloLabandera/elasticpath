/*
 * Copyright (c) Elastic Path Software Inc., 2020
 */
package com.elasticpath.performancetools.queryanalyzer.beans;

import java.io.Serializable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.elasticpath.performancetools.queryanalyzer.utils.Utils;

/**
 * DB statement representation.
 */
public class DbStatement implements Serializable {
	/**
	 * Serial version.
	 */
	public static final long serialVersionUID = 1L;

	private final String statement;
	private long exeTimeMs;

	/**
	 * Default constructor.
	 */
	public DbStatement() {
		this.statement = "";
	}

	/**
	 * Custom constructor.
	 *
	 * @param statement     SQL query.
	 * @param exeTimeMs execution time in ms.
	 */
	public DbStatement(final String statement, final long exeTimeMs) {
		this.statement = Utils.removeTabAndCRChars(statement);
		this.exeTimeMs = exeTimeMs;
	}

	public String getStatement() {
		return statement;
	}

	public long getExeTimeMs() {
		return exeTimeMs;
	}

	@Override
	public boolean equals(final Object otherSQLQuery) {
		return EqualsBuilder.reflectionEquals(this, otherSQLQuery, false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
}
