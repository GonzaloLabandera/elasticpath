/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.accounts.ReferencesToBuyerRolesRelationship;
import com.elasticpath.rest.definition.references.ReferencesIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account buyer roles from references link.
 */
public class AccountBuyerRolesFromReferencesRelationshipImpl implements ReferencesToBuyerRolesRelationship.LinkTo {

	private final ReferencesIdentifier identifier;
	private final LinksRepository<ReferencesIdentifier, AccountBuyerRolesIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param identifier identifier.
	 * @param repository the repository.
	 */
	@Inject
	public AccountBuyerRolesFromReferencesRelationshipImpl(@RequestIdentifier final ReferencesIdentifier identifier,
														   @ResourceRepository final LinksRepository<ReferencesIdentifier,
																AccountBuyerRolesIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountBuyerRolesIdentifier> onLinkTo() {
		return repository.getElements(identifier);
	}
}
