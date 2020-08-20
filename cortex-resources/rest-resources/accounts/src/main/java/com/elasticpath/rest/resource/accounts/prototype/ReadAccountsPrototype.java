/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountsResource;
import com.elasticpath.rest.definition.emails.EmailsIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.helix.data.annotation.UriPart;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Read Accounts Prototype.
 */
public class ReadAccountsPrototype implements AccountsResource.Read {

	private final Repository<AccountEntity, AccountIdentifier> repository;
	private final IdentifierPart<String> scope;


	/**
	 * Constructor.
	 *
	 * @param scope the scope
	 * @param repository repository
	 */
	@Inject
	public ReadAccountsPrototype(@ResourceRepository final Repository<AccountEntity, AccountIdentifier> repository,
								 @UriPart(EmailsIdentifier.SCOPE) final IdentifierPart<String> scope) {
		this.repository = repository;
		this.scope = scope;
	}

	@Override
	public Observable<AccountIdentifier> onRead() {
		return repository.findAll(scope);
	}
}
