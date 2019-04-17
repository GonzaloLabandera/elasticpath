/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ErrorCheckPredicate.createErrorCheckPredicate;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.CURRENCY;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SEARCH_IDENTIFIER_MAP;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static com.elasticpath.service.search.solr.FacetConstants.APPLIED_FACETS_SEPARATOR;
import static com.elasticpath.service.search.solr.FacetConstants.BRAND;
import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_COUNT;
import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_DISPLAY_NAME;
import static com.elasticpath.service.search.solr.FacetConstants.FACET_VALUE_FILTER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import java.util.Currency;
import java.util.List;
import java.util.Locale;
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

import com.elasticpath.rest.ResourceOperationFailure;
import com.elasticpath.rest.ResourceStatus;
import com.elasticpath.rest.definition.offersearches.FacetIdIdentifierPart;
import com.elasticpath.rest.definition.offersearches.FacetIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetSelectorIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetValueIdentifier;
import com.elasticpath.rest.definition.offersearches.FacetsIdentifier;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.selector.ChoiceStatus;
import com.elasticpath.service.search.ProductCategorySearchCriteria;
import com.elasticpath.service.search.query.ProductSearchCriteria;
import com.elasticpath.service.search.solr.FacetValue;

/**
 * Test for {@link FacetSelectorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FacetSelectorRepositoryImplTest {

	private static final String COUNT = "1";

	private static final String NOT_FOUND = "notFound";

	private static final String CHOSEN_FACET = "chosenFacet";

	private static final String CHOSEN_DISPLAY = "chosenDisplay";

	private static final String CHOOSABLE_DISPLAY = "choosableDisplay";

	private static final String CHOOSABLE_FACET = "choosableFacet";

	private static final String CURRENT_APPLIED_FACET_STRING = CHOSEN_FACET;

	private static final Map<String, String> APPLIED_FACETS = ImmutableMap.of("", "", BRAND, CURRENT_APPLIED_FACET_STRING);

	private static final FacetsIdentifier FACETS_IDENTIFIER = FacetsIdentifier.builder()
			.withSearchId(SEARCH_IDENTIFIER_MAP)
			.withScope(StringIdentifier.of(SCOPE))
			.withAppliedFacets(CompositeIdentifier.of(APPLIED_FACETS))
			.build();

	private static final FacetIdentifier FACET_IDENTIFIER = FacetIdentifier.builder()
			.withFacetId(FacetIdIdentifierPart.of(BRAND))
			.withFacets(FACETS_IDENTIFIER)
			.build();
	private static final FacetSelectorIdentifier FACET_SELECTOR_IDENTIFIER = FacetSelectorIdentifier.builder()
			.withFacet(FACET_IDENTIFIER)
			.build();

	private static final Map<String, String> CHOSEN_VALUE_MAP =
			ImmutableMap.of(FACET_VALUE_DISPLAY_NAME, CHOSEN_DISPLAY, FACET_VALUE_COUNT, COUNT,
					FACET_VALUE_FILTER, CHOSEN_FACET);

	private static final Map<String, String> CHOOSABLE_VALUE_MAP =
			ImmutableMap.of(FACET_VALUE_DISPLAY_NAME, CHOOSABLE_DISPLAY, FACET_VALUE_COUNT, COUNT,
					FACET_VALUE_FILTER, CHOOSABLE_FACET);

	private final ProductCategorySearchCriteria searchCriteria = new ProductSearchCriteria();

	@InjectMocks
	private FacetSelectorRepositoryImpl<FacetSelectorIdentifier, FacetSelectorChoiceIdentifier> repository;

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
	public void ensureGetChoicesReturnChosenAndChoosableFacetValues() {
		FacetValue chosen = new FacetValue(CHOSEN_DISPLAY, CHOSEN_FACET, COUNT);
		FacetValue choosable = new FacetValue(CHOOSABLE_DISPLAY, CHOOSABLE_FACET, COUNT);
		List<FacetValue> facetValues = ImmutableList.of(chosen, choosable);
		when(searchRepository.getFacetValues(anyString(), any(ProductCategorySearchCriteria.class)))
				.thenReturn(Observable.fromIterable(facetValues));

		repository.getChoices(FACET_SELECTOR_IDENTIFIER)
				.test()
				.assertValueAt(0, selectorChoice -> selectorChoice.getStatus() == ChoiceStatus.CHOSEN)
				.assertValueAt(1, selectorChoice -> selectorChoice.getStatus() == ChoiceStatus.CHOOSABLE);
	}

	@Test
	public void ensureGetChoicesReturnsNotFoundWhenGetFacetValuesThrowAnError() {
		when(searchRepository.getFacetValues(anyString(), any(ProductCategorySearchCriteria.class)))
				.thenReturn(Observable.error(ResourceOperationFailure.notFound(NOT_FOUND)));

		repository.getChoices(FACET_SELECTOR_IDENTIFIER)
				.test()
				.assertError(createErrorCheckPredicate(NOT_FOUND, ResourceStatus.NOT_FOUND));
	}

	@Test
	public void ensureGetChoiceReturnsCorrectChoice() throws Exception {

		FacetValueIdentifier facetValueIdentifier = FacetValueIdentifier.builder()
				.withFacet(FACET_IDENTIFIER)
				.withFacetValueId(CompositeIdentifier.of(CHOSEN_VALUE_MAP))
				.withFacet(FACET_IDENTIFIER)
				.build();

		FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier = FacetSelectorChoiceIdentifier.builder()
				.withFacetValue(facetValueIdentifier)
				.build();

		repository.getChoice(facetSelectorChoiceIdentifier)
				.test()
				.assertValue(choice ->
						((FacetValueIdentifier) choice.getDescription()).getFacetValueId().getValue().get(FACET_VALUE_FILTER).equals(CHOSEN_FACET));
	}

	@Test
	public void ensureSelectChoiceRemovesAlreadySelectedChoice() throws Exception {

		FacetValueIdentifier facetValueIdentifier = FacetValueIdentifier.builder()
				.withFacet(FACET_IDENTIFIER)
				.withFacetValueId(CompositeIdentifier.of(CHOSEN_VALUE_MAP))
				.withFacet(FACET_IDENTIFIER)
				.build();

		FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier = FacetSelectorChoiceIdentifier.builder()
				.withFacetValue(facetValueIdentifier)
				.build();

		repository.selectChoice(facetSelectorChoiceIdentifier)
				.test()
				.assertValue(selectResult -> selectResult.getIdentifier().getFacet().getFacets()
						.getAppliedFacets().getValue().get(BRAND) == null);
	}

	@Test
	public void ensureSelectChoiceAddsUnSelectedChoice() {
		FacetValueIdentifier facetValueIdentifier = FacetValueIdentifier.builder()
				.withFacet(FACET_IDENTIFIER)
				.withFacetValueId(CompositeIdentifier.of(CHOOSABLE_VALUE_MAP))
				.withFacet(FACET_IDENTIFIER)
				.build();

		FacetSelectorChoiceIdentifier facetSelectorChoiceIdentifier = FacetSelectorChoiceIdentifier.builder()
				.withFacetValue(facetValueIdentifier)
				.build();

		String expectedAppliedFacetString = CHOSEN_FACET + APPLIED_FACETS_SEPARATOR + CHOOSABLE_FACET;

		repository.selectChoice(facetSelectorChoiceIdentifier)
				.test()
				.assertValue(selectResult -> selectResult.getIdentifier().getFacet().getFacets()
						.getAppliedFacets().getValue().get(BRAND).equals(expectedAppliedFacetString));
	}

}