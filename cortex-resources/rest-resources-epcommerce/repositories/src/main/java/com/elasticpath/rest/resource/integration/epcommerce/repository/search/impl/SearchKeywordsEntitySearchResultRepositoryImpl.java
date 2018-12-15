/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl.SearchRepositoryImpl.FIRST_PAGE;

import com.google.common.collect.ImmutableMap;
import io.reactivex.Single;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.searches.KeywordSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.SearchKeywordsEntity;
import com.elasticpath.rest.definition.searches.SearchesIdentifier;
import com.elasticpath.rest.form.SubmitResult;
import com.elasticpath.rest.form.SubmitStatus;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Repository for search keywords entity.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component
public class SearchKeywordsEntitySearchResultRepositoryImpl<E extends SearchKeywordsEntity, I extends KeywordSearchResultIdentifier>
		implements Repository<SearchKeywordsEntity, KeywordSearchResultIdentifier> {

	private SearchRepository searchRepository;

	@Override
	public Single<SubmitResult<KeywordSearchResultIdentifier>> submit(final SearchKeywordsEntity searchKeywordsEntity,
																	  final IdentifierPart<String> scope) {
		return searchRepository.validate(searchKeywordsEntity)
				.andThen(Single.just(SubmitResult.<KeywordSearchResultIdentifier>builder()
						.withIdentifier(buildKeywordSearchResultIdentifier(searchKeywordsEntity, scope))
						.withStatus(SubmitStatus.CREATED)
						.build()));
	}

	private KeywordSearchResultIdentifier buildKeywordSearchResultIdentifier(final SearchKeywordsEntity searchKeywordsEntity,
																			 final IdentifierPart<String> scope) {
		return KeywordSearchResultIdentifier.builder()
				.withSearchId(CompositeIdentifier.of(ImmutableMap.of(
						SearchKeywordsEntity.KEYWORDS_PROPERTY, searchKeywordsEntity.getKeywords(),
						SearchKeywordsEntity.PAGE_SIZE_PROPERTY, String.valueOf(searchKeywordsEntity.getPageSize())
				)))
				.withPageId(IntegerIdentifier.of(FIRST_PAGE))
				.withSearches(SearchesIdentifier.builder().withScope(scope).build())
				.build();
	}

	@Reference
	public void setSearchRepository(final SearchRepository searchRepository) {
		this.searchRepository = searchRepository;
	}
}
