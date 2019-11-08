/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.pagination.PaginatedResult;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.service.search.ProductCategorySearchCriteria;

/**
 * Test for {@link OfferSearchResultToSortAttributeSelectorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferSearchResultToSortAttributeSelectorRepositoryImplTest {

	private static final int PAGE_SIZE = 1;

	private static final String PRICE_GUID = "priceGuid";

	private static final String LOCALE_CODE = "en";

	private final IdentifierPart<Map<String, String>> appliedFacets = CompositeIdentifier.of(DEFAULT_APPLIED_FACETS);

	private final IdentifierPart<Map<String, String>> searchId = CompositeIdentifier.of(ImmutableMap.of("keyword", "key"));

	private final IdentifierPart<Integer> pageId = IntegerIdentifier.of(1);

	private final IdentifierPart<String> scopeId = StringIdentifier.of(SCOPE);

	private final OfferSearchResultIdentifier offerSearchResultIdentifier = OfferSearchResultIdentifier.builder()
			.withSearchId(searchId)
			.withScope(scopeId)
			.withAppliedFacets(appliedFacets)
			.withPageId(pageId)
			.build();

	@InjectMocks
	private OfferSearchResultToSortAttributeSelectorRepositoryImpl<OfferSearchResultIdentifier, SortAttributeSelectorIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock
	private ProductCategorySearchCriteria searchCriteria;

	@Mock
	private PaginatedResult paginatedResult;

	@Before
	public void setUp() {
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocale(SCOPE, USER_ID, LOCALE));
	}

	@Test
	public void verifyGetElementsReturnEmptyWhenThereAreNoSortAttributes() {
		when(searchRepository.getSortAttributeGuidsForStoreAndLocale(SCOPE, LOCALE_CODE))
				.thenReturn(Observable.fromIterable(ImmutableList.of()));

		repository.getElements(offerSearchResultIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void verifyGetElementsReturnEmptyWhenThereAreNoOffersInTheResult() {
		mockSearchResult(0);

		repository.getElements(offerSearchResultIdentifier)
				.test()
				.assertNoErrors()
				.assertNoValues();
	}

	@Test
	public void verifyGetElementsReturnIdentifierWhenSortAttributesExist() {
		mockSearchResult(PAGE_SIZE);

		repository.getElements(offerSearchResultIdentifier)
				.test()
				.assertNoErrors()
				.assertValueCount(PAGE_SIZE);
	}

	private void mockSearchResult(final int totalNumberOfOffers) {
		when(searchRepository.getSortAttributeGuidsForStoreAndLocale(SCOPE, LOCALE_CODE))
				.thenReturn(Observable.fromIterable(ImmutableList.of(PRICE_GUID)));
		when(searchRepository.getSearchCriteria(any(), any(), any()))
				.thenReturn(Single.just(searchCriteria));
		when(searchRepository.searchForProductIds(searchCriteria, PAGE_SIZE, PAGE_SIZE))
				.thenReturn(Single.just(paginatedResult));

		when(paginatedResult.getTotalNumberOfResults()).thenReturn(totalNumberOfOffers);
	}
}
