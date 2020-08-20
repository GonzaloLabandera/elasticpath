/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.tools.sync.target.impl;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.service.modifier.ModifierService;
import com.elasticpath.tools.sync.exception.SyncToolConfigurationException;
import com.elasticpath.tools.sync.exception.SyncToolRuntimeException;

/**
 * Dao Adapter for {@link ModifierGroup}.
 */
public class ModifierGroupDaoAdapterImpl extends AbstractDaoAdapter<ModifierGroup> {

	private ModifierService cartItemModifierService;
	private BeanFactory beanFactory;

	@Override
	public ModifierGroup update(final ModifierGroup mergedPersistence) throws SyncToolRuntimeException {
		return cartItemModifierService.saveOrUpdate(mergedPersistence);
	}

	@Override
	public void add(final ModifierGroup newPersistence) throws SyncToolRuntimeException {
		cartItemModifierService.saveOrUpdate(newPersistence);
	}

	@Override
	public boolean remove(final String guid) throws SyncToolRuntimeException {
		ModifierGroup cartItemModifierGroup = get(guid);
		if (cartItemModifierGroup == null) {
			return false;
		}
		cartItemModifierService.remove(cartItemModifierGroup);
		return true;
	}

	@Override
	public ModifierGroup get(final String guid) {
		try {
			return (ModifierGroup) getEntityLocator().locatePersistence(guid, ModifierGroup.class);
		} catch (SyncToolConfigurationException e) {
			throw new SyncToolRuntimeException("Unable to locate persistence", e);
		}
	}

	@Override
	public ModifierGroup createBean(final ModifierGroup bean) {
		return beanFactory.getPrototypeBean(ContextIdNames.MODIFIER_GROUP, ModifierGroup.class);
	}

	public void setModifierService(final ModifierService cartItemModifierService) {
		this.cartItemModifierService = cartItemModifierService;
	}

	public void setBeanFactory(final BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

}
