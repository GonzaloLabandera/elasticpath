/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountResource;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Customer account prototype for Update operation.
 */
public class UpdateAccountPrototype implements AccountResource.Update {

	private final AccountEntity accountEntity;

	private final AccountIdentifier accountIdentifier;

	private final Repository<AccountEntity, AccountIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountEntity profile entity
	 * @param accountIdentifier profile identifier
	 * @param repository        repository
	 */
	@Inject
	public UpdateAccountPrototype(
			@RequestForm final AccountEntity accountEntity,
			@RequestIdentifier final AccountIdentifier accountIdentifier,
			@ResourceRepository final Repository<AccountEntity, AccountIdentifier> repository) {
		this.accountEntity = accountEntity;
		this.accountIdentifier = accountIdentifier;
		this.repository = repository;
	}

	@Override
	public Completable onUpdate() {
		return repository.update(accountEntity, accountIdentifier);
	}
}
