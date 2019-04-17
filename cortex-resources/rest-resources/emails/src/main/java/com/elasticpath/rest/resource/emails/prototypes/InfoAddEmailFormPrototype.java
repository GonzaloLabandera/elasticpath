/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.emails.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.emails.AddEmailFormResource;
import com.elasticpath.rest.resource.emails.constants.EmailsResourceFamilyConstants;

/**
 * Add Email Form prototype for Info operation.
 */
public class InfoAddEmailFormPrototype implements AddEmailFormResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(EmailsResourceFamilyConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
