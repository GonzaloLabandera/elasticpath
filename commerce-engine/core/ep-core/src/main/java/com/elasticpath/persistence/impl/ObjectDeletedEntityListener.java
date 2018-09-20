/**
 * Copyright (c) Elastic Path Software Inc., 2014
 */
package com.elasticpath.persistence.impl;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.event.AbstractLifecycleListener;
import org.apache.openjpa.event.LifecycleEvent;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.catalog.ProductSku;
import com.elasticpath.domain.search.ObjectDeleted;
import com.elasticpath.domain.search.impl.ObjectDeletedImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.service.misc.TimeService;

/**
 * EntityListener which sets the {@code lastModifiedDate} on entities.
 */
public class ObjectDeletedEntityListener extends AbstractLifecycleListener {
	
	private TimeService timeService;
	private BeanFactory beanFactory;
	
	@Override
	public void beforeDelete(final LifecycleEvent event) {
		PersistenceCapable pcObject = (PersistenceCapable) event.getSource();
		if (pcObject instanceof ProductSku) {
			ObjectDeleted objectDeleted = new ObjectDeletedImpl();
			objectDeleted.setObjectType(ObjectDeleted.OBJECT_DELETED_TYPE_SKU);
			objectDeleted.setObjectUid(((ProductSku) pcObject).getUidPk());
			objectDeleted.setDeletedDate(getTimeService().getCurrentTime());

			getPersistenceEngine().save(objectDeleted);
		}
	}
	
	private PersistenceEngine getPersistenceEngine() {
		return beanFactory.getBean(ContextIdNames.PERSISTENCE_ENGINE);
	}

	/**
	 * Get the time service.
	 * 
	 * @return the time service.
	 */
	protected TimeService getTimeService() {
		if (timeService == null) {
			timeService = beanFactory.getBean(ContextIdNames.TIME_SERVICE);
		}
		return timeService;
	}
	
	/**
	 * 
	 * @param beanFactory The bean factory.
	 */
	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory; 
	}
}
