/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.profiles.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.profiles.AttributesFromProfileRelationship;
import com.elasticpath.rest.definition.profiles.AttributesIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Implementation for attributes-from-profile.
 */
public class ProfileAttributesFromProfileRelationshipImpl implements AttributesFromProfileRelationship.LinkTo {

	@Inject
	@RequestIdentifier
	private ProfileIdentifier profileIdentifier;

	@Override
	public Observable<AttributesIdentifier> onLinkTo() {
		return Observable.just(AttributesIdentifier.builder().withProfile(profileIdentifier).build());
	}
}
