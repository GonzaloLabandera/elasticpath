/*
 * Copyright (c) Elastic Path Software Inc., 2019
 */

package com.elasticpath.persistence.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.elasticpath.persistence.api.ChangeType;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.persistence.api.PersistenceEngineOperationListener;
import com.elasticpath.persistence.openjpa.routing.HDSSupportBean;
import com.elasticpath.persistence.openjpa.util.QueryUtil;

/**
 * This listener is required to support horizontal database scaling (HDS) feature.
 * If HDS feature is enabled, every modified entity will be stored in a list of modified entities, sitting
 * in a thread-local variable within {@link HDSSupportBean}.
 */
public class EntityModifiedListener implements PersistenceEngineOperationListener {

	private static final Logger LOG = LoggerFactory.getLogger(EntityModifiedListener.class);

	private HDSSupportBean hdsSupportBean;
	private QueryUtil queryUtil;

	@Override
	public void endSingleOperation(final Persistable entity, final ChangeType type) {
		if (hdsSupportBean.isHdsSupportEnabled()) {
			String modifiedEntityName = queryUtil.getEntityClassName(entity.getClass());

			LOG.debug("Entity {} is modified. Storing the name in HDSSupportBean TL list", modifiedEntityName);

			hdsSupportBean.addModifiedEntity(modifiedEntityName);
		}
	}

	public void setHdsSupportBean(final HDSSupportBean hdsSupportBean) {
		this.hdsSupportBean = hdsSupportBean;
	}

	public void setQueryUtil(final QueryUtil queryUtil) {
		this.queryUtil = queryUtil;
	}
}
