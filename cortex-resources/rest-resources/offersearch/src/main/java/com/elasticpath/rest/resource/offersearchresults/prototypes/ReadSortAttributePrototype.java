/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offersearchresults.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offersearches.SortAttributeEntity;
import com.elasticpath.rest.definition.offersearches.SortAttributeIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Read sort attribute.
 */
public class ReadSortAttributePrototype implements SortAttributeResource.Read {

	private final SortAttributeIdentifier identifier;

	private final Repository<SortAttributeEntity, SortAttributeIdentifier> repository;

	/**
	 * Constructor.
	 * @param identifier identifier
	 * @param repository repository
	 */
	@Inject
	public ReadSortAttributePrototype(@RequestIdentifier final SortAttributeIdentifier identifier,
									  @ResourceRepository final  Repository<SortAttributeEntity, SortAttributeIdentifier> repository) {
		this.identifier = identifier;
		this.repository = repository;
	}

	@Override
	public Single<SortAttributeEntity> onRead() {
		return repository.findOne(identifier);
	}
}
