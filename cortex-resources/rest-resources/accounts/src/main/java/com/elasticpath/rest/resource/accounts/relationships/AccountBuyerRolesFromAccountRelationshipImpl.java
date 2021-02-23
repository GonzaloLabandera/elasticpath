/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesFromAccountRelationship;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account buyer roles from account link.
 */
public class AccountBuyerRolesFromAccountRelationshipImpl implements AccountBuyerRolesFromAccountRelationship.LinkTo {

	private final AccountsIdentifier identifier;
	private final LinksRepository<AccountsIdentifier, AccountBuyerRolesIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier identifier.
	 * @param repository the repository.
	 */
	@Inject
	public AccountBuyerRolesFromAccountRelationshipImpl(@RequestIdentifier final AccountsIdentifier identifier,
												 @ResourceRepository final LinksRepository<AccountsIdentifier,
														 AccountBuyerRolesIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountBuyerRolesIdentifier> onLinkTo() {
		return repository.getElements(identifier);
	}
}
