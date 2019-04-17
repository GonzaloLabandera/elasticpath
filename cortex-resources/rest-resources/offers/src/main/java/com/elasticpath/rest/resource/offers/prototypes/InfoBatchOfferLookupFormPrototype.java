/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.offers.BatchOffersLookupFormResource;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants;

/**
 * Info for batch offer lookup form.
 */
public class InfoBatchOfferLookupFormPrototype implements BatchOffersLookupFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(OffersResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
