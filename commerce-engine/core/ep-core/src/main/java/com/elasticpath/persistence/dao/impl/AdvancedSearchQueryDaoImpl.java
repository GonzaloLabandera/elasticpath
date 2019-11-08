/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.persistence.dao.impl;

import static com.elasticpath.persistence.support.FetchFieldConstants.DESCRIPTION;
import static com.elasticpath.persistence.support.FetchFieldConstants.NAME;
import static com.elasticpath.persistence.support.FetchFieldConstants.OWNER;
import static com.elasticpath.persistence.support.FetchFieldConstants.QUERY_CONTENT;
import static com.elasticpath.persistence.support.FetchFieldConstants.QUERY_TYPE;
import static com.elasticpath.persistence.support.FetchFieldConstants.QUERY_VISIBILITY;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.advancedsearch.AdvancedQueryType;
import com.elasticpath.domain.advancedsearch.AdvancedSearchQuery;
import com.elasticpath.domain.advancedsearch.QueryVisibility;
import com.elasticpath.domain.cmuser.CmUser;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.dao.AdvancedSearchQueryDao;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;

/**
 * Provides <code>AdvancedSearchQuery</code> data access methods.
 */
public class AdvancedSearchQueryDaoImpl implements AdvancedSearchQueryDao {

	private final PersistentBeanFinder persistentBeanFinder = new PersistentBeanFinder();
	private PersistenceEngine persistenceEngine;
	private FetchPlanHelper fetchPlanHelper;
	private BeanFactory beanFactory;

	/**
	 * Save or update the given query.
	 * 
	 * @param searchQuery the query to save or update
	 * @return the merged object if it is merged, or the persisted object for save action
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public AdvancedSearchQuery saveOrUpdate(final AdvancedSearchQuery searchQuery) throws EpServiceException {
		sanityCheck();
		return getPersistenceEngine().saveOrMerge(searchQuery);
	}

	/**
	 * Get the query with the given UIDPK. Return null if no matching records exist.
	 * 
	 * @param queryUidPk the Query UIDPK.
	 * @return the advanced search query if UIDPK exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public AdvancedSearchQuery get(final long queryUidPk) {
		sanityCheck();

		if (queryUidPk <= 0) {
			return getBean(ContextIdNames.ADVANCED_SEARCH_QUERY);
		}

		return getPersistentBeanFinder()
			.withCollectionOfLazyFields(getLazyFields(true))
			.get(ContextIdNames.ADVANCED_SEARCH_QUERY, queryUidPk);
	}

	/**
	 * Finds all visible queries (all public queries and all owner`s queries) with given query types.
	 * 
	 * @param owner the query owner
	 * @param queryTypes the query types
	 * @param withDetails if true, additional search query fields will be loaded
	 * @return list of obtained queries
	 */
	@Override
	public List<AdvancedSearchQuery> findAllVisibleQueriesWithTypes(final CmUser owner, final List<AdvancedQueryType> queryTypes,
																	final boolean withDetails) {
		sanityCheck();

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("uidPk", owner.getUidPk());
		parameters.put("queryVisibility", QueryVisibility.PUBLIC);
		parameters.put("listQueryType", queryTypes);

		return getPersistenceEngine()
			.withCollectionOfLazyFields(getLazyFields(withDetails))
			.retrieveByNamedQuery("ADVANCED_QUERY_FIND_VISIBLE_WITH_TYPES", parameters);
	}

	/**
	 * Finds all queries with given query types.
	 * 
	 * @param queryTypes the query types
	 * @param withDetails if true, additional search query fields will be loaded
	 * @return list of obtained queries
	 */
	@Override
	public List<AdvancedSearchQuery> findAllQueriesWithTypes(final List<AdvancedQueryType> queryTypes, final boolean withDetails) {
		sanityCheck();

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("list", queryTypes);

		return getPersistenceEngine()
			.withCollectionOfLazyFields(getLazyFields(withDetails))
			.retrieveByNamedQuery("ADVANCED_QUERY_FIND_ALL_WITH_TYPES", parameters);
	}
	
	/**
	 * Finds queries with the given name.
	 * 
	 * @param queryName the query name
	 * @param withDetails if true, additional search query fields will be loaded
	 * @return list of queries matching the given name
	 */
	@Override
	public List<AdvancedSearchQuery> findByName(final String queryName, final boolean withDetails) {
		sanityCheck();

		Map<String, Object> parameters = new HashMap<>();
		parameters.put("name", queryName.toLowerCase(Locale.getDefault()));

		return getPersistenceEngine()
			.withCollectionOfLazyFields(getLazyFields(withDetails))
			.retrieveByNamedQuery("ADVANCED_QUERY_FIND_BY_NAME", parameters);
	}

	/**
	 * Get a map with lazy fields. By default, the following fields will be loaded:
	 *
	 * <strong>name</strong>, <strong>description</strong>, <strong>queryVisibility</strong>, <strong>queryType</strong>.
	 *
	 * If <strong>withDetails</strong> is true, then <strong>queryContent</strong> and <strong>owner</strong> fields will be loaded too.
	 *
	 * @param withDetails if true, additional fields will be loaded.
	 *
	 * @return a map with lazy fields.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected Map<Class<?>, Collection<String>> getLazyFields(final boolean withDetails) {

		List<String> fields = Lists.newArrayList(NAME, DESCRIPTION, QUERY_VISIBILITY, QUERY_TYPE);

		if (withDetails) {
			fields.add(QUERY_CONTENT);
			fields.add(OWNER);
		}

		Map<Class<?>, Collection<String>> mapLazyFields = new HashMap<>();
		mapLazyFields.put(beanFactory.getBeanImplClass(ContextIdNames.ADVANCED_SEARCH_QUERY), fields);

		return mapLazyFields;
	}

	/**
	 * Removes the given query form database.
	 * 
	 * @param query the query to remove
	 */
	@Override
	public void remove(final AdvancedSearchQuery query) {
		sanityCheck();
		getPersistenceEngine().delete(query);
	}

	/**
	 * Convenience method for getting a bean instance.
	 * 
	 * @param <T> the type of bean to return
	 * @param beanName the name of the bean to get and instance of.
	 * @return an instance of the requested bean.
	 */
	public <T> T getBean(final String beanName) {
		return beanFactory.<T>getBean(beanName);
	}

	/**
	 * Gets the persistent bean finder.
	 *
	 * @return the persistentBeanFinder
	 */
	public PersistentBeanFinder getPersistentBeanFinder() {
		return persistentBeanFinder;
	}

	/**
	 * Sanity check of this service instance.
	 *
	 * @throws EpServiceException - if something goes wrong.
	 */
	public void sanityCheck() throws EpServiceException {
		if (getPersistenceEngine() == null) {
			throw new EpServiceException("The persistence engine is not correctly initialized.");
		}
	}

	/**
	 * Gets the persistence engine.
	 * 
	 * @return The persistence engine.
	 */
	public PersistenceEngine getPersistenceEngine() {
		return persistenceEngine;
	}

	/**
	 * Sets the persistence engine to use.
	 * 
	 * @param persistenceEngine The persistence engine.
	 */
	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	/**
	 * Sets the bean factory object.
	 * 
	 * @param beanFactory the bean factory instance.
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	/**
	 * Sets fetch plan helper.
	 *
	 * @param fetchPlanHelper the fetchPlanHelper to set
	 */
	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}

	/**
	 * Helper for finding persistent beans which determines the bean implementation class from a bean factory and then delegates the loading to a
	 * {@link PersistenceEngine}.
	 */
	public final class PersistentBeanFinder {

		/**
		 * Private constructor to prevent external instantiation.
		 */
		PersistentBeanFinder() {
			// Prevent external instantiation.
		}

		/**
		 * Get a persistent instance with the given id. Return null if no matching record exists.
		 *
		 * @param <T> the type of the object
		 * @param beanName the name of the bean to find the implementation class for.
		 * @param uidPk the persistent instance id.
		 * @return the persistent instance
		 * @throws com.elasticpath.persistence.api.EpPersistenceException - in case of persistence errors
		 */
		public <T extends Persistable> T get(final String beanName, final long uidPk) {
			return getPersistenceEngine().get(beanFactory.<T>getBeanImplClass(beanName), uidPk);
		}

		/**
		 * Set a map with pairs of Class and a collection of lazy fields to be loaded.
		 * E.g CustomerImpl.class, {"preferredShippingAddress", "customerProfile"}
		 *
		 * @param lazyFields a map with lazy fields to be loaded.
		 * @return the current instance of {@link PersistenceEngine}
		 */
		public PersistentBeanFinder withCollectionOfLazyFields(final Map<Class<?>, Collection<String>> lazyFields) {
			fetchPlanHelper.setCollectionOfLazyFields(lazyFields);
			return this;
		}
	}
}
