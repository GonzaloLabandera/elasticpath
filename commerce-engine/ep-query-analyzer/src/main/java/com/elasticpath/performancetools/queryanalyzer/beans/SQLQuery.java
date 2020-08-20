/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer.beans;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * SQL query representation.
 */
public final class SQLQuery extends DbStatement {
	/**
	 * Serial version.
	 */
	public static final long serialVersionUID = 1L;

	private static final String JOIN_KEYWORD = "JOIN";

	private final int numberOfJoins;

	/**
	 * Default constructor.
	 */
	public SQLQuery() {
		super();
		this.numberOfJoins = 0;
	}

	/**
	 * Custom constructor.
	 *
	 * @param query     SQL query.
	 * @param exeTimeMs execution time in ms.
	 */
	public SQLQuery(final String query, final long exeTimeMs) {
		super(query, exeTimeMs);
		this.numberOfJoins = getNumberOfJoins(query);
	}

	private int getNumberOfJoins(final String query) {
		return StringUtils.countMatches(query, JOIN_KEYWORD);
	}

	public int getNumberOfJoins() {
		return numberOfJoins;
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
