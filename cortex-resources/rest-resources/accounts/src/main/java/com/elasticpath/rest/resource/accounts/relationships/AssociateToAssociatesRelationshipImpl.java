/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateToAssociatesRelationship;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Link from associate to associates.
 */
public class AssociateToAssociatesRelationshipImpl implements AssociateToAssociatesRelationship.LinkTo {

	private final AssociateIdentifier associateIdentifier;

	/**
	 * Constructor.
	 * @param associateIdentifier identifier
	 */
	@Inject
	public AssociateToAssociatesRelationshipImpl(@RequestIdentifier final AssociateIdentifier associateIdentifier) {
		this.associateIdentifier = associateIdentifier;
	}

	@Override
	public Observable<AssociatesIdentifier> onLinkTo() {
		return Observable.just(associateIdentifier.getAssociates());
	}
}
