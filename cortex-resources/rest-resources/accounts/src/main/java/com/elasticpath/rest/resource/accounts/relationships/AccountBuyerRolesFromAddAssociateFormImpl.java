/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.relationships;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesFromAddAccountAssociateByEmailFormRelationship;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.accounts.AddAssociateFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account buyer roles from add associate form link.
 */
public class AccountBuyerRolesFromAddAssociateFormImpl implements AccountBuyerRolesFromAddAccountAssociateByEmailFormRelationship.LinkTo {
	private final AddAssociateFormIdentifier identifier;
	private final LinksRepository<AddAssociateFormIdentifier, AccountBuyerRolesIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier identifier.
	 * @param repository the repository.
	 */
	@Inject
	public AccountBuyerRolesFromAddAssociateFormImpl(@RequestIdentifier final AddAssociateFormIdentifier identifier,
			@ResourceRepository final LinksRepository<AddAssociateFormIdentifier, AccountBuyerRolesIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Observable<AccountBuyerRolesIdentifier> onLinkTo() {
		return repository.getElements(identifier);
	}
}
