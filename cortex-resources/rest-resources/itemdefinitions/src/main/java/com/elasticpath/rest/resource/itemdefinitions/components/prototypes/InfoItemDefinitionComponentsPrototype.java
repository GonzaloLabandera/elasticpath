/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.itemdefinitions.components.prototypes;

import io.reactivex.Single;

import com.elasticpath.rest.ResourceInfo;
import com.elasticpath.rest.definition.itemdefinitions.ItemDefinitionComponentsResource;
import com.elasticpath.rest.resource.itemdefinitions.constant.ItemDefinitionResourceConstants;

/**
 * Item Definition Components prototype for Read operation.
 */
public class InfoItemDefinitionComponentsPrototype implements ItemDefinitionComponentsResource.Info {

	private static final Single<ResourceInfo> INFO_SINGLE = Single.just(ResourceInfo.builder()
			.withMaxAge(ItemDefinitionResourceConstants.MAX_AGE)
			.build());

	@Override
	public Single<ResourceInfo> onInfo() {
		return INFO_SINGLE;
	}
}
