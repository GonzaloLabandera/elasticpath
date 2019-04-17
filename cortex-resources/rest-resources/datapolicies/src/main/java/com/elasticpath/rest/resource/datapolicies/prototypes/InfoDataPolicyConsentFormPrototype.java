/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.datapolicies.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.datapolicies.DataPolicyConsentFormResource;
import com.elasticpath.rest.resource.datapolicies.constants.DataPoliciesResourceFamilyConstants;

/**
 * Data policy consent form prototype for read operations.
 */
public class InfoDataPolicyConsentFormPrototype implements DataPolicyConsentFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(DataPoliciesResourceFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
