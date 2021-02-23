/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import io.reactivex.Single;
import org.apache.commons.lang3.StringUtils;

import com.elasticpath.rest.definition.accounts.AddAssociateFormEntity;
import com.elasticpath.rest.definition.accounts.AddAssociateFormResource;

/**
 * Account Associate Form prototype for Read operation.
 */
public class ReadAccountAssociateFormPrototype implements AddAssociateFormResource.Read {
	@Override
	public Single<AddAssociateFormEntity> onRead() {

		return Single.just(
				AddAssociateFormEntity.builder()
						.withEmail(StringUtils.EMPTY)
						.withRole(StringUtils.EMPTY)
						.build());
	}

}