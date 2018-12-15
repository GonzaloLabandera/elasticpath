/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.offers.CodeForOfferResource;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants;

/**
 * Code For Offer prototype for Info operation.
 */
public class InfoCodeForOfferPrototype implements CodeForOfferResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(OffersResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
