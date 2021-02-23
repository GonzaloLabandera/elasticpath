/*
 * Copyright © 2020 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.paymentinstructions.prototypes.account;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.paymentinstructions.AccountPaymentInstructionsIdentifier;
import com.elasticpath.rest.definition.paymentinstructions.AccountPaymentInstructionsResource;
import com.elasticpath.rest.definition.paymentinstructions.InstructionsEntity;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Request Instructions Form prototype for Read operation.
 */
public class ReadAccountPaymentInstructionsPrototype implements AccountPaymentInstructionsResource.Read {

	private final AccountPaymentInstructionsIdentifier accountPaymentInstructionsIdentifier;
	private final Repository<InstructionsEntity, AccountPaymentInstructionsIdentifier> repository;

	/**
	 * Constructor.
	 *
	 * @param accountPaymentInstructionsIdentifier {@link AccountPaymentInstructionsIdentifier}
	 * @param repository                           the corresponding identifier-entity repository
	 */
	@Inject
	public ReadAccountPaymentInstructionsPrototype(
			@RequestIdentifier final AccountPaymentInstructionsIdentifier accountPaymentInstructionsIdentifier,
		    @ResourceRepository final Repository<InstructionsEntity, AccountPaymentInstructionsIdentifier> repository) {
		this.accountPaymentInstructionsIdentifier = accountPaymentInstructionsIdentifier;
		this.repository = repository;
	}

	@Override
	public Single<InstructionsEntity> onRead() {
		return repository.findOne(accountPaymentInstructionsIdentifier);
	}
}