/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.registrations.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.registrations.NewAccountRegistrationFormResource;
import com.elasticpath.rest.resource.registrations.constants.RegistrationsResourceFamilyConstants;

/**
 * New Account Registration Form prototype for Info operation.
 */
public class InfoNewAccountRegistrationFormPrototype implements NewAccountRegistrationFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(RegistrationsResourceFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}

}
