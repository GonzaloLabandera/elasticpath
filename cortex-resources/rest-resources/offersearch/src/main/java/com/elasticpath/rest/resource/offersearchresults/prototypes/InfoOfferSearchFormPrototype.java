/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.offersearches.OfferSearchFormResource;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants;

/**
 * Offer Search Form prototype for Info operation.
 */
public class InfoOfferSearchFormPrototype implements OfferSearchFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(OffersResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
