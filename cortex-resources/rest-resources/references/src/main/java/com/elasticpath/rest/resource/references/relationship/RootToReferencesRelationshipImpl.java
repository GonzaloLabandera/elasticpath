/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.references.relationship;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.references.ReferencesIdentifier;
import com.elasticpath.rest.definition.references.RootToReferencesRelationship;

/**
 * Root to References link.
 */
public class RootToReferencesRelationshipImpl implements RootToReferencesRelationship.LinkTo {

	@Override
	public Observable<ReferencesIdentifier> onLinkTo() {
		return Observable.just(ReferencesIdentifier.builder().build());
	}
}