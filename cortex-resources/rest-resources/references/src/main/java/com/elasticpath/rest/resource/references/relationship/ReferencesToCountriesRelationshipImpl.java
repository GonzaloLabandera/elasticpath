/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.references.relationship;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.geographies.CountriesIdentifier;
import com.elasticpath.rest.definition.references.ReferencesToCountriesRelationship;
import com.elasticpath.rest.helix.data.annotation.UserScopes;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Link from References to Countries.
 */
public class ReferencesToCountriesRelationshipImpl implements ReferencesToCountriesRelationship.LinkTo {
	private final Iterable<String> scopes;

	/**
	 * Constructor.
	 *
	 * @param scopes The scopes
	 */
	@Inject
	public ReferencesToCountriesRelationshipImpl(@UserScopes final Iterable<String> scopes) {
		this.scopes = scopes;
	}

	@Override
	public Observable<CountriesIdentifier> onLinkTo() {
		return Observable.fromIterable(scopes)
				.map(StringIdentifier::of)
				.map(scopeId -> CountriesIdentifier.builder().withScope(scopeId).build())
				.firstElement().toObservable();
	}
}