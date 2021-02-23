/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountStatusEntity;
import com.elasticpath.rest.definition.accounts.AccountStatusIdentifier;
import com.elasticpath.rest.definition.accounts.AccountStatusResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read Account Status Prototype.
 */
public class ReadAccountStatusPrototype implements AccountStatusResource.Read {

	private final Repository<AccountStatusEntity, AccountStatusIdentifier> repository;

	private final AccountStatusIdentifier accountStatusIdentifier;

	/**
	 * Constructor.
	 *
	 * @param accountStatusIdentifier the identifier
	 * @param repository repository
	 */
	@Inject
	public ReadAccountStatusPrototype(
			@RequestIdentifier final AccountStatusIdentifier accountStatusIdentifier,
			@ResourceRepository final Repository<AccountStatusEntity, AccountStatusIdentifier> repository) {
		this.repository = repository;
		this.accountStatusIdentifier = accountStatusIdentifier;
	}

	@Override
	public Single<AccountStatusEntity> onRead() {
		return repository.findOne(accountStatusIdentifier);
	}
}
