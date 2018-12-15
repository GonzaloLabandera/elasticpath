/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.rest.resource.searches.offer.prototypes;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.searches.OfferSearchFormIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchFormResource;
import com.elasticpath.rest.definition.searches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.SearchOfferEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;
import com.elasticpath.rest.id.IdentifierPart;

/**
 * Submit prototype for offer search form resource.
 */
public class SubmitOfferSearchFormPrototype implements OfferSearchFormResource.SubmitWithResult {

	private final SearchOfferEntity searchKeywordsEntity;

	private final Repository<SearchOfferEntity, OfferSearchResultIdentifier> repository;

	private final OfferSearchFormIdentifier keywordSearchFormIdentifier;

	/**
	 * Constructor.
	 *
	 * @param searchKeywordsEntity SearchKeywordsEntity
	 * @param repository           Repository
	 * @param searchFormIdentifier KeywordSearchFormIdentifier
	 */
	@Inject
	public SubmitOfferSearchFormPrototype(@RequestForm final SearchOfferEntity searchKeywordsEntity,
												 @ResourceRepository final Repository<SearchOfferEntity, OfferSearchResultIdentifier> repository,
												 @RequestIdentifier final OfferSearchFormIdentifier searchFormIdentifier) {
		this.searchKeywordsEntity = searchKeywordsEntity;
		this.repository = repository;
		this.keywordSearchFormIdentifier = searchFormIdentifier;
	}

	@Override
	public Single<SubmitResult<OfferSearchResultIdentifier>> onSubmitWithResult() {
		IdentifierPart<String> scope = keywordSearchFormIdentifier.getSearches().getScope();
		return repository.submit(searchKeywordsEntity, scope);
	}
}
