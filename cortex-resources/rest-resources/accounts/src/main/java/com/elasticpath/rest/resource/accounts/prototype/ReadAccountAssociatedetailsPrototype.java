/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AssociatedetailsEntity;
import com.elasticpath.rest.definition.accounts.AssociatedetailsIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatedetailsResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read Account Associate Prototype.
 */
public class ReadAccountAssociatedetailsPrototype implements AssociatedetailsResource.Read {

	private final Repository<AssociatedetailsEntity, AssociatedetailsIdentifier> repository;

	private final AssociatedetailsIdentifier associatedetailsIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associateDetailsIdentifier the identifier
	 * @param repository repository
	 */
	@Inject
	public ReadAccountAssociatedetailsPrototype(
			@RequestIdentifier final AssociatedetailsIdentifier associateDetailsIdentifier,
			@ResourceRepository final Repository<AssociatedetailsEntity, AssociatedetailsIdentifier> repository) {
		this.repository = repository;
		this.associatedetailsIdentifier = associateDetailsIdentifier;
	}

	@Override
	public Single<AssociatedetailsEntity> onRead() {
		return repository.findOne(associatedetailsIdentifier);
	}
}