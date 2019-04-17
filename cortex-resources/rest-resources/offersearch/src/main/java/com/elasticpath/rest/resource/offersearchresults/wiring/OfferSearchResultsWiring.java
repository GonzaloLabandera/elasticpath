/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.offersearchresults.wiring;

import javax.inject.Named;

import com.elasticpath.rest.definition.offersearches.OfferSearchResultResource;
import com.elasticpath.rest.helix.api.AbstractHelixModule;

/**
 *  Peaberry plumbing class used for importing required services and registering additional parameter resolvers.
 */
@Named
public class OfferSearchResultsWiring extends AbstractHelixModule {

	@Override
	protected String resourceName() {
		return OfferSearchResultResource.FAMILY;
	}
}
