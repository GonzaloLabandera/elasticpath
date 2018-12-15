/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.items.lookups.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.items.BatchItemsFormIdentifier;
import com.elasticpath.rest.definition.items.BatchItemsFormResource;
import com.elasticpath.rest.definition.items.BatchItemsIdentifier;
import com.elasticpath.rest.definition.items.CodesEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.type.StringIdentifier;

/**
 * Submit prototype for batch items lookup form resource.
 */
public class SubmitBatchItemsLookupFormPrototype implements BatchItemsFormResource.SubmitWithResult {

	private final CodesEntity codesEntity;

	private final Repository<CodesEntity, BatchItemsIdentifier> repository;

	private final BatchItemsFormIdentifier batchItemsFormIdentifier;

	/**
	 * Constructor.
	 *
	 * @param codesEntity               codesEntity
	 * @param repository                Repository
	 * @param batchItemsFormIdentifier  batchItemsFormIdentifier
	 */
	@Inject
	public SubmitBatchItemsLookupFormPrototype(@RequestForm final CodesEntity codesEntity,
											   @ResourceRepository final Repository<CodesEntity, BatchItemsIdentifier> repository,
											   @RequestIdentifier final BatchItemsFormIdentifier batchItemsFormIdentifier) {
		this.codesEntity = codesEntity;
		this.repository = repository;
		this.batchItemsFormIdentifier = batchItemsFormIdentifier;
	}

	@Override
	public Single<SubmitResult<BatchItemsIdentifier>> onSubmitWithResult() {
		return repository.submit(codesEntity, StringIdentifier.of(batchItemsFormIdentifier.getScope().getValue()));
	}
}
