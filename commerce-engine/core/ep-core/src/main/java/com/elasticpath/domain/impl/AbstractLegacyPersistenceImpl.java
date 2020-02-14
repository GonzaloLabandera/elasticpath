/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.impl;

import com.elasticpath.base.Initializable;
import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * This class provides methods to allow accessing {@link ElasticPath} and getting beans from within the
 * persistent objects.
 */
public abstract class AbstractLegacyPersistenceImpl extends AbstractPersistableImpl implements Initializable, BeanFactory {
	/**
	 * Serial version id.
	 */
	private static final long serialVersionUID = 5000000001L;

	/**
	 * Get the ElasticPath singleton.
	 * Consider using {@link #getPrototypeBean(String, Class)} directly on the bean factory
	 * for obtaining new instances of prototype beans.
	 *
	 * @return elasticpath the ElasticPath singleton.
	 */
	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	protected ElasticPath getElasticPath() {
		return ElasticPathImpl.getInstance();
	}

	@Override
	@Deprecated
	public <T> T getBean(final String beanName) {
		return getElasticPath().getBean(beanName);
	}

	@Override
	public <T> T getPrototypeBean(final String name, final Class<T> clazz) {
		return getElasticPath().getPrototypeBean(name, clazz);
	}

	@Override
	public <T> T getSingletonBean(final String name, final Class<T> clazz) {
		return getElasticPath().getSingletonBean(name, clazz);
	}

	@Override
	public <T> Class<T> getBeanImplClass(final String beanName) {
		return getElasticPath().getBeanImplClass(beanName);
	}

	/**
	 * Set default values for those fields need default values and it's somehow expensive to create the default values for them, either from memory
	 * perspective or cpu perspective. A good example of a memory expensive field will be a field with type <code>Map</code>. Another good example
	 * of a cpu expensive field will be a field like GUID, current date, etc. We prefer this way rather than using the domain object constructor. It
	 * doesn't make sense to set default values everytime when creating a new domain object, because most of the time the default value you set will
	 * be overwritten by hibernate immediately.
	 * @deprecated use initialize instead
	 */
	@Deprecated
	public void setDefaultValues() {
		//do nothing.
	}

	/**
	 * Default implementation for initialize().  Calls setDefaultValues() for compatibility with legacy code.
	 */
	@Override
	public void initialize() {
		setDefaultValues();
	}
}
