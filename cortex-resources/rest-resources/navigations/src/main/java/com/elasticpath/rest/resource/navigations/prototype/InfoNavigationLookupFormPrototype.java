/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.navigations.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.navigations.NavigationLookupFormResource;
import com.elasticpath.rest.resource.navigations.constants.NavigationsResourceConstants;

/**
 * Navigation Lookup prototype for Info operation.
 */
public class InfoNavigationLookupFormPrototype implements NavigationLookupFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(NavigationsResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
