/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.searches.keyword.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.searches.KeywordSearchFormResource;
import com.elasticpath.rest.resource.searches.constants.SearchesResourceFamilyConstants;

/**
 * Keyword Search Form prototype for Info operation.
 */
public class InfoKeywordSearchFormPrototype implements KeywordSearchFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(SearchesResourceFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
