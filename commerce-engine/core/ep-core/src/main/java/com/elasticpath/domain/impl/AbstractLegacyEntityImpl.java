/**
 * Copyright (c) Elastic Path Software Inc., 2013
 */
package com.elasticpath.domain.impl;

import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.commons.util.Utility;
import com.elasticpath.domain.ElasticPath;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * This class provides methods to allow accessing {@link ElasticPath} and getting beans from within the 
 * persistent objects. //REFACTORING
 */
public abstract class AbstractLegacyEntityImpl extends AbstractEntityImpl {
	private static final long serialVersionUID = 1L;

	/**
	 * Inject the ElasticPath singleton.
	 *
	 * @param elasticpath the ElasticPath singleton.
	 */
	@Deprecated
	public void setElasticPath(final ElasticPath elasticpath) {
		// Do nothing, ElasticPath instance doesn't need inject anymore.
	}

	/**
	 * Get the ElasticPath singleton.
	 * Consider using {@link #getBean(String)} for obtaining new
	 * instances of prototype beans.
	 *
	 * @return elasticpath the ElasticPath singleton.
	 */
	@SuppressWarnings("PMD.DontUseElasticPathImplGetInstance")
	public ElasticPath getElasticPath() {
		return ElasticPathImpl.getInstance();
	}

	/**
	 * Convenience method for getting a bean instance from elastic path.
	 * @param <T> the type of bean to return
	 * @param beanName the name of the bean to get and instance of.
	 * @return an instance of the requested bean.
	 */
	protected <T> T getBean(final String beanName) {
		return getElasticPath().<T>getBean(beanName);
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
		// allows subclasses to override out of the box
		// implementation of initialize()
		// we will inline this method into initialize() once
		// we finally remove setDefaultValue()
		super.initialize();
	}

	/**
	 * Default implementation for initialize().  Calls setDefaultValues() for compatibility with legacy code.
	 */
	@Override
	public void initialize() {
		setDefaultValues();
	}

	/**
	 * Returns the <code>Utility</code> singleton.
	 * @return the <code>Utility</code> singleton.
	 * @deprecated If the implementation class needs the Utility object it should be retrieved inside that class.
	 */
	@Deprecated
	public Utility getUtility() {
		return getBean(ContextIdNames.UTILITY);
	}
}
