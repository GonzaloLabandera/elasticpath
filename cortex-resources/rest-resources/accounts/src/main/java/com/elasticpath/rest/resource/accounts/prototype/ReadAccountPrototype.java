/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.accounts.AccountIdentifier;
import com.elasticpath.rest.definition.accounts.AccountResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account prototype for Read operation.
 */
public class ReadAccountPrototype implements AccountResource.Read {

	private final AccountIdentifier accountIdentifier;

	private final Repository<AccountEntity, AccountIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountIdentifier accountIdentifier
	 * @param repository        repository
	 */
	@Inject
	public ReadAccountPrototype(
			@RequestIdentifier final AccountIdentifier accountIdentifier,
			@ResourceRepository final Repository<AccountEntity, AccountIdentifier> repository) {
		this.accountIdentifier = accountIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<AccountEntity> onRead() {
		return repository.findOne(accountIdentifier);
	}
}
