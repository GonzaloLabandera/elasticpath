/**
 * Copyright (c) Elastic Path Software Inc., 2008
 */
package com.elasticpath.persistence.dao.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.persistence.api.LoadTuner;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.persistence.openjpa.util.FetchPlanHelper;

/**
 * Defines methods useful for all persistable DAO classes.
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class AbstractDaoImpl implements BeanFactory {

	private PersistenceEngine persistenceEngine;
	private BeanFactory beanFactory;
	private FetchPlanHelper fetchPlanHelper;
	private final PersistentBeanFinder persistentBeanFinder = new PersistentBeanFinder();
	
	/**
	 * Helper for finding persistent beans which determines the bean implementation
	 * class from a bean factory and then delegates the loading to a 
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
			return getPersistenceEngine().load(beanFactory.<T>getBeanImplClass(beanName), uidPk);
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
	}

	@Override
	@Deprecated
	public <T> T getBean(final String beanName) {
		return beanFactory.getBean(beanName);
	}

	@Override
	public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
		return beanFactory.getPrototypeBean(name, clazz);
	}

	@Override
	public <T> T getSingletonBean(final String name, final Class<T> clazz) {
		return beanFactory.getSingletonBean(name, clazz);
	}

	@Override
	public <T> Class<T> getBeanImplClass(final String beanName) {
		return beanFactory.getBeanImplClass(beanName);
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
	 * Return a convenience class for retrieving persistent bean instances from 
	 * their bean name, not their implementation class.
	 * 
	 * @return a finder for persistent beans.
	 */
	public PersistentBeanFinder getPersistentBeanFinder() {
		return persistentBeanFinder;
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
	 * Sets the persistence engine to use.
	 *  
	 * @param persistenceEngine The persistence engine.
	 */
	public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
		this.persistenceEngine = persistenceEngine;
	}

	public void setFetchPlanHelper(final FetchPlanHelper fetchPlanHelper) {
		this.fetchPlanHelper = fetchPlanHelper;
	}

	public FetchPlanHelper getFetchPlanHelper() {
		return fetchPlanHelper;
	}
}
