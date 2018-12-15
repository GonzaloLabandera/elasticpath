/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemselections.wiring;

import javax.inject.Named;

import com.elasticpath.rest.definition.itemselections.ItemOptionSelectorResource;
import com.elasticpath.rest.helix.api.AbstractHelixModule;

/**
 * Peaberry plumbing class used for importing required services and registering additional parameter resolvers.
 */
@Named
public class ItemSelectionsWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return ItemOptionSelectorResource.FAMILY;
	}
}
