/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.items.CodesEntity;
import com.elasticpath.rest.definition.offers.BatchOffersIdentifier;
import com.elasticpath.rest.definition.offers.BatchOffersLookupFormIdentifier;
import com.elasticpath.rest.definition.offers.BatchOffersLookupFormResource;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Submit batch offer lookup form.
 */
public class SubmitBatchOfferLookupFormPrototype implements BatchOffersLookupFormResource.SubmitWithResult {

	private final CodesEntity codesEntity;
	private final Repository<CodesEntity, BatchOffersIdentifier> repository;
	private final BatchOffersLookupFormIdentifier batchOffersLookupFormIdentifier;

	/**
	 * Constructor.
	 * @param codesEntity codes
	 * @param repository repository
	 * @param batchOffersLookupFormIdentifier identifier
	 */
	@Inject
	public SubmitBatchOfferLookupFormPrototype(@RequestForm final CodesEntity codesEntity,
											   @ResourceRepository final Repository<CodesEntity, BatchOffersIdentifier> repository,
											   @RequestIdentifier final BatchOffersLookupFormIdentifier batchOffersLookupFormIdentifier) {
		this.codesEntity = codesEntity;
		this.repository = repository;
		this.batchOffersLookupFormIdentifier = batchOffersLookupFormIdentifier;
	}

	@Override
	public Single<SubmitResult<BatchOffersIdentifier>> onSubmitWithResult() {
		return repository.submit(codesEntity, batchOffersLookupFormIdentifier.getScope());
	}
}
