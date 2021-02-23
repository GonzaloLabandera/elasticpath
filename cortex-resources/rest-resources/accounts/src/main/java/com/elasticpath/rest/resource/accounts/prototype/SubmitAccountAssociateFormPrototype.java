/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.accounts.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.CreatorRepository;
import com.elasticpath.rest.definition.accounts.AddAssociateFormEntity;
import com.elasticpath.rest.definition.accounts.AddAssociateFormIdentifier;
import com.elasticpath.rest.definition.accounts.AddAssociateFormResource;
import com.elasticpath.rest.definition.accounts.AssociateIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Account Associate Form prototype for Submit operation.
 */
public class SubmitAccountAssociateFormPrototype implements AddAssociateFormResource.SubmitWithResult {
	private final AddAssociateFormEntity addAssociateFormEntity;

	private final CreatorRepository<AddAssociateFormEntity, AddAssociateFormIdentifier, AssociateIdentifier> repository;

	private final AddAssociateFormIdentifier addAssociateFormIdentifier;

	/**
	 * Constructor.
	 *
	 * @param addAssociateFormEntity addAssociateFormEntity
	 * @param addAssociateFormIdentifier addAssociateFormIdentifier
	 * @param repository    repository
	 */
	@Inject
	public SubmitAccountAssociateFormPrototype(@RequestForm final AddAssociateFormEntity addAssociateFormEntity,
			@RequestIdentifier final AddAssociateFormIdentifier addAssociateFormIdentifier,
			@ResourceRepository final CreatorRepository<AddAssociateFormEntity, AddAssociateFormIdentifier, AssociateIdentifier> repository) {
		this.addAssociateFormEntity = addAssociateFormEntity;
		this.addAssociateFormIdentifier = addAssociateFormIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<SubmitResult<AssociateIdentifier>> onSubmitWithResult() {
		return repository.submit(addAssociateFormEntity, addAssociateFormIdentifier);
	}
}