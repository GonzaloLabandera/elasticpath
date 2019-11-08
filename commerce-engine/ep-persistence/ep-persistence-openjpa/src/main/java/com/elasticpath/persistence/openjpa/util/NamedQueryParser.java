/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;

import com.google.common.annotations.VisibleForTesting;

import org.apache.openjpa.kernel.jpql.JPQLParser;
import org.apache.openjpa.meta.ClassMetaData;
import org.apache.openjpa.meta.QueryMetaData;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class parses all named and dynamic queries (OOTB and extensions) and caches a query with a list of queried
 * (those in JPQL FROM clause) entity names.
 *
 * The {@link com.elasticpath.persistence.openjpa.routing.QueryRouter} relies on this cache for making a reliable decision when choosing
 * between read-write and read-only entity managers.
 *
 */
public class NamedQueryParser {
	private static final Logger LOG = LoggerFactory.getLogger(NamedQueryParser.class);

	/*
		This is the best effort to identify queries that MUST return 1 or more records
		Such queries contain the following operators: =, IN, >=, <=, IS.
		Also, calling PersistentEngine.load (i.e. entityManager.find) must return an entity instance

		If unsafe entity is queried and query returns 0 records where at least 1 is expected,
		then the query will be executed against the master db.

		How pattern works:
		(?si) - (i) case insensitive; (s) treat multiple lines as a single line
		Check if query STARTS with either "load" or "select" keyword -  ^load or ^select

		When PersistentEngine.load is called, a dynamic query is created e.g. "Load From CustomerImpl".
		To cover all cases for SELECT queries, "(=| is | in )" is required

	 */
	private final Pattern retriableQueryPattern = Pattern.compile("(?si)(^select.*(=| is | in ))");

	private final Map<String, Set<String>> queryNameToQueriedEntitiesMap = new TreeMap<>();
	private final Set<String> nonRetriableQueries = new HashSet<>();

	private EntityManager readWriteEntityManager;

	/**
	 * Parse named queries and store the outcome in 2 collections.
	 *
	 * @param queryMetaDatas the array of {@link QueryMetaData}.
	 */
	@SuppressWarnings("PMD.UseLocaleWithCaseConversions")
	public void parse(final QueryMetaData[] queryMetaDatas) {

		int totalNumOfNamedQueries = queryMetaDatas.length;
		LOG.info("Found {} named queries", totalNumOfNamedQueries);

		long start = System.currentTimeMillis();
		int numOfSelects = 0;

		for (QueryMetaData queryMetaData : queryMetaDatas) {

			String queryName = queryMetaData.getName();
			String queryString = queryMetaData.getQueryString();

			if (queryString.toLowerCase().startsWith("select")) {
				processQuery(queryName, queryString);
				numOfSelects++;
			}
		}
		LOG.info("Parsing of named queries completed in {} ms. Processed {} SELECT queries out of total {} named queries ",
			(System.currentTimeMillis() - start), numOfSelects, totalNumOfNamedQueries);
		LOG.info("Total number of retriable queries is {}", queryNameToQueriedEntitiesMap.size());
	}

	private Set<String> processQuery(final String queryName, final String queryString) {
		Set<String> queriedEntities = new HashSet<>();

		//check if query can be retried
		if (queryMatchesRetriablePattern(queryString)) {

			try {
				long start = System.currentTimeMillis();
				//it's fast
				ClassMetaData[] queryClassMetaDatas = getQueryClassMetaDatas(queryString);

				LOG.debug("==== Getting class datas time {}", (System.currentTimeMillis() - start));
				for (ClassMetaData queryClassMetaData : queryClassMetaDatas) {
					queriedEntities.add(queryClassMetaData.getDescribedType().getSimpleName());
				}

				queryNameToQueriedEntitiesMap.put(queryName, queriedEntities);

			} catch (Exception e) {
				LOG.error("Error while processing query {}", queryName, e);
			}

		} else if (!nonRetriableQueries.contains(queryName)) {
			nonRetriableQueries.add(queryName);
		}

		return queriedEntities;
	}

	/**
	 * Parse given query string and return query class meta data instances.
	 * Visible for testing purposes.
	 *
	 * @param queryString the query string.
	 * @return an array of {@link ClassMetaData} instances
	 */
	@VisibleForTesting
	ClassMetaData[] getQueryClassMetaDatas(final String queryString) {
		return JPAFacadeHelper.toBroker(OpenJPAPersistence.cast(readWriteEntityManager))
			.newQuery(JPQLParser.LANG_JPQL, queryString)
			.getAccessPathMetaDatas();
	}

	private boolean queryMatchesRetriablePattern(final String queryString) {
		return retriableQueryPattern.matcher(queryString).find();
	}

	/**
	 * Get queried entities for a given query name.
	 *
	 * @param queryName the query name.
	 * @return a set with queried (part of JPQL FROM clause) entity names.
	 */
	public Set<String> getQueriedEntitiesByQueryName(final String queryName) {

		Set<String> queriedEntities = queryNameToQueriedEntitiesMap.get(queryName);

		//can be null only in case of dynamic queries, not previously processed
		if (queriedEntities == null)  {
			//this is a dynamic query and because there is no query name, the actual query will be used as a map key to store queried entities
			queriedEntities = processQuery(queryName, queryName);
		}

		return queriedEntities;

	}

	/**
	 * Check if query is retriable.
	 *
 	 * @param queryName the query name or dynamic query to check.
	 * @return true, if query is retriable.
	 */
	public boolean isQueryRetriable(final String queryName) {
		return queryNameToQueriedEntitiesMap.containsKey(queryName);
	}

	public void setReadWriteEntityManager(final EntityManager readWriteEntityManager) {
		this.readWriteEntityManager = readWriteEntityManager;
	}
}
