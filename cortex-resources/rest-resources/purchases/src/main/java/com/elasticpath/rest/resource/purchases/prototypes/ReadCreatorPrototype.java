/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.purchases.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.accounts.AccountEntity;
import com.elasticpath.rest.definition.purchases.PurchaseCreatorEntity;
import com.elasticpath.rest.definition.purchases.PurchaseCreatorIdentifier;
import com.elasticpath.rest.definition.purchases.PurchaseCreatorResource;
import com.elasticpath.rest.definition.purchases.PurchaseIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * creator prototype for Read operation.
 */
public class ReadCreatorPrototype implements PurchaseCreatorResource.Read {

	private final PurchaseCreatorIdentifier identifier;

	private final Repository<PurchaseCreatorEntity, PurchaseCreatorIdentifier> repository;

	//This unused field is required to access the accountEntityPurchaseIdentifierRepository in PurchaseIdParameterStrategy
	@SuppressWarnings("PMD.UnusedPrivateField")
	private final Repository<AccountEntity, PurchaseIdentifier> accountEntityPurchaseIdentifierRepository;

	/**
	 * Constructor.
	 *
	 * @param identifier identifier
	 * @param repository repository
	 * @param accountEntityPurchaseIdentifierRepository accountEntityPurchaseIdentifierRepository
	 */
	@Inject
	public ReadCreatorPrototype(
			@RequestIdentifier final PurchaseCreatorIdentifier identifier,
			@ResourceRepository final Repository<PurchaseCreatorEntity, PurchaseCreatorIdentifier> repository,
			@ResourceRepository final Repository<AccountEntity, PurchaseIdentifier> accountEntityPurchaseIdentifierRepository) {
		this.identifier = identifier;
		this.repository = repository;
		this.accountEntityPurchaseIdentifierRepository = accountEntityPurchaseIdentifierRepository;
	}

	@Override
	public Single<PurchaseCreatorEntity> onRead() {
		return repository.findOne(identifier);
	}
}
