/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.impl;

import static com.elasticpath.persistence.openjpa.util.QueryUtil.getEntityClassName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.ChangeType;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngineOperationListener;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;

/**
 * This listener is required to support horizontal database scaling (HDS) feature.
 * If HDS feature is enabled, every modified entity will be stored in a list of modified entities, sitting
 * in a thread-local variable within {@link HDSSupportBean}.
 */
public class EntityModifiedListener implements PersistenceEngineOperationListener {

	private static final Logger LOG = LoggerFactory.getLogger(EntityModifiedListener.class);

	private HDSSupportBean hdsSupportBean;

	@Override
	public void endSingleOperation(final Persistable entity, final ChangeType type) {
		if (hdsSupportBean.isHdsSupportEnabled()) {
			String modifiedEntityName = getEntityClassName(entity.getClass());

			LOG.debug("Entity {} is modified. Storing the name in HDSSupportBean TL list", modifiedEntityName);

			hdsSupportBean.addModifiedEntity(modifiedEntityName);
		}
	}

	public void setHdsSupportBean(final HDSSupportBean hdsSupportBean) {
		this.hdsSupportBean = hdsSupportBean;
	}
}
