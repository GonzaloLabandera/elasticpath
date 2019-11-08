/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormResource;
import com.elasticpath.rest.definition.purchases.PurchaseFormEntity;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Create Purchase Form prototype for Submit operation.
 */
public class SubmitCreatePurchaseFormPrototype implements CreatePurchaseFormResource.SubmitWithResult {

	private final CreatePurchaseFormIdentifier createPurchaseFormIdentifier;
	private final Repository<PurchaseFormEntity, PurchaseIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param createPurchaseFormIdentifier form identifier for purchase creation
	 * @param repository repository
	 */
	@Inject
	public SubmitCreatePurchaseFormPrototype(
			@RequestIdentifier final CreatePurchaseFormIdentifier createPurchaseFormIdentifier,
			@ResourceRepository final Repository<PurchaseFormEntity, PurchaseIdentifier> repository) {
		this.createPurchaseFormIdentifier = createPurchaseFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<SubmitResult<PurchaseIdentifier>> onSubmitWithResult() {
		return repository.submit(PurchaseFormEntity.builder().build(), createPurchaseFormIdentifier.getOrder().getScope());
	}
}
