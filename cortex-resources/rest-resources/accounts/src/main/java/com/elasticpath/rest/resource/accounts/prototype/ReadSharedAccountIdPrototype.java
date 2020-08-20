/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.SharedAccountIdIdentifier;
import com.elasticpath.rest.definition.accounts.SharedAccountIdResource;
import com.elasticpath.rest.definition.accounts.SharedIdEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read Shared Account Id Prototype.
 */
public class ReadSharedAccountIdPrototype implements SharedAccountIdResource.Read {

	private final Repository<SharedIdEntity, SharedAccountIdIdentifier> repository;

	private final SharedAccountIdIdentifier sharedAccountIdIdentifier;

	/**
	 * Constructor.
	 *
	 * @param sharedAccountIdIdentifier the identifier
	 * @param repository repository
	 */
	@Inject
	public ReadSharedAccountIdPrototype(
			@RequestIdentifier final SharedAccountIdIdentifier sharedAccountIdIdentifier,
			@ResourceRepository final Repository<SharedIdEntity, SharedAccountIdIdentifier> repository) {
		this.repository = repository;
		this.sharedAccountIdIdentifier = sharedAccountIdIdentifier;
	}

	@Override
	public Single<SharedIdEntity> onRead() {
		return repository.findOne(sharedAccountIdIdentifier);
	}
}
