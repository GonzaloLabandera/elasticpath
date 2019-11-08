/*
 * Copyright © 2019 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.rest.resource.integration.epcommerce.repository.search.impl;

import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.LOCALE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.SCOPE;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.ResourceTestConstants.USER_ID;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.DEFAULT_APPLIED_FACETS;
import static com.elasticpath.rest.resource.integration.epcommerce.repository.search.OffersResourceConstants.SORT;
import static org.mockito.Mockito.when;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactivex.Observable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.elasticpath.rest.definition.offersearches.OfferSearchResultIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorChoiceIdentifier;
import com.elasticpath.rest.definition.offersearches.SortAttributeSelectorIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.CompositeIdentifier;
import com.elasticpath.rest.id.type.IntegerIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.identity.TestSubjectFactory;
import com.elasticpath.rest.resource.ResourceOperationContext;
import com.elasticpath.rest.resource.integration.epcommerce.repository.search.SearchRepository;
import com.elasticpath.rest.selector.ChoiceStatus;

/**
 * Test for {@link SortAttributeSelectorRepositoryImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SortAttributeSelectorRepositoryImplTest {

	private static final String PRICE_GUID = "priceGuid";

	private static final String RATING_GUID = "ratingGuid";

	private static final String LOCALE_CODE = "en";

	private final IdentifierPart<Map<String, String>> appliedFacets = CompositeIdentifier.of(DEFAULT_APPLIED_FACETS);

	private final IdentifierPart<Map<String, String>> searchId = CompositeIdentifier.of(ImmutableMap.of(SORT, RATING_GUID, "keyword", "key"));

	private final IdentifierPart<Integer> pageId = IntegerIdentifier.of(1);

	private final IdentifierPart<String> scopeId = StringIdentifier.of(SCOPE);

	private final OfferSearchResultIdentifier offerSearchResultIdentifier = OfferSearchResultIdentifier.builder()
			.withSearchId(searchId)
			.withScope(scopeId)
			.withAppliedFacets(appliedFacets)
			.withPageId(pageId)
			.build();

	private final SortAttributeSelectorIdentifier selectorIdentifier = SortAttributeSelectorIdentifier.builder()
			.withOfferSearchResult(offerSearchResultIdentifier)
			.build();

	private final SortAttributeSelectorChoiceIdentifier selectorChoiceIdentifier = SortAttributeSelectorChoiceIdentifier.builder()
			.withSortAttributeId(StringIdentifier.of(RATING_GUID))
			.withSortAttributeSelector(selectorIdentifier)
			.build();

	@InjectMocks
	private SortAttributeSelectorRepositoryImpl<SortAttributeSelectorIdentifier, SortAttributeSelectorChoiceIdentifier> repository;

	@Mock
	private SearchRepository searchRepository;

	@Mock
	private ResourceOperationContext resourceOperationContext;

	@Before
	public void setUp() {
		when(resourceOperationContext.getSubject())
				.thenReturn(TestSubjectFactory.createWithScopeAndUserIdAndLocale(SCOPE, USER_ID, LOCALE));
		when(searchRepository.getSortAttributeGuidsForStoreAndLocale(SCOPE, LOCALE_CODE))
				.thenReturn(Observable.fromIterable(ImmutableList.of(PRICE_GUID, RATING_GUID)));
	}

	@Test
	public void verifyGetChoicesReturnChosenAndChoosable() {

		repository.getChoices(selectorIdentifier)
				.test()
				.assertValueAt(0, selectorChoice -> selectorChoice.getStatus() == ChoiceStatus.CHOOSABLE)
				.assertValueAt(1, selectorChoice -> selectorChoice.getStatus() == ChoiceStatus.CHOSEN);
	}

	@Test
	public void verifyGetChoiceReturnsCorrectChoice() {
		repository.getChoice(selectorChoiceIdentifier)
				.test()
				.assertValue(choice -> {
					SortAttributeIdentifier sortAttributeIdentifier = ((SortAttributeIdentifier) choice.getDescription());
					return sortAttributeIdentifier.getSortAttributeSelectorChoice().getSortAttributeId().getValue().equals(RATING_GUID);
				});
	}

	@Test
	public void verifySelectChoiceChangesSelection() {
		SortAttributeSelectorChoiceIdentifier identifier = SortAttributeSelectorChoiceIdentifier.builder()
				.withSortAttributeSelector(selectorIdentifier)
				.withSortAttributeId(StringIdentifier.of(PRICE_GUID))
				.build();

		repository.selectChoice(identifier)
				.test()
				.assertValue(choice -> choice.getIdentifier().getOfferSearchResult().getSearchId().getValue().get(SORT).equals(PRICE_GUID));
	}
}
