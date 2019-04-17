/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CURRENCY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

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

import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;

/**
 * Test for {@link OfferSearchResultToFacetsRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OfferSearchResultToFacetsRepositoryImplTest {

	private static final String FACET_FIELD = "field";

	private static final List<String> FACET_FIELDS = ImmutableList.of(FACET_FIELD);

	private static final OfferSearchResultIdentifier OFFER_SEARCH_RESULT_IDENTIFIER = OfferSearchResultIdentifier.builder()
			.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
			.withPageId(IntegerIdentifier.of(1))
			.withScope(StringIdentifier.of(SCOPE))
			.withSearchId(CompositeIdentifier.of(ImmutableMap.of("page-size", "20")))
			.build();

	private final ProductCategorySearchCriteria searchCriteria = new ProductSearchCriteria();

	@InjectMocks
	private OfferSearchResultToFacetsRepositoryImpl<OfferSearchResultIdentifier, FacetsIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Before
	public void setUp() {
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE, USER_ID, LOCALE, CURRENCY));
		when(searchRepository.getSearchCriteria(
				nullable(String.class), anyString(), any(Locale.class), any(Currency.class), anyMap(), nullable(String.class)))
				.thenReturn(Single.just(searchCriteria));
	}

	@Test
	public void ensureGetElementsReturnEmptyWhenFacetValuesAreEmpty() {
		when(searchRepository.getFacetFields(any(ProductCategorySearchCriteria.class), anyInt(), anyInt())).thenReturn(Observable.empty());

		repository.getElements(OFFER_SEARCH_RESULT_IDENTIFIER).test()
				.assertNoValues();
	}

	@Test
	public void ensureGetElementsReturnFacetsIdentifierWhenFacetValuesExists() {
		when(searchRepository.getFacetFields(any(ProductCategorySearchCriteria.class), anyInt(), anyInt()))
				.thenReturn(Observable.fromIterable(FACET_FIELDS));

		repository.getElements(OFFER_SEARCH_RESULT_IDENTIFIER)
				.test()
				.assertValueCount(1);
	}
}
