/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.addresses.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.addresses.AccountAddressesResource;
import com.elasticpath.rest.resource.addresses.constants.AddressesResourceConstants;

/**
 * Addresses prototype for Info operation.
 */
public class InfoAccountAddressesPrototype implements AccountAddressesResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(AddressesResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
