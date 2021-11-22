/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.rest.definition.offers.CodeEntity;
import com.elasticpath.rest.definition.offers.OfferLookupFormResource;

/**
 * Read offerlookupform.
 */
public class ReadOfferLookupFormPrototype implements OfferLookupFormResource.Read {

	@Override
	public Single<CodeEntity> onRead() {
		return Single.just(CodeEntity.builder()
				.withCode(StringUtils.EMPTY)
				.build());
	}
}
