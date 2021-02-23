/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatedetailsFromAssociateRelationship;
import com.elasticpath.rest.definition.accounts.AssociatedetailsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Account associate to Account associate details link.
 */
public class AccountAssociateToAccountAssociateDetailsRelationshipImpl implements AssociatedetailsFromAssociateRelationship.LinkTo {

	private final AssociateIdentifier associateIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associateIdentifier associateIdentifier
	 */
	@Inject
	public AccountAssociateToAccountAssociateDetailsRelationshipImpl(@RequestIdentifier final AssociateIdentifier associateIdentifier) {
		this.associateIdentifier = associateIdentifier;
	}

	@Override
	public Observable<AssociatedetailsIdentifier> onLinkTo() {
		return Observable.just(AssociatedetailsIdentifier.builder()
				.withAssociate(associateIdentifier)
				.build());
	}
}