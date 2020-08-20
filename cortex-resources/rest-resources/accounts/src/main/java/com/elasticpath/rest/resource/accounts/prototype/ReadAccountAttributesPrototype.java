/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountAttributesEntity;
import com.elasticpath.rest.definition.accounts.AccountAttributesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountAttributesResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account Attributes prototype for Read operation.
 */
public class ReadAccountAttributesPrototype implements AccountAttributesResource.Read {

	private final AccountAttributesIdentifier accountAttributesIdentifier;

	private final Repository<AccountAttributesEntity, AccountAttributesIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountAttributesIdentifier accountIdentifier
	 * @param repository                  repository
	 */
	@Inject
	public ReadAccountAttributesPrototype(
			@RequestIdentifier final AccountAttributesIdentifier accountAttributesIdentifier,
			@ResourceRepository final Repository<AccountAttributesEntity, AccountAttributesIdentifier> repository) {
		this.repository = repository;
		this.accountAttributesIdentifier = accountAttributesIdentifier;
	}

	@Override
	public Single<AccountAttributesEntity> onRead() {
		return repository.findOne(accountAttributesIdentifier);
	}
}
