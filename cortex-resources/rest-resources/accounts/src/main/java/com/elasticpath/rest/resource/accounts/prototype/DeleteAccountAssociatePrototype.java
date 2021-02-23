/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AssociateEntity;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociateResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account associate prototype for delete operation.
 */
public class DeleteAccountAssociatePrototype implements AssociateResource.Delete {
	private final Repository<AssociateEntity, AssociateIdentifier> repository;

	private final AssociateIdentifier associateIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associateIdentifier the identifier
	 * @param repository repository
	 */
	@Inject
	public DeleteAccountAssociatePrototype(
			@RequestIdentifier final AssociateIdentifier associateIdentifier,
			@ResourceRepository final Repository<AssociateEntity, AssociateIdentifier> repository) {
		this.repository = repository;
		this.associateIdentifier = associateIdentifier;
	}

	@Override
	public Completable onDelete() {
		return repository.delete(associateIdentifier);
	}
}
