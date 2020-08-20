/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.accounts.AccountsResource;
import com.elasticpath.rest.resource.accounts.constants.AccountsFamilyConstants;

/**
 * Accounts Info.
 */
public class InfoAccountsPrototype implements AccountsResource.Info {
	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(AccountsFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}

