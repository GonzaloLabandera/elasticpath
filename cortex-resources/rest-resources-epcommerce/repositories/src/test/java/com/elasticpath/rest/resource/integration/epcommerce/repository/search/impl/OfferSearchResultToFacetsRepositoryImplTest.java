/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CATALOG_CODE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CURRENCY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.domain.store.Store;
import com.elasticpath.rest.definition.searches.FacetsIdentifier;
import com.elasticpath.rest.definition.searches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.searches.SearchesIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.resource.integration.epcommerce.repository.store.StoreRepository;
import com.elasticpath.service.search.query.KeywordSearchCriteria;

/**
 * Test for {@link OfferSearchResultToFacetsRepositoryImpl}
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferSearchResultToFacetsRepositoryImplTest {

	private static final String FACET_FIELD = "field";

	private static final List<String> FACET_FIELDS = ImmutableList.of(FACET_FIELD);

	private static final OfferSearchResultIdentifier OFFER_SEARCH_RESULT_IDENTIFIER = OfferSearchResultIdentifier.builder()
			.withAppliedFacets(CompositeIdentifier.of(ImmutableMap.of("", "")))
			.withPageId(IntegerIdentifier.of(1))
			.withSearches(SearchesIdentifier.builder().withScope(StringIdentifier.of(SCOPE)).build())
			.withSearchId(CompositeIdentifier.of(ImmutableMap.of("page-size", "20")))
			.build();

	@InjectMocks
	private OfferSearchResultToFacetsRepositoryImpl<OfferSearchResultIdentifier, FacetsIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	@Mock
	private StoreRepository storeRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Mock(answer = Answers.RETURNS_DEEP_STUBS)
	private Store store;

	@Before
	public void setUp() {
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE, USER_ID, LOCALE, CURRENCY));
		when(storeRepository.findStoreAsSingle(SCOPE)).thenReturn(Single.just(store));
		when(store.getCode()).thenReturn(SCOPE);
		when(store.getCatalog().getCode()).thenReturn(CATALOG_CODE);
	}

	@Test
	public void ensureGetElementsReturnEmptyWhenFacetValuesAreEmpty() {
		when(searchRepository.getFacetFields(any(KeywordSearchCriteria.class), anyInt())).thenReturn(Observable.empty());

		repository.getElements(OFFER_SEARCH_RESULT_IDENTIFIER).test()
				.assertNoValues();
	}

	@Test
	public void ensureGetElementsReturnFacetsIdentifierWhenFacetValuesExists() {
		when(searchRepository.getFacetFields(any(KeywordSearchCriteria.class), anyInt())).thenReturn(Observable.fromIterable(FACET_FIELDS));

		repository.getElements(OFFER_SEARCH_RESULT_IDENTIFIER)
				.test()
				.assertValueCount(1);
	}
}
