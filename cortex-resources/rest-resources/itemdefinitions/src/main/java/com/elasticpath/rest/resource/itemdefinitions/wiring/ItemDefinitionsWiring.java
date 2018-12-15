/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.wiring;

import javax.inject.Named;

import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionResource;
import com.elasticpath.rest.helix.api.AbstractHelixModule;

/**
 * Peaberry plumbing class used for importing required services and registering additional parameter resolvers.
 */
@Named
public class ItemDefinitionsWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return ItemDefinitionResource.FAMILY;
	}
}

