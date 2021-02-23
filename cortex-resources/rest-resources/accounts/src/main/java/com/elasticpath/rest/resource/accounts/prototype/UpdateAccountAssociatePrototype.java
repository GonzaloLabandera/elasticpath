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
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account associate prototype for Update operation.
 */
public class UpdateAccountAssociatePrototype  implements AssociateResource.Update {

	private final AssociateEntity associateEntity;

	private final AssociateIdentifier associateIdentifier;

	private final Repository<AssociateEntity, AssociateIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param associateEntity     associateEntity
	 * @param associateIdentifier associateIdentifier
	 * @param repository          repository
	 */
	@Inject
	public UpdateAccountAssociatePrototype(@RequestForm final AssociateEntity associateEntity,
			@RequestIdentifier final AssociateIdentifier associateIdentifier,
			@ResourceRepository final Repository<AssociateEntity, AssociateIdentifier> repository) {
		this.associateEntity = associateEntity;
		this.associateIdentifier = associateIdentifier;
		this.repository = repository;
	}

	@Override
	public Completable onUpdate() {
		return repository.update(associateEntity, associateIdentifier);
	}
}
