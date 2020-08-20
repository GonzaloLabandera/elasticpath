/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.performancetools.queryanalyzer.beans;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.elasticpath.performancetools.queryanalyzer.utils.Utils;

/**
 * JPA query representation.
 */
public final class JPAQuery implements Serializable {
	/**
	 * Serial version.
	 */
	public static final long serialVersionUID = 1L;

	private final String query;
	private final Collection<String> eagerRelations = new LinkedHashSet<>();
	private final List<SQLQuery> sqlQueries = new LinkedList<>();
	private final List<DbStatement> sqlUpdates = new LinkedList<>();
	private final List<DbStatement> sqlDeletes = new LinkedList<>();
	private final List<DbStatement> sqlInserts = new LinkedList<>();
	private Date startedAt;

	/**
	 * Default constructor.
	 */
	public JPAQuery() {
		this.query = "";
	}

	/**
	 * Custom constructor.
	 *
	 * @param query JPA query.
	 */
	public JPAQuery(final String query) {
		this.query = Utils.removeTabAndCRChars(query);
	}

	public String getQuery() {
		return query;
	}

	public Collection<String> getEagerRelations() {
		return eagerRelations;
	}

	public List<SQLQuery> getSqlQueries() {
		return sqlQueries;
	}

	public List<DbStatement> getSqlUpdates() {
		return sqlUpdates;
	}

	public List<DbStatement> getSqlDeletes() {
		return sqlDeletes;
	}

	public List<DbStatement> getSqlInserts() {
		return sqlInserts;
	}

	public Date getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(final Date startedAt) {
		this.startedAt = startedAt;
	}

	/**
	 * Add the statement, along with execution time, to the correct list of statements.
	 *
	 * @param statement the statement
	 * @param exeTimeMillis the statement's execution time
	 */
	@SuppressWarnings("PMD.UseLocaleWithCaseConversions")
	public void addStatement(final String statement, final long exeTimeMillis) {
		String lowerCaseStatement = statement.toLowerCase();

		if (lowerCaseStatement.startsWith("select")) {
			this.sqlQueries.add(new SQLQuery(statement, exeTimeMillis));
		} else if (lowerCaseStatement.startsWith("update")) {
			this.sqlUpdates.add(new DbStatement(statement, exeTimeMillis));
		} else if (lowerCaseStatement.startsWith("insert")) {
			this.sqlInserts.add(new DbStatement(statement, exeTimeMillis));
		} else  {
			this.sqlDeletes.add(new DbStatement(statement, exeTimeMillis));
		}
	}
	/**
	 * Add eager relations string to the collection.
	 * Only unique (because JPA log may repeat the same string N times for the same JPA query).
	 *
	 * @param eagerRelations the eager relations string
	 */
	public void addEagerRelations(final String eagerRelations) {
		this.eagerRelations.add(eagerRelations);
	}

	@Override
	public boolean equals(final Object otherJPAQuery) {
		return EqualsBuilder.reflectionEquals(this, otherJPAQuery, false);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}
}
