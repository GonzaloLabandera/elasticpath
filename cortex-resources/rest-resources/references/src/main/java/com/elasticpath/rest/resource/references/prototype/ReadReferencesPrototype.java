/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.references.prototype;

import io.reactivex.Single;

import com.elasticpath.rest.definition.references.ReferenceEntity;
import com.elasticpath.rest.definition.references.ReferencesResource;

/**
 * References prototype for Read operation.
 */
public class ReadReferencesPrototype implements ReferencesResource.Read {

	@Override
	public Single<ReferenceEntity> onRead() {
		return Single.just(ReferenceEntity.builder().build());
	}
}