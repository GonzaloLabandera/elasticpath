/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.availabilities.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.availabilities.AvailabilityEntity;
import com.elasticpath.rest.definition.availabilities.AvailabilityForOfferIdentifier;
import com.elasticpath.rest.definition.availabilities.AvailabilityForOfferResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Offer Availability prototype for Read operation.
 */
public class ReadAvailabilityForOfferPrototype implements AvailabilityForOfferResource.Read {

	private final AvailabilityForOfferIdentifier availabilityForOfferIdentifier;

	private final Repository<AvailabilityEntity, AvailabilityForOfferIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param availabilityForOfferIdentifier availabilityForOfferIdentifier
	 * @param repository                     repository
	 */
	@Inject
	public ReadAvailabilityForOfferPrototype(
			@RequestIdentifier final AvailabilityForOfferIdentifier availabilityForOfferIdentifier,
			@ResourceRepository final Repository<AvailabilityEntity, AvailabilityForOfferIdentifier> repository) {
		this.availabilityForOfferIdentifier = availabilityForOfferIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<AvailabilityEntity> onRead() {
		return repository.findOne(availabilityForOfferIdentifier);
	}
}
