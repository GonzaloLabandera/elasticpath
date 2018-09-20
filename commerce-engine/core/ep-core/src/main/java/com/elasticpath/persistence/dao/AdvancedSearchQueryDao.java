/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.persistence.dao;

import java.util.List;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.domain.advancedsearch.AdvancedQueryType;
import com.elasticpath.domain.advancedsearch.AdvancedSearchQuery;
import com.elasticpath.domain.cmuser.CmUser;

/**
 * Interface for advanced search query DAO operations.
 */
public interface AdvancedSearchQueryDao {

	/**
	 * Finds queries with the given name.
	 *
	 * @param queryName the query name
	 * @param withDetails is indicator which define what fetch plan must be used.
	 * @return list of queries matching the given name
	 */
	List<AdvancedSearchQuery> findByName(String queryName, boolean withDetails);

	/**
	 * Save or update the given query.
	 *
	 * @param searchQuery the query to save or update
	 * @return the merged object if it is merged, or the persisted object for save action
	 * @throws EpServiceException - in case of any errors
	 */
	AdvancedSearchQuery saveOrUpdate(AdvancedSearchQuery searchQuery) throws EpServiceException;

	/**
	 * Get the product with the given UIDPK. Return a new object if no matching record exists.
	 *
	 * @param queryUidPk the Query UIDPK.
	 * @return the advanced search query if UIDPK exists, otherwise a new (empty) Query
	 * @throws EpServiceException - in case of any errors
	 */
	AdvancedSearchQuery get(long queryUidPk);

	/**
	 * Finds all visible queries (all public queries and all owner`s queries) with given query types.
	 *
	 * @param owner the query owner
	 * @param queryTypes the query types
	 * @param withDetails is indicator which define what fetch plan must be used.
	 * @return list of obtained queries
	 */
	List<AdvancedSearchQuery> findAllVisibleQueriesWithTypes(CmUser owner, List<AdvancedQueryType> queryTypes, boolean withDetails);

	/**
	 * Finds all queries with given types.
	 *
	 * @param withDetails is indicator which define what fetch plan must be used
	 * @param queryTypes the query types
	 * @return list of obtained queries
	 */
	List<AdvancedSearchQuery> findAllQueriesWithTypes(List<AdvancedQueryType> queryTypes, boolean withDetails);

	/**
	 * Removes the given query form database.
	 *
	 * @param query the query to remove
	 */
	void remove(AdvancedSearchQuery query);

}