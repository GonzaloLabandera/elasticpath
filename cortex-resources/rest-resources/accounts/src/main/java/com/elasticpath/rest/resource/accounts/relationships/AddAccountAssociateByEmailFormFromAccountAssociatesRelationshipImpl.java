/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.accounts.AddAccountAssociateByEmailFormFromAccountAssociatesRelationship;
import com.elasticpath.rest.definition.accounts.AddAssociateFormIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;


/**
 * Link from Associates list to add associate by email form.
 */
public class AddAccountAssociateByEmailFormFromAccountAssociatesRelationshipImpl
		implements AddAccountAssociateByEmailFormFromAccountAssociatesRelationship.LinkTo {

	private final AssociatesIdentifier associatesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associatesIdentifier associatesIdentifier
	 */
	@Inject
	public AddAccountAssociateByEmailFormFromAccountAssociatesRelationshipImpl(@RequestIdentifier final AssociatesIdentifier associatesIdentifier) {
		this.associatesIdentifier = associatesIdentifier;
	}

	@Override
	public Observable<AddAssociateFormIdentifier> onLinkTo() {
		return Observable.just(AddAssociateFormIdentifier.builder().withAssociates(associatesIdentifier).build());
	}
}