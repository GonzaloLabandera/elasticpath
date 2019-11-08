/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.openjpa.util;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import com.google.common.annotations.VisibleForTesting;

import org.apache.openjpa.meta.MetaDataRepository;
import org.apache.openjpa.persistence.JPAFacadeHelper;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;

/**
 * Basically, a data holder used for easier and faster processing during query routing.
 * The data structures are populated only if horizontal db scaling (HDS) feature is enabled.
 */
public class QueryRouterMetaInfoHolder {

	private static final Logger LOG = LoggerFactory.getLogger(QueryRouterMetaInfoHolder.class);

	private HDSSupportBean hdsSupportBean;
	private JPAAnnotationParser jpaAnnotationParser;
	private NamedQueryParser namedQueryParser;

	/**
	 * The init method, called from {@link com.elasticpath.persistence.openjpa.routing.QueryRouter}, used to initialize required data structures.
	 *
	 * @param readWriteEntityManager the read-write entity manager.
	 */
	//called only once, at application startup, from QueryRouter
	public void initFromRWEntityManager(final EntityManager readWriteEntityManager) {
		MetaDataRepository metaDataRepository = getMetaDataRepositoryInstance(readWriteEntityManager);

		jpaAnnotationParser.parse(metaDataRepository.getMetaDatas());

		namedQueryParser.parse(metaDataRepository.getQueryMetaDatas());
	}

	/**
	 * Check if given query name or dynamic query string is safe for reading from db replica.
	 *
	 * @param queryName the query name or dynamic query string to check.
	 *
	 * @return true, if query is safe for replica.
	 */
	public boolean isQuerySafeForReadingFromReplica(final String queryName) {

		LOG.debug("Checking if query {} is safe to read from replica", queryName);

		Set<String> queriedEntities = getQueriedEntitiesByQueryName(queryName);
		Set<String> modifiedEntities = hdsSupportBean.getModifiedEntities();

		boolean isSafe = isQuerySafe(modifiedEntities, queriedEntities);

		if (isSafe && !modifiedEntities.isEmpty()) {
			for (String queriedEntity : queriedEntities) {
				if (isReferencedEntityModified(queriedEntity, modifiedEntities)) {
					LOG.debug("Query {} has an entity {} for which referenced entity is modified {}", queryName, queriedEntity, modifiedEntities);
					isSafe = false;
					break;
				}
			}
		}

		hdsSupportBean.setQueryIsSafeForReplica(isSafe);

		LOG.debug("Is query {} safe to read from replica? {}", queryName, isSafe);

		return isSafe;
	}

	/*
		In order to verify whether a query is safe to execute on replica, the list of modified entities must not contain
		any queried entity (those entities are extracted from a JPQL query - see NamedQueryParser).

		The check works by using a copy of the list of modified entities and removing the list of queried entities.
		The "containsAll" method can't be used because it tries to match all collection elements.
		Instead "removeAll" is used and if collection is modified (i.e. at least one element removed) it's a sign that one or more queried entities
		are modified.
	 */
	private boolean isQuerySafe(final Set<String> modifiedEntities, final Set<String> queriedEntities) {
		return !new HashSet<>(modifiedEntities).removeAll(queriedEntities);
	}

	/**
	 * Get the set of queried JPA entities (those in FROM clause).
	 *
	 * @param queryName In case of named queries this parameter will contain query name. For dynamic queries, it will be the query itself.
	 *                  If the set is not found (usually in case of dynamic queries), the query will be parsed and stored in the map.
	 * @return The set of queried entities
	 */
	public Set<String> getQueriedEntitiesByQueryName(final String queryName) {
		return namedQueryParser.getQueriedEntitiesByQueryName(queryName);
	}

	private boolean isReferencedEntityModified(final String queriedEntity, final Set<String> modifiedEntities) {

		for (String modifiedEntity : modifiedEntities) {
			if (jpaAnnotationParser.isQueriedEntityCoupledToModifiedEntity(queriedEntity, modifiedEntity)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check if query is retriable.
	 *
	 * @param queryName the query name or dynamic query string
	 * @return true, if query is retriable.
	 */
	public boolean isQueryRetriable(final String queryName) {
		return namedQueryParser.isQueryRetriable(queryName);
	}

	public void setHdsSupportBean(final HDSSupportBean hdsSupportBean) {
		this.hdsSupportBean = hdsSupportBean;
	}

	public void setJpaAnnotationParser(final JPAAnnotationParser jpaAnnotationParser) {
		this.jpaAnnotationParser = jpaAnnotationParser;
	}

	public void setNamedQueryParser(final NamedQueryParser namedQueryParser) {
		this.namedQueryParser = namedQueryParser;
	}

	/**
	 * Visible for testing.
	 * @param readWriteEntityManager read-write entity manager.
	 * @return {@link MetaDataRepository}
	 */
	@VisibleForTesting
	MetaDataRepository getMetaDataRepositoryInstance(final EntityManager readWriteEntityManager) {
		return JPAFacadeHelper.toBroker(OpenJPAPersistence.cast(readWriteEntityManager))
			.getConfiguration()
			.getMetaDataRepositoryInstance();
	}
}