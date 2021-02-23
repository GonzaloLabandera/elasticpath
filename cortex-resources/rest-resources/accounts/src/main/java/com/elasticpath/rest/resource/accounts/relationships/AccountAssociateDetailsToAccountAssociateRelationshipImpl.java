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
 * Account associate details to Account associate link.
 */
public class AccountAssociateDetailsToAccountAssociateRelationshipImpl implements AssociatedetailsFromAssociateRelationship.LinkFrom {

	private final AssociatedetailsIdentifier associatedetailsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associatedetailsIdentifier identifier
	 */
	@Inject
	public AccountAssociateDetailsToAccountAssociateRelationshipImpl(@RequestIdentifier final AssociatedetailsIdentifier associatedetailsIdentifier) {
		this.associatedetailsIdentifier = associatedetailsIdentifier;
	}

	@Override
	public Observable<AssociateIdentifier> onLinkFrom() {
		return Observable.just(associatedetailsIdentifier.getAssociate());
	}
}