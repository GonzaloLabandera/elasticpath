/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.service.impl;

import java.util.Collection;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.elasticpath.base.exception.EpServiceException;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.domain.impl.ElasticPathImpl;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;
import com.elasticpath.service.EpPersistenceService;

/**
 * <code>AbstractEpPersistenceServiceImpl</code> is abstract implementation of the base interface for
 * other services of the persistable domain models.
 */
public abstract class AbstractEpPersistenceServiceImpl implements EpPersistenceService, BeanFactory {
	private static final Logger LOG = LogManager.getLogger(AbstractEpPersistenceServiceImpl.class);
	private PersistenceEngine persistenceEngine;
	private final PersistentBeanFinder persistentBeanFinder = new PersistentBeanFinder();

	private FetchPlanHelper fetchPlanHelper;
	private ElasticPath elasticPath;

	/**
	 * Sets the persistence engine.
	 *
	 * @param persistenceEngine the persistence engine to set.
	 */
	@Override
	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
		if (LOG.isDebugEnabled()) {
			LOG.debug("Persistence engine initialized ... " + persistenceEngine);
		}
	}

	/**
	 * Returns the persistence engine.
	 *
	 * @return the persistence engine.
	 */
	@Override
	public PersistenceEngine getPersistenceEngine() {
		return this.persistenceEngine;
	}

	/**
	 * Sanity check of this service instance.
	 * @throws EpServiceException - if something goes wrong.
	 */
	protected void sanityCheck() throws EpServiceException {
		if (getPersistenceEngine() == null) {
			throw new EpServiceException("The persistence engine is not correctly initialized.");
		}
	}

	/**
	 * Load method for all persistable domain models specifying fields to be loaded.
	 * By default, just calls the generic load method.
	 *
	 * @param uid the persisted instance uid
	 * @param fieldsToLoad the fields of this object that need to be loaded
	 * @return the persisted instance if exists, otherwise null
	 * @throws EpServiceException - in case of any errors
	 */
	@Override
	public Object getObject(final long uid, final Collection<String> fieldsToLoad) throws EpServiceException {
		return getObject(uid);
	}
	
	/**
	 * Return a convience class for retrieving persistent bean instances from 
	 * their bean name, not their implementation class.
	 * 
	 * @return a finder for persistent beans.
	 */
	protected PersistentBeanFinder getPersistentBeanFinder() {
		return persistentBeanFinder;
	}

	/**
	 * Helper for finding persistent beans which determines the bean implementation
	 * class from a bean factory and then delegates the loading to a 
	 * {@link PersistenceEngine}.
	 */
	protected final class PersistentBeanFinder {
		
		/**
		 * Private constructor to prevent external instantiation.
		 */
		PersistentBeanFinder() {
			// Prevent external instantiation.
		}
		
		/**
		 * Load a persistent instance with a given id.  The persistent class to 
		 * load will be determined from the beanName
		 * Throw an unrecoverable exception if there is no matching database row.
		 *
		 * @param <T> the type of the object
		 * @param beanName the name of the bean to find the implementation class for.
		 * @param uidPk the persistent instance id.
		 * @return the persistent instance
		 * @throws com.elasticpath.persistence.api.EpPersistenceException - in case of persistence errors
		 */
		public <T extends Persistable> T load(final String beanName, final long uidPk) {
			return getPersistenceEngine().load(getElasticPath().<T>getBeanImplClass(beanName), uidPk);
		}
		
		/**
		 * Load a persistent instance with the given id. Throw an unrecoverable exception if there is
		 * no matching database row. This method will create a new session (EntityManager) to execute
		 * the query, and close the new session when completed.
		 * 
		 * @param <T> the type of the object
		 * @param beanName the name of the bean to find the implementation class for.
		 * @param uidPk the persistent instance id.
		 * @return the persistent instance
		 * @throws com.elasticpath.persistence.api.EpPersistenceException in case of persistence errors
		 */
		public <T extends Persistable> T loadWithNewSession(final String beanName, final long uidPk) {
			return getPersistenceEngine().loadWithNewSession(getElasticPath().<T>getBeanImplClass(beanName), uidPk);
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
			return getPersistenceEngine().get(getElasticPath().<T>getBeanImplClass(beanName), uidPk);
		}

		/**
		 * Set one or more load tuners.
		 *
		 * @param loadTuners an array of load tuners.
		 * @return the current instance of {@link PersistenceEngine}
		 */
		public PersistentBeanFinder withLoadTuners(final LoadTuner... loadTuners) {
			fetchPlanHelper.setLoadTuners(loadTuners);
			return this;
		}

		/**
		 * Set a map with pairs of Class and a lazy field to be loaded.
		 * E.g CustomerImpl.class, "preferredShippingAddress"
		 *
		 * @param lazyFields a map with lazy fields to be loaded.
		 * @return the current instance of {@link PersistenceEngine}
		 */
		public PersistentBeanFinder withLazyFields(final Map<Class<?>, String> lazyFields) {
			fetchPlanHelper.setLazyFields(lazyFields);
			return this;
		}
	}

	/**
	 * Inject the ElasticPath singleton for tests.
	 *
	 * @param elasticpath the ElasticPath singleton.
	 */
	public void setElasticPath(final ElasticPath elasticpath) {
		this.elasticPath = elasticpath;
	}

	/**
	 * Get the ElasticPath singleton.
	 *
	 * @return elasticpath the ElasticPath singleton.
	 */
	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	public ElasticPath getElasticPath() {
		if (this.elasticPath != null) {
			return this.elasticPath;
		}
		return ElasticPathImpl.getInstance();
	}

	
	@Deprecated
	@Override
	public <T> T getBean(final String beanName) {
		return getElasticPath().getBean(beanName);
	}
	
	@Override
	public <T> Class<T> getBeanImplClass(final String beanName) {
		return getElasticPath().getBeanImplClass(beanName);
	}

	@Override
	public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
		return getElasticPath().getPrototypeBean(name, clazz);
	}

	@Override
	public <T> T getSingletonBean(final String name, final Class<T> clazz) {
		return  getElasticPath().getSingletonBean(name, clazz);
	}

	public FetchPlanHelper getFetchPlanHelper() {
		return fetchPlanHelper;
	}

	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}
}
