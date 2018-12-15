/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offerdefinitions.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.offerdefinitions.OfferDefinitionResource;
import com.elasticpath.rest.resource.offerdefinitions.constant.OfferDefinitionResourceConstants;

/**
 * Offer Definition prototype for Info operation.
 */
public class InfoOfferDefinitionPrototype implements OfferDefinitionResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(OfferDefinitionResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
