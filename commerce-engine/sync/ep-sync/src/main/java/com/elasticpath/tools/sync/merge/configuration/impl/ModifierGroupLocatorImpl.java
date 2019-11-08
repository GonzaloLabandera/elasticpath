/*
 * Copyright (c) Elastic Path Software Inc., 2016
 */

package com.elasticpath.tools.sync.merge.configuration.impl;

import com.elasticpath.domain.modifier.ModifierGroup;
import com.elasticpath.persistence.api.Persistable;
import com.elasticpath.service.modifier.ModifierService;
import com.elasticpath.tools.sync.exception.SyncToolConfigurationException;

/**
 * Locator for {@link ModifierGroup}.
 */
public class ModifierGroupLocatorImpl extends AbstractEntityLocator {

	private ModifierService cartItemModifierService;

	@Override
	public Persistable locatePersistence(final String guid, final Class<?> clazz) throws SyncToolConfigurationException {
		return cartItemModifierService.findModifierGroupByCode(guid);
	}

	@Override
	public boolean isResponsibleFor(final Class<?> clazz) {
		return ModifierGroup.class.isAssignableFrom(clazz);
	}

	public void setModifierService(final ModifierService cartItemModifierService) {
		this.cartItemModifierService = cartItemModifierService;
	}
}
