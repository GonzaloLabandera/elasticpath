/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.offers.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offers.CodeEntity;
import com.elasticpath.rest.definition.offers.OfferIdentifier;
import com.elasticpath.rest.definition.offers.OfferLookupFormIdentifier;
import com.elasticpath.rest.definition.offers.OfferLookupFormResource;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Submit offerlookupform.
 */
public class SubmitOfferLookupFormPrototype implements OfferLookupFormResource.SubmitWithResult {

	private final CodeEntity codeEntity;

	private final Repository<CodeEntity, OfferIdentifier> repository;

	private final OfferLookupFormIdentifier offerLookupFormIdentifier;

	/**
	 * Constructor.
	 * @param codeEntity the product code
	 * @param repository repository
	 * @param offerLookupFormIdentifier identifier
	 */
	@Inject
	public SubmitOfferLookupFormPrototype(@RequestForm final CodeEntity codeEntity,
										  @ResourceRepository final Repository<CodeEntity, OfferIdentifier> repository,
										  @RequestIdentifier final OfferLookupFormIdentifier offerLookupFormIdentifier) {
		this.codeEntity = codeEntity;
		this.repository = repository;
		this.offerLookupFormIdentifier = offerLookupFormIdentifier;
	}

	@Override
	public Single<SubmitResult<OfferIdentifier>> onSubmitWithResult() {
		return repository.submit(codeEntity, offerLookupFormIdentifier.getScope());
	}
}
