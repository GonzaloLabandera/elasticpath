/*
 * Copyright © 2021 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesEntity;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesIdentifier;
import com.elasticpath.rest.definition.accounts.AccountBuyerRolesResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account buyer roles prototype for read operation.
 */
public class ReadAccountBuyerRolesPrototype implements AccountBuyerRolesResource.Read {

	private final AccountBuyerRolesIdentifier identifier;
	private final Repository<AccountBuyerRolesEntity, AccountBuyerRolesIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param identifier identifier.
	 * @param repository the repository.
	 */
	@Inject
	public ReadAccountBuyerRolesPrototype(@RequestIdentifier final AccountBuyerRolesIdentifier identifier,
										   @ResourceRepository final Repository<AccountBuyerRolesEntity,
												   AccountBuyerRolesIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Single<AccountBuyerRolesEntity> onRead() {
		return repository.findOne(identifier);
	}
}
