/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AssociateEntity;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read Account Associate Prototype.
 */
public class ReadAccountAssociatePrototype implements AssociateResource.Read {

	private final Repository<AssociateEntity, AssociateIdentifier> repository;

	private final AssociateIdentifier associateIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associateIdentifier the identifier
	 * @param repository repository
	 */
	@Inject
	public ReadAccountAssociatePrototype(
			@RequestIdentifier final AssociateIdentifier associateIdentifier,
			@ResourceRepository final Repository<AssociateEntity, AssociateIdentifier> repository) {
		this.repository = repository;
		this.associateIdentifier = associateIdentifier;
	}

	@Override
	public Single<AssociateEntity> onRead() {
		return repository.findOne(associateIdentifier);
	}
}
