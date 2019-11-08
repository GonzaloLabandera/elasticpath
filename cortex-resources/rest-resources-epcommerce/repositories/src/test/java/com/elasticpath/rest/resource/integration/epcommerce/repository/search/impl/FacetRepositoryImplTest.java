/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CURRENCY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SEARCH_IDENTIFIER_MAP;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.ImmutableList;
import io.reactivex.Observable;
import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.offersearches.FacetIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;

/**
 * Test for {@link FacetRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FacetRepositoryImplTest {

	private static final String FACET_FIELD = "field";

	private static final List<String> FACET_FIELDS = ImmutableList.of(FACET_FIELD);

	private final ProductCategorySearchCriteria searchCriteria = new ProductSearchCriteria();

	@InjectMocks
	private FacetRepositoryImpl<FacetsIdentifier, FacetIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Before
	public void setUp() {
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocaleAndCurrency(SCOPE, USER_ID, LOCALE, CURRENCY));
		when(searchRepository.getSearchCriteria(any(OfferSearchData.class), any(Locale.class), any(Currency.class)))
				.thenReturn(Single.just(searchCriteria));
		when(searchRepository.getFacetFields(any(ProductCategorySearchCriteria.class), anyInt(), anyInt()))
				.thenReturn(Observable.fromIterable(FACET_FIELDS));
	}

	@Test
	public void ensureGetElementsReturnFacetIdentifiersForFacetFields() {
		FacetsIdentifier facetsIdentifier = FacetsIdentifier.builder()
				.withAppliedFacets(CompositeIdentifier.of(DEFAULT_APPLIED_FACETS))
				.withSearchId(SEARCH_IDENTIFIER_MAP)
				.withScope(StringIdentifier.of(SCOPE))
				.build();

		repository.getElements(facetsIdentifier)
				.test()
				.assertValueAt(0, facetIdentifier -> {
					String facetFields = facetIdentifier.getFacetId().getValue();
					return facetFields.equals(FACET_FIELD);
				});
	}

}