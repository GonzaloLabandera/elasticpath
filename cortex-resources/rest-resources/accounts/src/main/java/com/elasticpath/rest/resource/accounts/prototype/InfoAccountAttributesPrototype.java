/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.accounts.AccountAttributesResource;
import com.elasticpath.rest.resource.accounts.constants.AccountsFamilyConstants;

/**
 * Account Attribute Info.
 */
public class InfoAccountAttributesPrototype implements AccountAttributesResource.Info {
	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(AccountsFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}

