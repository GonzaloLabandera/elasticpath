/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.geographies.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.geographies.CountriesIdentifier;
import com.elasticpath.rest.definition.geographies.RootToCountriesRelationship;
import com.elasticpath.rest.helix.data.annotation.UserScopes;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Root to countries link.
 */
public class RootToCountriesRelationshipImpl implements RootToCountriesRelationship.LinkTo {

	private final Iterable<String> scopes;

	/**
	 * Constructor.
	 *
	 * @param scopes scopes
	 */
	@Inject
	public RootToCountriesRelationshipImpl(@UserScopes final Iterable<String> scopes) {
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
