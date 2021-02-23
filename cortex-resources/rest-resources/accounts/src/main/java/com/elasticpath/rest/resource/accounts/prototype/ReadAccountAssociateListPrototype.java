/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.repository.LinksRepository;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesIdentifier;
import com.elasticpath.rest.definition.accounts.AssociatesResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read Account Associate List Prototype.
 */
public class ReadAccountAssociateListPrototype implements AssociatesResource.Read {

	private final LinksRepository<AssociatesIdentifier, AssociateIdentifier> repository;

	private final AssociatesIdentifier associatesIdentifier;

	/**
	 * Constructor.
	 *
	 * @param associatesIdentifier the identifier
	 * @param repository repository
	 */
	@Inject
	public ReadAccountAssociateListPrototype(
			@RequestIdentifier final AssociatesIdentifier associatesIdentifier,
			@ResourceRepository final LinksRepository<AssociatesIdentifier, AssociateIdentifier> repository) {
		this.repository = repository;
		this.associatesIdentifier = associatesIdentifier;
	}

	@Override
	public Observable<AssociateIdentifier> onRead() {
		return repository.getElements(associatesIdentifier);
	}
}
