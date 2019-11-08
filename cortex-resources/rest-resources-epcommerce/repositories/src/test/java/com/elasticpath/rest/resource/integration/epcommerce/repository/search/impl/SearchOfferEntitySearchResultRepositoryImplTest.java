/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SORT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.search.SortAttribute;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SearchOfferEntity;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;

/**
 * Test for {@link SearchOfferEntitySearchResultRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchOfferEntitySearchResultRepositoryImplTest {

	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");
	private static final String KEYWORDS = "keywords";
	private static final String GUID = "guid";
	private static final int PAGE_SIZE = 1;

	@InjectMocks
	private SearchOfferEntitySearchResultRepositoryImpl<SearchOfferEntity, OfferSearchResultIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	@Mock
	private SortAttribute sortAttribute;

	@Before
	public void setUp() {
		when(searchRepository.validate(any(SearchOfferEntity.class))).thenReturn(Completable.complete());
	}

	@Test
	public void shouldCreateIdentifierAndNotPopulateSortingWhenDefaultSortAttributeDoesNotExist() {
		when(searchRepository.getDefaultSortAttributeForStore(anyString())).thenReturn(Maybe.empty());

		repository.submit(SearchOfferEntity.builder()
						.withKeywords(KEYWORDS)
						.withPageSize(PAGE_SIZE)
						.build(),
				SCOPE)
				.test()
				.assertValue(value -> !value.getIdentifier().getSearchId().getValue().containsKey(SORT));
	}

	@Test
	public void shouldCreateIdentifierAndPopulateSortingWhenDefaultSortAttributeExists() {
		when(searchRepository.getDefaultSortAttributeForStore(anyString())).thenReturn(Maybe.just(sortAttribute));
		when(sortAttribute.getGuid()).thenReturn(GUID);

		repository.submit(SearchOfferEntity.builder()
						.withKeywords(KEYWORDS)
						.withPageSize(PAGE_SIZE)
						.build(),
				SCOPE)
				.test()
				.assertValue(value -> value.getIdentifier().getSearchId().getValue().get(SORT).equals(GUID));
	}

}