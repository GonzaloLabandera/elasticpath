/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.profiles.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.profiles.ProfileIdentifier;
import com.elasticpath.rest.definition.profiles.SelectedAccountFromProfileRelationship;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Selected Account from Profile link.
 */
public class SelectedAccountFromProfileRelationshipImpl implements SelectedAccountFromProfileRelationship.LinkTo {

	private final ProfileIdentifier profileIdentifier;
	private final LinksRepository<ProfileIdentifier, AccountIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param profileIdentifier the profile identifier
	 * @param repository        the links repository
	 */
	@Inject
	public SelectedAccountFromProfileRelationshipImpl(@RequestIdentifier final ProfileIdentifier profileIdentifier,
													  @ResourceRepository final LinksRepository<ProfileIdentifier, AccountIdentifier> repository) {

		this.profileIdentifier = profileIdentifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountIdentifier> onLinkTo() {
		return repository.getElements(profileIdentifier);
	}
}
