/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.profiles.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.profiles.AttributesIdentifier;
import com.elasticpath.rest.definition.profiles.AttributesResource;
import com.elasticpath.rest.definition.profiles.ProfileAttributesEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Customer profile attributes prototype for Read operation.
 */
public class ReadAttributesPrototype implements AttributesResource.Read {

	private final AttributesIdentifier attributeIdentifier;

	private final Repository<ProfileAttributesEntity, AttributesIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param attributeIdentifier profile identifier
	 * @param repository          repository
	 */
	@Inject
	public ReadAttributesPrototype(@RequestIdentifier final AttributesIdentifier attributeIdentifier,
			@ResourceRepository final Repository<ProfileAttributesEntity, AttributesIdentifier> repository) {
		this.attributeIdentifier = attributeIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<ProfileAttributesEntity> onRead() {
		return repository.findOne(attributeIdentifier);
	}

}