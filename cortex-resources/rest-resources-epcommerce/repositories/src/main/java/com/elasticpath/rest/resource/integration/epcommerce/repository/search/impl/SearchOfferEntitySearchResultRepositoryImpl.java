/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.FIRST_PAGE;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository for search offer entity.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class SearchOfferEntitySearchResultRepositoryImpl<E extends SearchOfferEntity, I extends OfferSearchResultIdentifier>
		implements Repository<SearchOfferEntity, OfferSearchResultIdentifier> {

	private SearchRepository searchRepository;

	@Override
	public Single<SubmitResult<OfferSearchResultIdentifier>> submit(final SearchOfferEntity searchOfferEntity,
																	  final IdentifierPart<String> scope) {
		return searchRepository.validate(searchOfferEntity)
				.andThen(Single.just(SubmitResult.<OfferSearchResultIdentifier>builder()
						.withIdentifier(buildKeywordSearchResultIdentifier(searchOfferEntity, scope))
						.withStatus(SubmitStatus.CREATED)
						.build()));
	}

	private OfferSearchResultIdentifier buildKeywordSearchResultIdentifier(final SearchOfferEntity searchKeywordsEntity,
																			 final IdentifierPart<String> scope) {
		return OfferSearchResultIdentifier.builder()
				.withSearchId(CompositeIdentifier.of(ImmutableMap.of(
						SearchKeywordsEntity.KEYWORDS_PROPERTY, searchKeywordsEntity.getKeywords(),
						SearchKeywordsEntity.PAGE_SIZE_PROPERTY, String.valueOf(searchKeywordsEntity.getPageSize())
				)))
				.withPageId(IntegerIdentifier.of(FIRST_PAGE))
				.withScope(scope)
				.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
				.build();
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}
}
