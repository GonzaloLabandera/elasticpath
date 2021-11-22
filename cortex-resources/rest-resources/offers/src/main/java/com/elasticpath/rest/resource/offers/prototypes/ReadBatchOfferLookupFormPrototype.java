/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import com.google.common.collect.ImmutableList;
import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.rest.definition.items.CodesEntity;
import com.elasticpath.rest.definition.offers.BatchOffersLookupFormResource;

/**
 * Read batch offer lookup form.
 */
public class ReadBatchOfferLookupFormPrototype implements BatchOffersLookupFormResource.Read {

	@Override
	public Single<CodesEntity> onRead() {
		return Single.just(CodesEntity.builder()
				.withCodes(ImmutableList.of(StringUtils.EMPTY))
				.build());
	}
}
