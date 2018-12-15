/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.wiring;

import javax.inject.Named;

import com.elasticpath.rest.definition.offers.OfferResource;
import com.elasticpath.rest.helix.api.AbstractHelixModule;

/**
 * Peaberry plumbing class used for importing required services and registering additional parameter resolvers.
 */
@Named
public class OffersWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return OfferResource.FAMILY;
	}
}
