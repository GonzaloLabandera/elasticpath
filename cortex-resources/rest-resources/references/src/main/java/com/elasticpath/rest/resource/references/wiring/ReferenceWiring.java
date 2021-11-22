/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.references.wiring;

import javax.inject.Named;

import com.elasticpath.rest.definition.references.ReferencesResource;
import com.elasticpath.rest.helix.api.AbstractHelixModule;

/**
 * References wiring.
 */
@Named
public class ReferenceWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return ReferencesResource.FAMILY;
	}
}