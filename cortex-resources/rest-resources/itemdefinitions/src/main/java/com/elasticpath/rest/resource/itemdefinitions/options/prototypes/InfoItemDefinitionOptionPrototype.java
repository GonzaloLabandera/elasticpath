/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.options.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionOptionResource;
import com.elasticpath.rest.resource.itemdefinitions.constant.ItemDefinitionResourceConstants;

/**
 * Item Definition Option prototype for Info operation.
 */
public class InfoItemDefinitionOptionPrototype implements ItemDefinitionOptionResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(ItemDefinitionResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
